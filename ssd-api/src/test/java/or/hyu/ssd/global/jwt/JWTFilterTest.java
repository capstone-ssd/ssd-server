package or.hyu.ssd.global.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import or.hyu.ssd.domain.member.service.CustomUserDetailsService;
import or.hyu.ssd.global.api.ErrorCode;
import or.hyu.ssd.global.api.handler.UserExceptionHandler;
import or.hyu.ssd.global.config.properties.JWTConfig;
import or.hyu.ssd.global.jwt.repository.AccessTokenBlacklistRepository;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockFilterChain;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JWTFilterTest {

    @Mock
    private JWTUtil jwtUtil;

    @Mock
    private JWTConfig jwtConfig;

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private AccessTokenBlacklistRepository accessTokenBlacklistRepository;

    @Test
    @DisplayName("Bearer 스킴이 아닌 Authorization 헤더는 401과 상세 메시지를 반환한다")
    void doFilterInternal_invalidAuthorizationHeader_returnsUnauthorized() throws ServletException, IOException {
        JWTFilter jwtFilter = new JWTFilter(jwtUtil, jwtConfig, customUserDetailsService, accessTokenBlacklistRepository);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        when(jwtConfig.getHeader()).thenReturn("Authorization");
        request.addHeader("Authorization", "Token invalid");

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(ErrorCode.ACCESS_INVALID_TYPE.getStatus().value());
        assertThat(response.getContentAsString()).contains(ErrorCode.ACCESS_INVALID_TYPE.getCode());
        assertThat(response.getContentAsString()).contains(ErrorCode.ACCESS_INVALID_TYPE.getMessage());
    }

    @Test
    @DisplayName("만료된 액세스 토큰은 401과 만료 메시지를 반환한다")
    void doFilterInternal_expiredAccessToken_returnsUnauthorized() throws ServletException, IOException {
        JWTFilter jwtFilter = new JWTFilter(jwtUtil, jwtConfig, customUserDetailsService, accessTokenBlacklistRepository);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        when(jwtConfig.getHeader()).thenReturn("Authorization");
        request.addHeader("Authorization", "Bearer expired-token");
        when(jwtUtil.isExpired("expired-token")).thenThrow(new ExpiredJwtException(null, null, "expired"));

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(ErrorCode.ACCESS_TOKEN_EXPIRED.getStatus().value());
        assertThat(response.getContentAsString()).contains(ErrorCode.ACCESS_TOKEN_EXPIRED.getCode());
        assertThat(response.getContentAsString()).contains(ErrorCode.ACCESS_TOKEN_EXPIRED.getMessage());
    }

    @Test
    @DisplayName("토큰의 회원이 존재하지 않으면 401과 상세 메시지를 반환한다")
    void doFilterInternal_tokenMemberNotFound_returnsUnauthorized() throws ServletException, IOException {
        JWTFilter jwtFilter = new JWTFilter(jwtUtil, jwtConfig, customUserDetailsService, accessTokenBlacklistRepository);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        when(jwtConfig.getHeader()).thenReturn("Authorization");
        request.addHeader("Authorization", "Bearer valid-token");
        when(jwtUtil.isExpired("valid-token")).thenReturn(false);
        when(jwtUtil.getCategory("valid-token")).thenReturn("access");
        when(jwtUtil.getJti("valid-token")).thenReturn("test-jti");
        when(accessTokenBlacklistRepository.exists("test-jti")).thenReturn(false);
        when(jwtUtil.getId("valid-token")).thenReturn(1L);
        when(jwtUtil.getRole("valid-token")).thenReturn("ROLE_AUTHOR");
        when(customUserDetailsService.loadUserById(1L))
                .thenThrow(new UserExceptionHandler(ErrorCode.MEMBER_NOT_FOUND));

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(ErrorCode.TOKEN_MEMBER_NOT_FOUND.getStatus().value());
        assertThat(response.getContentAsString()).contains(ErrorCode.TOKEN_MEMBER_NOT_FOUND.getCode());
        assertThat(response.getContentAsString()).contains(ErrorCode.TOKEN_MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("블랙리스트에 등록된 액세스 토큰은 401과 상세 메시지를 반환한다")
    void doFilterInternal_blacklistedAccessToken_returnsUnauthorized() throws ServletException, IOException {
        JWTFilter jwtFilter = new JWTFilter(jwtUtil, jwtConfig, customUserDetailsService, accessTokenBlacklistRepository);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        when(jwtConfig.getHeader()).thenReturn("Authorization");
        request.addHeader("Authorization", "Bearer blacklisted-token");
        when(jwtUtil.isExpired("blacklisted-token")).thenReturn(false);
        when(jwtUtil.getCategory("blacklisted-token")).thenReturn("access");
        when(jwtUtil.getJti("blacklisted-token")).thenReturn("access-jti");
        when(accessTokenBlacklistRepository.exists("access-jti")).thenReturn(true);

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(ErrorCode.ACCESS_TOKEN_BLACKLISTED.getStatus().value());
        assertThat(response.getContentAsString()).contains(ErrorCode.ACCESS_TOKEN_BLACKLISTED.getCode());
        assertThat(response.getContentAsString()).contains(ErrorCode.ACCESS_TOKEN_BLACKLISTED.getMessage());
    }
}
