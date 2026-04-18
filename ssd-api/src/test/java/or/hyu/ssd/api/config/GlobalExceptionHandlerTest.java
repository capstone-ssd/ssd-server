package or.hyu.ssd.api.config;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import or.hyu.ssd.common.alert.ErrorAlertContext;
import or.hyu.ssd.common.alert.ErrorAlertNotifier;
import or.hyu.ssd.common.exception.DocumentException;
import or.hyu.ssd.common.exception.ErrorCode;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.lang.reflect.Method;
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
        // given
        GlobalExceptionHandler handler = new GlobalExceptionHandler(List.of(errorAlertNotifier));
        HttpServletRequest request = request("POST", "/api/v1/external-ai/evaluate");

        // when
        handler.handleGeneralException(new DocumentException(ErrorCode.EXTERNAL_AI_CALL_FAILED), request);

        // then
        ArgumentCaptor<ErrorAlertContext> contextCaptor = ArgumentCaptor.forClass(ErrorAlertContext.class);
        verify(errorAlertNotifier).notify(contextCaptor.capture());
        assertThat(contextCaptor.getValue().errorCode()).isEqualTo("AI50201");
        assertThat(contextCaptor.getValue().uri()).isEqualTo("/api/v1/external-ai/evaluate");
    }

    @Test
    @DisplayName("handleNotFound()는 4xx 예외에서 Discord 알림을 보내지 않는다")
    void handleNotFound_doesNotNotifyDiscordForClientErrors() {
        // given
        GlobalExceptionHandler handler = new GlobalExceptionHandler(List.of(errorAlertNotifier));
        HttpServletRequest request = request("GET", "/api/v1/folders");

        // when
        handler.handleNotFound(new NoHandlerFoundException("GET", "/api/v1/folders", null), request);

        // then
        verify(errorAlertNotifier, never()).notify(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("handleMethodArgumentNotValid()는 첫 번째 검증 메시지를 반환한다")
    void handleMethodArgumentNotValid_returnsFieldValidationMessage() throws NoSuchMethodException {
        // given
        GlobalExceptionHandler handler = new GlobalExceptionHandler(List.of(errorAlertNotifier));
        HttpServletRequest request = request("POST", "/api/v1/folders");
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "name", "폴더명은 필수입니다"));
        Method method = TestController.class.getDeclaredMethod("create", String.class);
        MethodParameter methodParameter = new MethodParameter(method, 0);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(methodParameter, bindingResult);

        // when
        var response = handler.handleMethodArgumentNotValid(exception, request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(ErrorCode.REQUEST_BODY_INVALID_VALUE.getStatus());
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().msg()).isEqualTo("폴더명은 필수입니다");
        verify(errorAlertNotifier, never()).notify(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("handleTypeMismatch()는 파라미터 이름을 포함한 메시지를 반환한다")
    void handleTypeMismatch_returnsReadableMessage() {
        // given
        GlobalExceptionHandler handler = new GlobalExceptionHandler(List.of(errorAlertNotifier));
        HttpServletRequest request = request("GET", "/api/v1/documents");
        MethodArgumentTypeMismatchException exception = new MethodArgumentTypeMismatchException(
                "INVALID",
                Long.class,
                "folderId",
                null,
                new IllegalArgumentException("bad request")
        );

        // when
        var response = handler.handleTypeMismatch(exception, request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(ErrorCode.REQUEST_PARAMETER_INVALID.getStatus());
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().msg()).isEqualTo("'folderId' 파라미터 형식이 올바르지 않습니다");
    }

    @Test
    @DisplayName("handleNotReadable()는 trailing token JSON 오류를 사람이 읽을 수 있게 변환한다")
    void handleNotReadable_returnsReadableJsonMessage() {
        // given
        GlobalExceptionHandler handler = new GlobalExceptionHandler(List.of(errorAlertNotifier));
        HttpServletRequest request = request("POST", "/api/v1/documents");
        HttpMessageNotReadableException exception = new HttpMessageNotReadableException(
                "JSON parse error: Trailing token (`JsonToken.START_OBJECT`) found after value",
                new MockHttpInputMessage(new byte[0])
        );

        // when
        var response = handler.handleNotReadable(exception, request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(ErrorCode.REQUEST_BODY_INVALID_JSON.getStatus());
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().msg()).isEqualTo("요청 본문에는 JSON 객체 하나만 포함되어야 합니다");
    }

    private HttpServletRequest request(String method, String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
        request.setRemoteAddr("127.0.0.1");
        return request;
    }

    @SuppressWarnings("unused")
    private static class TestController {
        private void create(String value) {
        }
    }
}
