package or.hyu.ssd.domain.member.service;

import feign.Request;
import feign.Response;
import jakarta.servlet.http.HttpServletResponse;
import or.hyu.ssd.domain.member.client.KaKaoOAuthClient;
import or.hyu.ssd.domain.member.client.KaKaoUserInfoClient;
import or.hyu.ssd.domain.member.controller.dto.kakao.KaKaoOAuthTokenDTO;
import or.hyu.ssd.domain.member.controller.dto.kakao.KaKaoUserInfoResponse;
import or.hyu.ssd.domain.member.entity.Member;
import or.hyu.ssd.domain.member.entity.Role;
import or.hyu.ssd.domain.member.repository.MemberRepository;
import or.hyu.ssd.global.api.handler.UserExceptionHandler;
import or.hyu.ssd.global.config.KaKaoConfig;
import or.hyu.ssd.global.config.properties.CookieConfig;
import or.hyu.ssd.global.config.properties.JWTConfig;
import or.hyu.ssd.global.config.properties.OAuthProperties;
import or.hyu.ssd.global.jwt.JWTUtil;
import or.hyu.ssd.global.jwt.repository.RefreshTokenRepository;
import or.hyu.ssd.global.oauth.repository.OAuthRedirectStateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OAuthServiceTest {

    @Mock
    private KaKaoOAuthClient kaKaoOAuthClient;
    @Mock
    private KaKaoUserInfoClient kaKaoUserInfoClient;
    @Mock
    private MemberRepository userRepository;
    @Mock
    private JWTUtil jwtUtil;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private OAuthRedirectStateRepository oAuthRedirectStateRepository;

    private JWTConfig jwtConfig;
    private KaKaoConfig kaKaoConfig;
    private CookieConfig cookieConfig;
    private OAuthProperties oAuthProperties;

    private OAuthService oAuthService;

    @BeforeEach
    void setUp() {
        jwtConfig = new JWTConfig();
        jwtConfig.setAccessTokenValidityInSeconds(3600L);
        jwtConfig.setRefreshTokenValidityInSeconds(2592000L);

        kaKaoConfig = new KaKaoConfig();
        ReflectionTestUtils.setField(kaKaoConfig, "clientId", "kakao-client-id");
        ReflectionTestUtils.setField(kaKaoConfig, "scope", "profile_nickname,account_email");
        ReflectionTestUtils.setField(kaKaoConfig, "redirectUri", "https://api.example.com/oauth/kakao/server/callback");

        cookieConfig = new CookieConfig();
        ReflectionTestUtils.setField(cookieConfig, "domain", "example.com");
        ReflectionTestUtils.setField(cookieConfig, "secure", true);
        ReflectionTestUtils.setField(cookieConfig, "sameSite", "None");

        oAuthProperties = new OAuthProperties();
        oAuthProperties.setAllowedOrigins(List.of(
                "https://client.example.com",
                "https://dev-api.simsaimdang.shop",
                "https://dev.simsaimdang.shop"
        ));
        oAuthProperties.setRedirectStateTtlSeconds(300L);

        oAuthService = new OAuthService(
                kaKaoOAuthClient,
                kaKaoUserInfoClient,
                userRepository,
                jwtUtil,
                jwtConfig,
                refreshTokenRepository,
                oAuthRedirectStateRepository,
                kaKaoConfig,
                cookieConfig,
                oAuthProperties
        );
    }

    @Test
    @DisplayName("requestRedirectToFixedRedirect()는 Origin 기준 /redirect를 state와 저장하고 카카오 authorize URL을 반환한다")
    void requestRedirectToFixedRedirect_savesStateAndBuildsAuthorizeUrl() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme("https");
        request.setServerName("api.example.com");
        request.setServerPort(443);
        request.addHeader("Origin", "https://client.example.com");
        request.addHeader("X-Forwarded-Proto", "https");
        request.addHeader("X-Forwarded-Host", "dev-api.simsaimdang.shop");

        String authorizeUrl = oAuthService.requestRedirectToFixedRedirect(request);

        ArgumentCaptor<String> stateCaptor = ArgumentCaptor.forClass(String.class);
        verify(oAuthRedirectStateRepository).save(stateCaptor.capture(), eq("https://client.example.com/redirect"), eq(300L));

        assertThat(authorizeUrl)
                .contains("https://kauth.kakao.com/oauth/authorize")
                .contains("client_id=kakao-client-id")
                .contains("redirect_uri=https%3A%2F%2Fdev-api.simsaimdang.shop%2Foauth%2Fkakao%2Fcallback")
                .contains("state=" + stateCaptor.getValue());
    }

    @Test
    @DisplayName("requestRedirectToFixedRedirect()는 https 요청에서 잘못 보고된 80 포트를 callback URL에 붙이지 않는다")
    void requestRedirectToFixedRedirect_dropsPort80ForHttpsCallback() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme("http");
        request.setServerName("dev-api.simsaimdang.shop");
        request.setServerPort(80);
        request.addHeader("Origin", "https://dev.simsaimdang.shop");
        request.addHeader("Host", "dev-api.simsaimdang.shop");
        request.addHeader("X-Forwarded-Proto", "https");

        String authorizeUrl = oAuthService.requestRedirectToFixedRedirect(request);

        assertThat(authorizeUrl)
                .contains("redirect_uri=https%3A%2F%2Fdev-api.simsaimdang.shop%2Foauth%2Fkakao%2Fcallback")
                .doesNotContain("%3A80%2Foauth%2Fkakao%2Fcallback");
    }

    @Test
    @DisplayName("requestRedirectToFixedRedirect()는 허용되지 않은 Origin을 거부한다")
    void requestRedirectToFixedRedirect_rejectsDisallowedOrigin() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme("https");
        request.setServerName("dev-api.simsaimdang.shop");
        request.setServerPort(443);
        request.addHeader("Origin", "https://evil.example.com");

        assertThatThrownBy(() -> oAuthService.requestRedirectToFixedRedirect(request))
                .isInstanceOf(UserExceptionHandler.class)
                .hasMessage("허용되지 않은 Origin 입니다");
    }

    @Test
    @DisplayName("kakaoLoginAndRedirect()는 토큰을 발급하고 최종 redirect 주소의 fragment로 access token을 전달한다")
    void kakaoLoginAndRedirect_setsCookieAndRedirectsToClient() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme("https");
        request.setServerName("dev-api.simsaimdang.shop");
        request.setServerPort(443);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(oAuthRedirectStateRepository.consume("state-1"))
                .thenReturn(Optional.of("https://client.example.com/redirect"));
        when(kaKaoOAuthClient.getToken(
                eq("authorization_code"),
                eq("kakao-client-id"),
                eq("https://dev-api.simsaimdang.shop/oauth/kakao/callback"),
                eq("auth-code")
        )).thenReturn(kakaoToken("kakao-access-token"));
        when(kaKaoUserInfoClient.getUserInfo("Bearer kakao-access-token"))
                .thenReturn(kakaoUserInfo("tester@example.com", "테스터"));
        when(userRepository.existsByEmail("tester@example.com")).thenReturn(true);
        when(userRepository.findByEmail("tester@example.com")).thenReturn(Optional.of(member(1L)));
        when(jwtUtil.createJwt(eq("access"), eq(1L), eq(Role.ROLE_AUTHOR.toString()), anyLong())).thenReturn("access-jwt");
        when(jwtUtil.createJwt(eq("refresh"), eq(1L), eq(Role.ROLE_AUTHOR.toString()), anyLong())).thenReturn("refresh-jwt");

        oAuthService.kakaoLoginAndRedirect("auth-code", "state-1", request, response);

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FOUND);
        assertThat(response.getHeader("access-token")).isEqualTo("access-jwt");
        assertThat(response.getHeader("Location"))
                .isEqualTo("https://client.example.com/redirect#accessToken=access-jwt&isNewUser=false");
        assertThat(response.getHeader("Set-Cookie"))
                .contains("refresh-token=refresh-jwt")
                .contains("Domain=example.com")
                .contains("SameSite=None")
                .contains("Secure")
                .contains("HttpOnly");

        verify(refreshTokenRepository).saveRefreshToken(1L, "refresh-jwt", 2592000L);
    }

    @Test
    @DisplayName("kakaoLoginAndRedirect()는 https 서버 콜백에서 80 포트를 제거한 redirect_uri로 토큰 교환한다")
    void kakaoLoginAndRedirect_usesNormalizedHttpsCallbackUri() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme("http");
        request.setServerName("dev-api.simsaimdang.shop");
        request.setServerPort(80);
        request.addHeader("Host", "dev-api.simsaimdang.shop");
        request.addHeader("X-Forwarded-Proto", "https");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(oAuthRedirectStateRepository.consume("state-2"))
                .thenReturn(Optional.of("https://client.example.com/redirect"));
        when(kaKaoOAuthClient.getToken(
                eq("authorization_code"),
                eq("kakao-client-id"),
                eq("https://dev-api.simsaimdang.shop/oauth/kakao/callback"),
                eq("auth-code")
        )).thenReturn(kakaoToken("kakao-access-token"));
        when(kaKaoUserInfoClient.getUserInfo("Bearer kakao-access-token"))
                .thenReturn(kakaoUserInfo("tester@example.com", "테스터"));
        when(userRepository.existsByEmail("tester@example.com")).thenReturn(true);
        when(userRepository.findByEmail("tester@example.com")).thenReturn(Optional.of(member(1L)));
        when(jwtUtil.createJwt(eq("access"), eq(1L), eq(Role.ROLE_AUTHOR.toString()), anyLong())).thenReturn("access-jwt");
        when(jwtUtil.createJwt(eq("refresh"), eq(1L), eq(Role.ROLE_AUTHOR.toString()), anyLong())).thenReturn("refresh-jwt");

        oAuthService.kakaoLoginAndRedirect("auth-code", "state-2", request, response);

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FOUND);
        verify(refreshTokenRepository).saveRefreshToken(1L, "refresh-jwt", 2592000L);
    }

    @Test
    @DisplayName("kakaoLoginAndRedirect()는 이메일 누락 시 카카오 raw user/me 응답을 추가 조회한다")
    void kakaoLoginAndRedirect_fetchesRawUserInfoWhenEmailMissing() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme("https");
        request.setServerName("dev-api.simsaimdang.shop");
        request.setServerPort(443);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(oAuthRedirectStateRepository.consume("state-3"))
                .thenReturn(Optional.of("https://client.example.com/redirect"));
        when(kaKaoOAuthClient.getToken(
                eq("authorization_code"),
                eq("kakao-client-id"),
                eq("https://dev-api.simsaimdang.shop/oauth/kakao/callback"),
                eq("auth-code")
        )).thenReturn(kakaoToken("kakao-access-token"));
        when(kaKaoUserInfoClient.getUserInfo("Bearer kakao-access-token"))
                .thenReturn(kakaoUserInfo(null, "테스터"));
        when(kaKaoUserInfoClient.getUserInfoRaw("Bearer kakao-access-token"))
                .thenReturn(rawUserInfoResponse("{\"id\":77,\"properties\":{\"nickname\":\"테스터\"}}"));

        assertThatThrownBy(() -> oAuthService.kakaoLoginAndRedirect("auth-code", "state-3", request, response))
                .isInstanceOf(UserExceptionHandler.class);

        verify(kaKaoUserInfoClient).getUserInfoRaw("Bearer kakao-access-token");
    }

    private KaKaoOAuthTokenDTO kakaoToken(String accessToken) {
        KaKaoOAuthTokenDTO dto = new KaKaoOAuthTokenDTO();
        dto.setAccess_token(accessToken);
        return dto;
    }

    private KaKaoUserInfoResponse kakaoUserInfo(String email, String nickname) {
        KaKaoUserInfoResponse response = new KaKaoUserInfoResponse();
        response.setId(77L);

        KaKaoUserInfoResponse.KakaoAccount.Profile profile = new KaKaoUserInfoResponse.KakaoAccount.Profile();
        profile.setNickname(nickname);

        KaKaoUserInfoResponse.KakaoAccount account = new KaKaoUserInfoResponse.KakaoAccount();
        account.setEmail(email);
        account.setProfile(profile);
        response.setKakaoAccount(account);

        return response;
    }

    private Member member(Long id) {
        return Member.builder()
                .id(id)
                .name("테스터")
                .email("tester@example.com")
                .profileImageUrl("")
                .profileImageKey("kakao:77")
                .role(Role.ROLE_AUTHOR)
                .build();
    }

    private Response rawUserInfoResponse(String body) {
        return Response.builder()
                .status(200)
                .reason("OK")
                .request(Request.create(
                        Request.HttpMethod.POST,
                        "https://kapi.kakao.com/v2/user/me",
                        Map.of(),
                        null,
                        StandardCharsets.UTF_8,
                        null
                ))
                .headers(Map.of())
                .body(body, StandardCharsets.UTF_8)
                .build();
    }
}
