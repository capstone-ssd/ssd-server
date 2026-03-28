package or.hyu.ssd.domain.member.service;

import feign.FeignException;
import feign.Response;
import feign.Util;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.hyu.ssd.domain.member.client.KaKaoOAuthClient;
import or.hyu.ssd.domain.member.client.KaKaoUserInfoClient;
import or.hyu.ssd.domain.member.controller.dto.kakao.KaKaoOAuthTokenDTO;
import or.hyu.ssd.domain.member.controller.dto.kakao.KaKaoUserInfoResponse;
import or.hyu.ssd.domain.member.entity.Member;
import or.hyu.ssd.domain.member.entity.Role;
import or.hyu.ssd.domain.member.repository.MemberRepository;
import or.hyu.ssd.domain.member.service.support.KakaoLoginResult;
import or.hyu.ssd.domain.member.service.support.KakaoProfileExtractor;
import or.hyu.ssd.domain.member.service.support.KakaoProfileExtractor.NormalizedKakaoProfile;
import or.hyu.ssd.global.api.ErrorCode;
import or.hyu.ssd.global.api.handler.UserExceptionHandler;
import or.hyu.ssd.global.config.KaKaoConfig;
import or.hyu.ssd.global.config.properties.CookieConfig;
import or.hyu.ssd.global.config.properties.OAuthProperties;
import or.hyu.ssd.global.jwt.JWTUtil;
import or.hyu.ssd.global.jwt.repository.RefreshTokenRepository;
import or.hyu.ssd.global.oauth.repository.OAuthRedirectStateRepository;
import or.hyu.ssd.global.config.properties.JWTConfig;
import or.hyu.ssd.global.util.CookieUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URLEncoder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuthService {

    private static final String DYNAMIC_CALLBACK_PATH = "/oauth/kakao/callback";

    private final KaKaoOAuthClient kaKaoOAuthClient;
    private final KaKaoUserInfoClient kaKaoUserInfoClient;
    private final MemberRepository userRepository;
    private final JWTUtil jwtUtil;
    private final JWTConfig jwtConfig;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OAuthRedirectStateRepository oAuthRedirectStateRepository;

    private final KaKaoConfig kaKaoConfig;
    private final CookieConfig cookieConfig;
    private final OAuthProperties oAuthProperties;

    /**
     * 카카오 authorize URL 생성 (동적 콜백)
     * - Origin 기반 또는 요청의 스킴/호스트/포트로 "{base}/oauth/kakao/callback"을 계산합니다.
     * - 프론트 콜백 플로우에 사용합니다.
     */
    public String requestRedirect(HttpServletRequest request) {
        String redirectUri = resolveDynamicCallbackUri(request);
        return buildAuthorizeUrl(redirectUri, null);
    }

    /**
     * 카카오 로그인 시작(고정 /redirect 리다이렉트)
     * - 최종 클라이언트 리다이렉트 대상은 요청 Origin/Referer를 기준으로 {base}/redirect 로 계산합니다.
     * - 카카오 콜백 redirect_uri는 항상 서버 콜백 URL을 사용합니다.
     * - 카카오 콜백은 기존 /oauth/kakao/callback 경로를 재사용합니다.
     */
    public String requestRedirectToFixedRedirect(HttpServletRequest request) {
        String redirectUri = resolveClientRedirectUri(request);
        String state = UUID.randomUUID().toString();
        oAuthRedirectStateRepository.save(state, redirectUri, oAuthProperties.getRedirectStateTtlSeconds());
        return buildAuthorizeUrl(resolveServerCallbackUri(request), state);
    }

    /**
     * 프론트에서 받은 code를 기반으로 토큰 교환/회원 처리/JWT 발급을 수행합니다.
     */
    public Boolean kakaoLoginNoState(String accessCode, HttpServletRequest request, HttpServletResponse response) {
        KakaoLoginResult loginResult = completeKakaoLogin(accessCode, resolveDynamicCallbackUri(request));
        writeTokens(response, loginResult);
        return loginResult.isNewUser();
    }

    /**
     * redirect 로그인 콜백
     * - state에 저장된 최종 클라이언트 redirect 주소를 복원합니다.
     * - 로그인 완료 후 refresh cookie를 설정하고 access token은 URL fragment로 전달합니다.
     */
    public void kakaoLoginAndRedirect(String accessCode,
                                      String state,
                                      HttpServletRequest request,
                                      HttpServletResponse response) {

        String redirectUri = oAuthRedirectStateRepository.consume(state)
                .orElseThrow(() -> new UserExceptionHandler(ErrorCode.REQUEST_PARAMETER_INVALID, "'state' 파라미터가 올바르지 않습니다"));

        KakaoLoginResult loginResult = completeKakaoLogin(accessCode, resolveServerCallbackUri(request));
        writeTokens(response, loginResult);
        response.setHeader("Location", buildRedirectLocation(redirectUri, loginResult));
        response.setStatus(HttpServletResponse.SC_FOUND);
    }

    /**
     * 항상 서버 콜백 방식 - 시작 단계 (yml redirect_uri 사용)
     * - yml에 설정된 redirect_uri를 그대로 사용합니다.
     */
    public String requestRedirectServer(HttpServletRequest request) {
        return buildAuthorizeUrl(kaKaoConfig.getRedirectUri(), null);
    }

    /**
     * 항상 서버 콜백 방식 - 카카오로부터 서버 콜백을 받을 때 호출 (yml redirect_uri 사용)
     * - 설정된 redirect_uri로 토큰 교환을 수행합니다.
     */
    public Boolean kakaoLoginServer(String accessCode, HttpServletRequest request, HttpServletResponse response) {
        KakaoLoginResult loginResult = completeKakaoLogin(accessCode, kaKaoConfig.getRedirectUri());
        writeTokens(response, loginResult);
        return loginResult.isNewUser();
    }

    private KakaoLoginResult completeKakaoLogin(String accessCode, String redirectUri) {
        if (!StringUtils.hasText(accessCode)) {
            throw new UserExceptionHandler(ErrorCode.KAKAO_AUTH_CODE_INVALID);
        }

        KaKaoOAuthTokenDTO authorizationCode;
        try {
            log.info("액세스 토큰 발급을 시작합니다");
            authorizationCode = kaKaoOAuthClient.getToken(
                    "authorization_code",
                    kaKaoConfig.getClientId(),
                    redirectUri,
                    accessCode
            );
        } catch (FeignException e) {
            log.info(e.getMessage());
            throw new UserExceptionHandler(ErrorCode.KAKAO_AUTH_CODE_INVALID);
        }

        KaKaoUserInfoResponse userInfo;
        String bearerAccessToken = "Bearer " + authorizationCode.getAccess_token();
        try {
            userInfo = kaKaoUserInfoClient.getUserInfo(bearerAccessToken);
        } catch (FeignException e) {
            throw new UserExceptionHandler(ErrorCode.KAKAO_ACCESSTOKEN_INVALID);
        }

        NormalizedKakaoProfile profile = KakaoProfileExtractor.extract(userInfo);
        if (!StringUtils.hasText(profile.email())) {
            KaKaoUserInfoResponse.KakaoAccount kakaoAccount = userInfo != null ? userInfo.getKakaoAccount() : null;
            log.warn(
                    "카카오 로그인 실패: 이메일이 응답에 없습니다. kakaoId={}, email={}, hasEmail={}, emailNeedsAgreement={}, isEmailValid={}, isEmailVerified={}, nickname={}, connectedAt={}",
                    profile.kakaoId(),
                    kakaoAccount != null ? kakaoAccount.getEmail() : null,
                    kakaoAccount != null ? kakaoAccount.getHasEmail() : null,
                    kakaoAccount != null ? kakaoAccount.getEmailNeedsAgreement() : null,
                    kakaoAccount != null ? kakaoAccount.getIsEmailValid() : null,
                    kakaoAccount != null ? kakaoAccount.getIsEmailVerified() : null,
                    profile.nickname(),
                    userInfo != null ? userInfo.getConnectedAt() : null
            );
            logRawKakaoUserInfoResponse(bearerAccessToken);
            throw new UserExceptionHandler(ErrorCode.KAKAO_AUTH_CODE_INVALID);
        }

        boolean isNewUser = createMemberIfAbsent(profile);

        Member member = userRepository.findByEmail(profile.email())
                .orElseThrow(() -> new UserExceptionHandler(ErrorCode.MEMBER_NOT_FOUND));

        String access = jwtUtil.createJwt("access", member.getId(), member.getRole().toString(), jwtConfig.getAccessTokenValidityInSeconds());
        String refresh = jwtUtil.createJwt("refresh", member.getId(), member.getRole().toString(), jwtConfig.getRefreshTokenValidityInSeconds());

        refreshTokenRepository.saveRefreshToken(member.getId(), refresh, jwtConfig.getRefreshTokenValidityInSeconds());

        return new KakaoLoginResult(access, refresh, isNewUser);
    }

    private boolean createMemberIfAbsent(NormalizedKakaoProfile profile) {
        Boolean userExist = userRepository.existsByEmail(profile.email());
        if (Boolean.TRUE.equals(userExist)) {
            return false;
        }

        String profileImageKey = "kakao:" + (profile.kakaoId() != null ? profile.kakaoId() : UUID.randomUUID());
        Member newMember = Member.join(profile.nickname(), profile.email(), profile.profileImageUrl(), profileImageKey, Role.ROLE_AUTHOR);
        userRepository.save(newMember);
        return true;
    }

    private void writeTokens(HttpServletResponse response, KakaoLoginResult loginResult) {
        response.setHeader("access-token", loginResult.accessToken());
        CookieUtil.addSameSiteCookie(
                response,
                "refresh-token",
                loginResult.refreshToken(),
                jwtConfig.getRefreshTokenValidityInSeconds().intValue(),
                cookieConfig.getDomain(),
                cookieConfig.isSecure(),
                cookieConfig.getSameSite()
        );
    }

    private void logRawKakaoUserInfoResponse(String bearerAccessToken) {
        Response rawResponse = null;
        try {
            rawResponse = kaKaoUserInfoClient.getUserInfoRaw(bearerAccessToken);
            if (rawResponse.body() == null) {
                log.warn("카카오 user/me raw 응답 body가 비어 있습니다. status={}, headers={}", rawResponse.status(), rawResponse.headers());
                return;
            }

            String rawBody = Util.toString(rawResponse.body().asReader(StandardCharsets.UTF_8));
            log.warn("카카오 user/me raw 응답: status={}, body={}", rawResponse.status(), rawBody);
        } catch (FeignException e) {
            log.warn("카카오 user/me raw 응답 조회 실패: {}", e.getMessage());
        } catch (IOException e) {
            log.warn("카카오 user/me raw 응답 로깅 실패", e);
        } finally {
            if (rawResponse != null && rawResponse.body() != null) {
                try {
                    rawResponse.body().close();
                } catch (IOException e) {
                    log.debug("카카오 user/me raw 응답 body close 실패", e);
                }
            }
        }
    }

    private String resolveDynamicCallbackUri(HttpServletRequest request) {
        return resolveRequestBase(request) + DYNAMIC_CALLBACK_PATH;
    }

    private String resolveServerCallbackUri(HttpServletRequest request) {
        return resolveServerBase(request) + DYNAMIC_CALLBACK_PATH;
    }

    private String resolveRequestBase(HttpServletRequest request) {
        String origin = request.getHeader("Origin");
        if (StringUtils.hasText(origin)) {
            if (!isAllowedOrigin(origin)) {
                log.warn("허용되지 않은 Origin: {}", origin);
                throw new UserExceptionHandler(ErrorCode.KAKAO_AUTH_CODE_INVALID);
            }
            return origin;
        }

        String scheme = String.valueOf(request.getHeader("X-Forwarded-Proto"));
        if (!StringUtils.hasText(scheme) || "null".equalsIgnoreCase(scheme)) {
            scheme = request.getScheme();
        }

        String forwardedHost = extractForwardedAuthority(request.getHeader("X-Forwarded-Host"));
        if (StringUtils.hasText(forwardedHost)) {
            return normalizeBase(scheme, forwardedHost);
        }

        String hostHeader = extractForwardedAuthority(request.getHeader("Host"));
        if (StringUtils.hasText(hostHeader)) {
            return normalizeBase(scheme, hostHeader);
        }

        String host = request.getServerName();
        int port = request.getServerPort();
        return normalizeHostPort(scheme, host, port);
    }

    private String resolveServerBase(HttpServletRequest request) {
        String scheme = String.valueOf(request.getHeader("X-Forwarded-Proto"));
        if (!StringUtils.hasText(scheme) || "null".equalsIgnoreCase(scheme)) {
            scheme = request.getScheme();
        }

        String forwardedHost = extractForwardedAuthority(request.getHeader("X-Forwarded-Host"));
        if (StringUtils.hasText(forwardedHost)) {
            return normalizeBase(scheme, forwardedHost);
        }

        String hostHeader = extractForwardedAuthority(request.getHeader("Host"));
        if (StringUtils.hasText(hostHeader)) {
            return normalizeBase(scheme, hostHeader);
        }

        String host = request.getServerName();
        int port = request.getServerPort();
        return normalizeHostPort(scheme, host, port);
    }

    private boolean isAllowedOrigin(String origin) {
        List<String> allowed = oAuthProperties.getAllowedOrigins();
        return allowed == null || allowed.isEmpty() || allowed.contains(origin);
    }

    private String resolveClientRedirectUri(HttpServletRequest request) {
        String base = resolveClientBase(request);
        return base + "/redirect";
    }

    private String resolveClientBase(HttpServletRequest request) {
        String origin = request.getHeader("Origin");
        if (StringUtils.hasText(origin)) {
            if (!isAllowedOrigin(origin)) {
                throw new UserExceptionHandler(ErrorCode.REQUEST_PARAMETER_INVALID, "허용되지 않은 Origin 입니다");
            }
            return origin;
        }

        String referer = request.getHeader("Referer");
        if (StringUtils.hasText(referer)) {
            try {
                URI refererUri = URI.create(referer);
                String refererOrigin = normalizeOrigin(refererUri);
                if (!isAllowedOrigin(refererOrigin)) {
                    throw new UserExceptionHandler(ErrorCode.REQUEST_PARAMETER_INVALID, "허용되지 않은 Referer 입니다");
                }
                return refererOrigin;
            } catch (IllegalArgumentException e) {
                throw new UserExceptionHandler(ErrorCode.REQUEST_PARAMETER_INVALID, "Referer 형식이 올바르지 않습니다");
            }
        }

        return resolveServerBase(request);
    }

    private String normalizeOrigin(URI uri) {
        String scheme = uri.getScheme();
        if (!StringUtils.hasText(scheme) || !StringUtils.hasText(uri.getHost())) {
            throw new IllegalArgumentException("Origin 형식이 올바르지 않습니다");
        }

        String normalizedScheme = scheme.toLowerCase();
        int port = uri.getPort();
        boolean isDefaultPort = port == -1
                || ("http".equalsIgnoreCase(normalizedScheme) && port == 80)
                || ("https".equalsIgnoreCase(normalizedScheme) && port == 443);
        return normalizedScheme + "://" + uri.getHost() + (isDefaultPort ? "" : ":" + port);
    }

    private String extractForwardedAuthority(String rawHeader) {
        if (!StringUtils.hasText(rawHeader)) {
            return null;
        }
        return rawHeader.split(",")[0].trim();
    }

    private String normalizeBase(String scheme, String authority) {
        try {
            URI uri = URI.create(scheme + "://" + authority);
            return normalizeOrigin(uri);
        } catch (IllegalArgumentException ignored) {
            int delimiter = authority.lastIndexOf(':');
            if (delimiter < 0) {
                return normalizeHostPort(scheme, authority, -1);
            }

            String host = authority.substring(0, delimiter);
            try {
                int port = Integer.parseInt(authority.substring(delimiter + 1));
                return normalizeHostPort(scheme, host, port);
            } catch (NumberFormatException ex) {
                return normalizeHostPort(scheme, authority, -1);
            }
        }
    }

    private String normalizeHostPort(String scheme, String host, int port) {
        String normalizedScheme = scheme.toLowerCase();
        boolean shouldDropPort = port == -1
                || ("http".equalsIgnoreCase(normalizedScheme) && port == 80)
                || ("https".equalsIgnoreCase(normalizedScheme) && (port == 443 || port == 80));
        return normalizedScheme + "://" + host + (shouldDropPort ? "" : ":" + port);
    }

    private String buildAuthorizeUrl(String redirectUri, String state) {
        String encodedRedirect = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
        String encodedScope = URLEncoder.encode(kaKaoConfig.getScope(), StandardCharsets.UTF_8);

        String url = String.format(
                "https://kauth.kakao.com/oauth/authorize?client_id=%s&redirect_uri=%s&response_type=code&scope=%s&prompt=consent",
                kaKaoConfig.getClientId(), encodedRedirect, encodedScope
        );
        if (StringUtils.hasText(state)) {
            url += "&state=" + URLEncoder.encode(state, StandardCharsets.UTF_8);
        }
        return url;
    }

    private String buildRedirectLocation(String redirectUri, KakaoLoginResult loginResult) {
        String fragment = "accessToken=" + URLEncoder.encode(loginResult.accessToken(), StandardCharsets.UTF_8)
                + "&isNewUser=" + loginResult.isNewUser();

        if (redirectUri.contains("#")) {
            return redirectUri + "&" + fragment;
        }
        return redirectUri + "#" + fragment;
    }
}
