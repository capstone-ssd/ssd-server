package or.hyu.ssd.domain.member.service;

import feign.FeignException;
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
import or.hyu.ssd.global.jwt.repository.RefreshTokenRepository;
import or.hyu.ssd.global.api.ErrorCode;
import or.hyu.ssd.global.api.handler.UserExceptionHandler;
import or.hyu.ssd.global.config.properties.CookieConfig;
import or.hyu.ssd.global.config.properties.JWTConfig;
import or.hyu.ssd.global.config.KaKaoConfig;
import or.hyu.ssd.global.config.properties.OAuthProperties;
import or.hyu.ssd.global.jwt.JWTUtil;
import or.hyu.ssd.global.util.CookieUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class OAuthService {

    private final KaKaoOAuthClient kaKaoOAuthClient;
    private final KaKaoUserInfoClient kaKaoUserInfoClient;
    private final MemberRepository userRepository;
    private final JWTUtil jwtUtil;
    private final JWTConfig jwtConfig;
    private final RefreshTokenRepository refreshTokenRepository;

    private final KaKaoConfig kaKaoConfig;
    private final CookieConfig cookieConfig;
    private final OAuthProperties oAuthProperties;



    /**
     * 카카오 authorize URL 생성 (동적 콜백)
     * - Origin 기반 또는 요청의 스킴/호스트/포트로 "{base}/oauth/kakao/callback"을 계산합니다.
     * - 프론트 콜백 플로우에 사용합니다.
     */
    public String requestRedirect(jakarta.servlet.http.HttpServletRequest request) {
        String origin = request.getHeader("Origin");
        String redirectBase;

        if (StringUtils.hasText(origin)) {
            // 브라우저가 보낸 Origin이 화이트리스트에 있는지 검증
            List<String> allowed = oAuthProperties.getAllowedOrigins();
            if (allowed != null && !allowed.isEmpty() && !allowed.contains(origin)) {
                log.warn("허용되지 않은 Origin: {}", origin);
                throw new UserExceptionHandler(ErrorCode.KAKAO_AUTH_CODE_INVALID);
            }
            redirectBase = origin;
        } else {
            // Origin 헤더가 없으면 현재 요청의 서버 기준으로 복원
            String scheme = java.util.Objects.toString(request.getHeader("X-Forwarded-Proto"), request.getScheme());
            String forwardedHost = request.getHeader("X-Forwarded-Host");
            if (StringUtils.hasText(forwardedHost)) {
                redirectBase = scheme + "://" + forwardedHost;
            } else {
                String host = request.getServerName();
                int port = request.getServerPort();
                boolean isDefault = ("http".equalsIgnoreCase(scheme) && port == 80) || ("https".equalsIgnoreCase(scheme) && port == 443);
                redirectBase = scheme + "://" + host + (isDefault ? "" : ":" + port);
            }
        }

        String redirectUri = redirectBase + "/oauth/kakao/callback";

        String encodedRedirect = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
        String encodedScope = URLEncoder.encode(kaKaoConfig.getScope(), StandardCharsets.UTF_8);

        return String.format(
                "https://kauth.kakao.com/oauth/authorize?client_id=%s&redirect_uri=%s&response_type=code&scope=%s&prompt=consent",
                kaKaoConfig.getClientId(), encodedRedirect, encodedScope
        );
    }

    /**
     * 프론트에서 받은 code/state를 검증하고, state에 저장된 redirect_uri로 토큰 교환을 수행합니다.
     * 이후 사용자 저장/조회 및 JWT 발급까지 처리합니다.
     */
    public Boolean kakaoLoginNoState(String accessCode, HttpServletRequest request, HttpServletResponse response) {

        Boolean isNewUser = false;

        if (!StringUtils.hasText(accessCode)) {
            throw new UserExceptionHandler(ErrorCode.KAKAO_AUTH_CODE_INVALID);
        }

        // 동적 콜백 redirect_uri를 재구성 (인가요청에서 사용한 값과 일치하도록)
        String origin = request.getHeader("Origin");
        String redirectBase;
        if (StringUtils.hasText(origin)) {
            List<String> allowed = oAuthProperties.getAllowedOrigins();
            if (allowed != null && !allowed.isEmpty() && !allowed.contains(origin)) {
                throw new UserExceptionHandler(ErrorCode.KAKAO_AUTH_CODE_INVALID);
            }
            redirectBase = origin;
        } else {
            String scheme = java.util.Objects.toString(request.getHeader("X-Forwarded-Proto"), request.getScheme());
            String forwardedHost = request.getHeader("X-Forwarded-Host");
            if (StringUtils.hasText(forwardedHost)) {
                redirectBase = scheme + "://" + forwardedHost;
            } else {
                String host = request.getServerName();
                int port = request.getServerPort();
                boolean isDefault = ("http".equalsIgnoreCase(scheme) && port == 80) || ("https".equalsIgnoreCase(scheme) && port == 443);
                redirectBase = scheme + "://" + host + (isDefault ? "" : ":" + port);
            }
        }
        String redirectUri = redirectBase + "/oauth/kakao/callback";

        // 액세스 토큰 발급
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

        // 사용자 정보 조회
        KaKaoUserInfoResponse userInfo;
        try {
            userInfo = kaKaoUserInfoClient.getUserInfo(
                    "Bearer " + authorizationCode.getAccess_token());
        } catch (FeignException e) {
            throw new UserExceptionHandler(ErrorCode.KAKAO_ACCESSTOKEN_INVALID);
        }

        // 핵심 정보 파싱/검증
        Long kakaoId = userInfo.getId();
        String email = (userInfo.getKakaoAccount() != null) ? userInfo.getKakaoAccount().getEmail() : null;
        String nickname = null;

        if (userInfo.getKakaoAccount() != null && userInfo.getKakaoAccount().getProfile() != null) {
            nickname = userInfo.getKakaoAccount().getProfile().getNickname();
        }
        if (!StringUtils.hasText(nickname) && userInfo.getProperties() != null) {
            nickname = userInfo.getProperties().getNickname();
        }
        String profileImageUrl = (userInfo.getProperties() != null) ? userInfo.getProperties().getProfileImage() : null;

        if (!StringUtils.hasText(email)) {
            log.warn("카카오 로그인 실패: 이메일 동의가 없어 회원 이메일을 확인할 수 없습니다");
            throw new UserExceptionHandler(ErrorCode.KAKAO_AUTH_CODE_INVALID);
        }

        // 신규 회원 생성
        Boolean userExist = userRepository.existsByEmail(email);

        if (userExist == Boolean.FALSE) {

            String profileImageKey = "kakao:" + (kakaoId != null ? kakaoId : java.util.UUID.randomUUID());

            Member newMember = Member.join(nickname, email, profileImageUrl, profileImageKey, Role.ROLE_AUTHOR);

            userRepository.save(newMember);

            isNewUser = true;
        }

        // 그리고 회원 정보를 기반으로 액세스토큰을 발급하여 헤더에 넣습니다
        Member byEmail = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserExceptionHandler(ErrorCode.MEMBER_NOT_FOUND));

        String access = jwtUtil.createJwt("access", byEmail.getId(), byEmail.getRole().toString(), jwtConfig.getAccessTokenValidityInSeconds());
        String refresh = jwtUtil.createJwt("refresh", byEmail.getId(), byEmail.getRole().toString(), jwtConfig.getRefreshTokenValidityInSeconds());

        refreshTokenRepository.saveRefreshToken(byEmail.getId(), refresh, jwtConfig.getRefreshTokenValidityInSeconds());

        response.setHeader("access-token", access);

        CookieUtil.addSameSiteCookie(
                response,
                "refresh-token",
                refresh,
                jwtConfig.getRefreshTokenValidityInSeconds().intValue(),
                cookieConfig.getDomain(),
                cookieConfig.isSecure(),
                cookieConfig.getSameSite()
        );

        return isNewUser;
    }




    /**
     * 항상 서버 콜백 방식 - 시작 단계 (yml redirect_uri 사용)
     * - yml에 설정된 redirect_uri를 그대로 사용합니다.
     */
    public String requestRedirectServer(HttpServletRequest request) {
        String redirectUri = kaKaoConfig.getRedirectUri();

        String encodedRedirect = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
        String encodedScope = URLEncoder.encode(kaKaoConfig.getScope(), StandardCharsets.UTF_8);

        return String.format(
                "https://kauth.kakao.com/oauth/authorize?client_id=%s&redirect_uri=%s&response_type=code&scope=%s&prompt=consent",
                kaKaoConfig.getClientId(), encodedRedirect, encodedScope
        );
    }

    /**
     * 항상 서버 콜백 방식 - 카카오로부터 서버 콜백을 받을 때 호출 (yml redirect_uri 사용)
     * - 설정된 redirect_uri로 토큰 교환을 수행합니다.
     */
    public Boolean kakaoLoginServer(String accessCode, HttpServletRequest request, HttpServletResponse response) {
        Boolean isNewUser = false;

        if (!StringUtils.hasText(accessCode)) {
            throw new UserExceptionHandler(ErrorCode.KAKAO_AUTH_CODE_INVALID);
        }

        String redirectUri = kaKaoConfig.getRedirectUri();

        KaKaoOAuthTokenDTO authorizationCode;
        try {
            log.info("액세스 토큰 발급을 시작합니다(서버 콜백)");
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
        try {
            userInfo = kaKaoUserInfoClient.getUserInfo(
                    "Bearer " + authorizationCode.getAccess_token());
        } catch (FeignException e) {
            throw new UserExceptionHandler(ErrorCode.KAKAO_ACCESSTOKEN_INVALID);
        }

        Long kakaoId = userInfo.getId();
        String email = (userInfo.getKakaoAccount() != null) ? userInfo.getKakaoAccount().getEmail() : null;
        String nickname = null;
        if (userInfo.getKakaoAccount() != null && userInfo.getKakaoAccount().getProfile() != null) {
            nickname = userInfo.getKakaoAccount().getProfile().getNickname();
        }
        if (!StringUtils.hasText(nickname) && userInfo.getProperties() != null) {
            nickname = userInfo.getProperties().getNickname();
        }
        String profileImageUrl = (userInfo.getProperties() != null) ? userInfo.getProperties().getProfileImage() : null;

        if (!StringUtils.hasText(email)) {
            log.warn("카카오 로그인 실패: 이메일 동의가 없어 회원 이메일을 확인할 수 없습니다");
            throw new UserExceptionHandler(ErrorCode.KAKAO_AUTH_CODE_INVALID);
        }

        Boolean userExist = userRepository.existsByEmail(email);
        if (userExist == Boolean.FALSE) {
            String profileImageKey = "kakao:" + (kakaoId != null ? kakaoId : java.util.UUID.randomUUID());

            Member newMember = Member.join(nickname, email, profileImageUrl, profileImageKey, Role.ROLE_AUTHOR);

            userRepository.save(newMember);
            isNewUser = true;
        }

        Member byEmail = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserExceptionHandler(ErrorCode.MEMBER_NOT_FOUND));

        String access = jwtUtil.createJwt("access", byEmail.getId(), byEmail.getRole().toString(), jwtConfig.getAccessTokenValidityInSeconds());
        String refresh = jwtUtil.createJwt("refresh", byEmail.getId(), byEmail.getRole().toString(), jwtConfig.getRefreshTokenValidityInSeconds());

        refreshTokenRepository.saveRefreshToken(byEmail.getId(), refresh, jwtConfig.getRefreshTokenValidityInSeconds());

        response.setHeader("access-token", access);
        CookieUtil.addSameSiteCookie(
                response,
                "refresh-token",
                refresh,
                jwtConfig.getRefreshTokenValidityInSeconds().intValue(),
                cookieConfig.getDomain(),
                cookieConfig.isSecure(),
                cookieConfig.getSameSite()
        );

        return isNewUser;
    }
}
