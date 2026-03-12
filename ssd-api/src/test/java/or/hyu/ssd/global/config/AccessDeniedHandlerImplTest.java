package or.hyu.ssd.global.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import or.hyu.ssd.global.api.ErrorCode;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class AccessDeniedHandlerImplTest {

    @Test
    @DisplayName("인가되지 않은 요청은 403과 권한 없음 메시지를 반환한다")
    void handle_accessDenied_returnsForbidden() throws IOException, jakarta.servlet.ServletException {
        AccessDeniedHandlerImpl handler = new AccessDeniedHandlerImpl();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(request, response, new AccessDeniedException("forbidden"));

        assertThat(response.getStatus()).isEqualTo(ErrorCode.REQUEST_ACCESS_DENIED.getStatus().value());
        assertThat(response.getContentAsString()).contains(ErrorCode.REQUEST_ACCESS_DENIED.getCode());
        assertThat(response.getContentAsString()).contains(ErrorCode.REQUEST_ACCESS_DENIED.getMessage());
    }
}
