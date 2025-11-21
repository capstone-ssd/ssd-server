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
import or.hyu.ssd.global.jwt.repository.OAuthStateRepository;
import or.hyu.ssd.global.api.ErrorCode;
import or.hyu.ssd.global.api.handler.UserExceptionHandler;
import or.hyu.ssd.global.config.properties.CookieConfig;
import or.hyu.ssd.global.config.properties.JWTConfig;
import or.hyu.ssd.global.config.properties.OAuthProperties;
import or.hyu.ssd.global.config.KaKaoConfig;
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
    private final OAuthStateRepository oAuthStateRepository;

    private final KaKaoConfig kaKaoConfig;
    private final CookieConfig cookieConfig;
    private final OAuthProperties oAuthProperties;

    // state TTL(초) — 5분
    private static final long STATE_TTL_SECONDS = 300L;



    /**
     * 동적 리다이렉트: 요청 Origin을 기반으로 redirect_uri를 계산하고 state를 발급/저장합니다.
     * 반환값은 카카오 authorize URL입니다.
     */
    public String requestRedirect(jakarta.servlet.http.HttpServletRequest request) {
        String origin = request.getHeader("Origin");
        String redirectBase;

        if (StringUtils.hasText(origin)) {

            // 브라우저가 Cross-Origin 요청으로 보낸 경우: Origin 헤더 기반으로 검증/사용
            List<String> allowed = oAuthProperties.getAllowedOrigins();

            if (allowed != null && !allowed.isEmpty() && !allowed.contains(origin)) {
                log.warn("허용되지 않은 Origin: {}", origin);
                throw new UserExceptionHandler(ErrorCode.KAKAO_AUTH_CODE_INVALID);
            }

            redirectBase = origin;
        } else {

            /**
             * Origin 헤더가 없는 경우(주소창 직접 입력, 같은 오리진 요청 등):
             * 현재 요청의 스킴/호스트/포트를 기반으로 베이스 URL을 계산합니다.
             * */
            String scheme = java.util.Objects.toString(request.getHeader("X-Forwarded-Proto"), request.getScheme());
            String forwardedHost = request.getHeader("X-Forwarded-Host");

            if (StringUtils.hasText(forwardedHost)) {
                
                // X-Forwarded-Host에 host 또는 host:port가 포함됨
                redirectBase = scheme + "://" + forwardedHost;
            } else {
                String host = request.getServerName();
                int port = request.getServerPort();
                boolean isDefault = ("http".equalsIgnoreCase(scheme) && port == 80) ||
                        ("https".equalsIgnoreCase(scheme) && port == 443);
                redirectBase = scheme + "://" + host + (isDefault ? "" : ":" + port);
            }
        }

        String redirectUri = redirectBase + "/oauth/kakao/callback";

        // CSRF/재사용 방지용 state 발급/저장(1회성)
        String state = java.util.UUID.randomUUID().toString();
        oAuthStateRepository.store(state, redirectUri, STATE_TTL_SECONDS);

        String encodedRedirect = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
        String encodedScope = URLEncoder.encode(kaKaoConfig.getScope(), StandardCharsets.UTF_8);

        return String.format(
                "https://kauth.kakao.com/oauth/authorize?client_id=%s&redirect_uri=%s&response_type=code&scope=%s&prompt=consent&state=%s",
                kaKaoConfig.getClientId(), encodedRedirect, encodedScope, state
        );
    }

    /**
     * 프론트에서 받은 code/state를 검증하고, state에 저장된 redirect_uri로 토큰 교환을 수행합니다.
     * 이후 사용자 저장/조회 및 JWT 발급까지 처리합니다.
     */
    public Boolean kakaoLoginWithState(String accessCode, String state, HttpServletRequest request, HttpServletResponse response) {

        Boolean isNewUser = false;

        if (!StringUtils.hasText(accessCode) || !StringUtils.hasText(state)) {
            throw new UserExceptionHandler(ErrorCode.KAKAO_AUTH_CODE_INVALID);
        }

        // state 검증 및 redirect_uri 복원(1회성 소비)
        String redirectUri = oAuthStateRepository.consume(state);
        if (!StringUtils.hasText(redirectUri)) {
            throw new UserExceptionHandler(ErrorCode.OAUTH_STATE_INVALID);
        }

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

            Member newMember = Member.builder()
                    .name(StringUtils.hasText(nickname) ? nickname : email)
                    .email(email)
                    .profileImageUrl(StringUtils.hasText(profileImageUrl) ? profileImageUrl : "")
                    .profileImageKey(profileImageKey)
                    .role(Role.ROLE_AUTHOR)
                    .build();

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
     * 항상 서버 콜백 방식 - 시작 단계
     * - Origin 유무와 무관하게 서버의 스킴/호스트/포트를 기준으로 redirect_uri를 계산합니다.
     * - 계산된 redirect_uri는 "/oauth/kakao/server/callback"으로 고정됩니다.
     * - 발급한 state와 redirect_uri를 1회성으로 저장 후, 카카오 authorize URL을 반환합니다.
     */
    public String requestRedirectServer(HttpServletRequest request) {
        String scheme = java.util.Objects.toString(request.getHeader("X-Forwarded-Proto"), request.getScheme());
        String forwardedHost = request.getHeader("X-Forwarded-Host");

        String redirectBase;
        if (StringUtils.hasText(forwardedHost)) {
            redirectBase = scheme + "://" + forwardedHost;
        } else {
            String host = request.getServerName();
            int port = request.getServerPort();
            boolean isDefault = ("http".equalsIgnoreCase(scheme) && port == 80) ||
                    ("https".equalsIgnoreCase(scheme) && port == 443);
            redirectBase = scheme + "://" + host + (isDefault ? "" : ":" + port);
        }

        // 서버 콜백 고정 경로
        String redirectUri = redirectBase + "/oauth/kakao/server/callback";

        String state = java.util.UUID.randomUUID().toString();
        oAuthStateRepository.store(state, redirectUri, STATE_TTL_SECONDS);

        String encodedRedirect = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
        String encodedScope = URLEncoder.encode(kaKaoConfig.getScope(), StandardCharsets.UTF_8);

        return String.format(
                "https://kauth.kakao.com/oauth/authorize?client_id=%s&redirect_uri=%s&response_type=code&scope=%s&prompt=consent&state=%s",
                kaKaoConfig.getClientId(), encodedRedirect, encodedScope, state
        );
    }

    /**
     * 항상 서버 콜백 방식 - 카카오로부터 서버 콜백을 받을 때 호출
     * - state로 저장된 redirect_uri를 복원하여 토큰 교환을 수행합니다.
     * - 이후 사용자 저장/조회 및 JWT 발급까지 처리합니다.
     */
    public Boolean kakaoLoginServer(String accessCode, String state, HttpServletRequest request, HttpServletResponse response) {
        Boolean isNewUser = false;

        if (!StringUtils.hasText(accessCode) || !StringUtils.hasText(state)) {
            throw new UserExceptionHandler(ErrorCode.KAKAO_AUTH_CODE_INVALID);
        }

        String redirectUri = oAuthStateRepository.consume(state);
        if (!StringUtils.hasText(redirectUri)) {
            throw new UserExceptionHandler(ErrorCode.OAUTH_STATE_INVALID);
        }

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

            Member newMember = Member.builder()
                    .name(StringUtils.hasText(nickname) ? nickname : email)
                    .email(email)
                    .profileImageUrl(StringUtils.hasText(profileImageUrl) ? profileImageUrl : "")
                    .profileImageKey(profileImageKey)
                    .role(Role.ROLE_AUTHOR)
                    .build();
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
