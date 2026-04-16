package or.hyu.ssd.api.security;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import or.hyu.ssd.global.api.ErrorCode;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.InsufficientAuthenticationException;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class AuthenticationEntryPointImplTest {

    @Test
    @DisplayName("인증되지 않은 요청은 401과 액세스 토큰 필요 메시지를 반환한다")
    void commence_unauthenticatedRequest_returnsUnauthorized() throws ServletException, IOException {
        // given
        AuthenticationEntryPointImpl entryPoint = new AuthenticationEntryPointImpl();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        entryPoint.commence(
                request,
                response,
                new InsufficientAuthenticationException("인증 필요")
        );

        // then
        assertThat(response.getStatus()).isEqualTo(ErrorCode.ACCESS_TOKEN_REQUIRED.getStatus().value());
        assertThat(response.getContentAsString()).contains(ErrorCode.ACCESS_TOKEN_REQUIRED.getCode());
        assertThat(response.getContentAsString()).contains(ErrorCode.ACCESS_TOKEN_REQUIRED.getMessage());
    }
}
