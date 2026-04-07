package or.hyu.ssd.global.jwt.service;

import jakarta.servlet.http.HttpServletResponse;
import or.hyu.ssd.domain.member.repository.MemberRepository;
import or.hyu.ssd.domain.member.valid.RefreshTokenValidator;
import or.hyu.ssd.global.config.properties.CookieConfig;
import or.hyu.ssd.global.config.properties.JWTConfig;
import or.hyu.ssd.global.jwt.JWTUtil;
import or.hyu.ssd.global.jwt.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
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

    private JWTConfig jwtConfig;
    private CookieConfig cookieConfig;
    private JWTService jwtService;

    @BeforeEach
    void setUp() {
        jwtConfig = new JWTConfig();
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
                cookieConfig,
                refreshTokenValidator
        );
    }

    @Test
    @DisplayName("logout()은 Redis refresh token을 삭제하고 refresh-token 쿠키를 만료시킨다")
    void logout_deletesRefreshTokenAndExpiresCookie() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtService.logout(1L, response);

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
