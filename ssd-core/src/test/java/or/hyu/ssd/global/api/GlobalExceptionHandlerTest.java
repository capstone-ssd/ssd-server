package or.hyu.ssd.global.api;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import or.hyu.ssd.global.alert.ErrorAlertContext;
import or.hyu.ssd.global.alert.ErrorAlertNotifier;
import or.hyu.ssd.global.api.handler.UserExceptionHandler;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private ErrorAlertNotifier errorAlertNotifier;

    @Test
    @DisplayName("handleGeneralException()는 5xx CustomException에서 Discord 알림을 보낸다")
    void handleGeneralException_notifiesDiscordForServerErrors() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler(List.of(errorAlertNotifier));
        HttpServletRequest request = request("POST", "/api/v1/external-ai/evaluate");

        handler.handleGeneralException(new UserExceptionHandler(ErrorCode.EXTERNAL_AI_CALL_FAILED), request);

        ArgumentCaptor<ErrorAlertContext> contextCaptor = ArgumentCaptor.forClass(ErrorAlertContext.class);
        verify(errorAlertNotifier).notify(contextCaptor.capture());
        assertThat(contextCaptor.getValue().errorCode()).isEqualTo("AI50201");
        assertThat(contextCaptor.getValue().uri()).isEqualTo("/api/v1/external-ai/evaluate");
    }

    @Test
    @DisplayName("handleNotFound()는 4xx 예외에서 Discord 알림을 보내지 않는다")
    void handleNotFound_doesNotNotifyDiscordForClientErrors() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler(List.of(errorAlertNotifier));
        HttpServletRequest request = request("GET", "/api/v1/folders");

        handler.handleNotFound(new NoHandlerFoundException("GET", "/api/v1/folders", null), request);

        verify(errorAlertNotifier, never()).notify(org.mockito.ArgumentMatchers.any());
    }

    private HttpServletRequest request(String method, String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
        request.setRemoteAddr("127.0.0.1");
        return request;
    }
}
