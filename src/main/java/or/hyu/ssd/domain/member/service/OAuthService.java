package or.hyu.ssd.domain.member.service;

import feign.FeignException;
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
import or.hyu.ssd.global.jwt.JWTUtil;
import or.hyu.ssd.global.util.CookieUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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


    /**
     * 카카오 인증 서버에 인가코드를 요청하는 메서드입니다
     * 실제 사용은 클라이언트 리다이렉트지만, 서버에서 유효성 점검을 위해 제공합니다.
     */
    public String requestRedirect() {
        String encodedRedirect = URLEncoder.encode(kaKaoConfig.getRedirectUri(), StandardCharsets.UTF_8);
        String encodedScope = URLEncoder.encode(kaKaoConfig.getScope(), StandardCharsets.UTF_8);

        return String.format(
                "https://kauth.kakao.com/oauth/authorize?client_id=%s&redirect_uri=%s&response_type=code&scope=%s&prompt=consent",
                kaKaoConfig.getClientId(), encodedRedirect, encodedScope
        );
    }


    public Boolean kakaoLogin(String accessCode, HttpServletResponse response) {

        // 신규회원인지 검증하는 필드
        Boolean isNewUser = false;

        // 인가코드가 비어있다면 예외발생
        if (!StringUtils.hasText(accessCode)) {
            throw new UserExceptionHandler(ErrorCode.KAKAO_AUTH_CODE_INVALID);
        }

        log.info("인가코드가 비어있지 않습니다");

        // 인가코드를 받고 그걸 통해서 인증 액세스 토큰을 발급받습니다
        KaKaoOAuthTokenDTO authorizationCode;
        try {
            log.info("액세스 토큰 발급을 시작합니다");
            authorizationCode = getKaKaoOAuthTokenDTO(accessCode);
        } catch (FeignException e) {
            log.info(e.getMessage());
            throw new UserExceptionHandler(ErrorCode.KAKAO_AUTH_CODE_INVALID);
        }

        // 그리고 액세스 토큰을 이용하여 회원 정보를 가져옵니다
        KaKaoUserInfoResponse userInfo;
        try {
            userInfo = kaKaoUserInfoClient.getUserInfo(
                    "Bearer " + authorizationCode.getAccess_token());
        } catch (FeignException e) {
            throw new UserExceptionHandler(ErrorCode.KAKAO_ACCESSTOKEN_INVALID);
        }

        // 사용자 핵심 정보 파싱 및 검증
        Long kakaoId = userInfo.getId();
        String email = (userInfo.getKakaoAccount() != null) ? userInfo.getKakaoAccount().getEmail() : null;
        String nickname = null;

        if (userInfo.getKakaoAccount() != null && userInfo.getKakaoAccount().getProfile() != null) {
            nickname = userInfo.getKakaoAccount().getProfile().getNickname();
        }
        if (!StringUtils.hasText(nickname) && userInfo.getProperties() != null) {
            nickname = userInfo.getProperties().getNickname();
        }
        String profileImageUrl = (userInfo.getProperties() != null) ? userInfo.getProperties().getProfileImage() : "";

        if (!StringUtils.hasText(email)) {
            log.warn("카카오 로그인 실패: 이메일 동의가 없어 회원 이메일을 확인할 수 없습니다");
            throw new UserExceptionHandler(ErrorCode.KAKAO_AUTH_CODE_INVALID);
        }

        // 만약 해당 이메일을 통해 회원가입된 회원이 존재하지 않는다면, 새로운 회원을 생성합니다
        Boolean userExist = userRepository.existsByEmail(email);

        // 현재 작성자로서 로그인만 가능한 형태임
        if (userExist == Boolean.FALSE) {

            String profileImageKey = "kakao:" + (kakaoId != null ? kakaoId : UUID.randomUUID());

            Member newMember = Member.builder()
                    .name(StringUtils.hasText(nickname) ? nickname : (StringUtils.hasText(email) ? email : "KakaoUser"))
                    .email(email)
                    .profileImageUrl(StringUtils.hasText(profileImageUrl) ? profileImageUrl : "프로필 이미지가 비어있습니다. 추후 폴백 이미지로 대체되어야 합니다")
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


    private KaKaoOAuthTokenDTO getKaKaoOAuthTokenDTO(String accessCode) {

        return kaKaoOAuthClient.getToken(
                "authorization_code",
                kaKaoConfig.getClientId(),
                kaKaoConfig.getRedirectUri(),
                accessCode
        );
    }
}

