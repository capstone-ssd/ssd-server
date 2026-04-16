package or.hyu.ssd.auth.jwt.service;

import or.hyu.ssd.member.repository.MemberRepository;
import or.hyu.ssd.auth.jwt.support.RefreshTokenValidator;
import or.hyu.ssd.auth.jwt.property.CookieConfig;
import or.hyu.ssd.auth.jwt.property.JWTConfig;
import or.hyu.ssd.auth.jwt.support.JWTUtil;
import or.hyu.ssd.auth.jwt.repository.AccessTokenBlacklistRepository;
import or.hyu.ssd.auth.jwt.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JWTServiceTest {

    @Mock
    private JWTUtil jwtUtil;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private RefreshTokenValidator refreshTokenValidator;
    @Mock
    private AccessTokenBlacklistRepository accessTokenBlacklistRepository;

    private JWTConfig jwtConfig;
    private CookieConfig cookieConfig;
    private JWTService jwtService;

    @BeforeEach
    void setUp() {
        jwtConfig = new JWTConfig();
        jwtConfig.setHeader("Authorization");
        jwtConfig.setAccessTokenValidityInSeconds(3600L);
        jwtConfig.setRefreshTokenValidityInSeconds(2592000L);

        cookieConfig = new CookieConfig();
        ReflectionTestUtils.setField(cookieConfig, "domain", "example.com");
        ReflectionTestUtils.setField(cookieConfig, "secure", true);
        ReflectionTestUtils.setField(cookieConfig, "sameSite", "None");

        jwtService = new JWTService(
                jwtUtil,
                jwtConfig,
                memberRepository,
                refreshTokenRepository,
                accessTokenBlacklistRepository,
                cookieConfig,
                refreshTokenValidator
        );
    }

    @Test
    @DisplayName("logout()은 access token을 블랙리스트에 등록하고 refresh token을 삭제한 뒤 refresh-token 쿠키를 만료시킨다")
    void logout_blacklistsAccessTokenAndExpiresCookie() {
        // given
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(jwtUtil.getRemainingExpiration("valid-access-token")).thenReturn(1800L);
        when(jwtUtil.getJti("valid-access-token")).thenReturn("access-jti");

        // when
        jwtService.logout(1L, "valid-access-token", response);

        // then
        verify(accessTokenBlacklistRepository).blacklist("access-jti", 1800L);
        verify(refreshTokenRepository).deleteById(1L);
        assertThat(response.getHeader("Set-Cookie"))
                .contains("refresh-token=")
                .contains("Max-Age=0")
                .contains("Domain=example.com")
                .contains("SameSite=None")
                .contains("Secure")
                .contains("HttpOnly");
    }
}
