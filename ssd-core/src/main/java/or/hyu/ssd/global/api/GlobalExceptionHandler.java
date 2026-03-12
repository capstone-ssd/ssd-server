package or.hyu.ssd.global.api;


import io.sentry.Sentry;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.hyu.ssd.global.alert.ErrorAlertContext;
import or.hyu.ssd.global.alert.ErrorAlertNotifier;
import or.hyu.ssd.global.api.handler.CustomException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final List<ErrorAlertNotifier> errorAlertNotifiers;

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(CustomException e, HttpServletRequest request) {
        ErrorCode errorCode = e.getErrorCode();
        log.warn("CustomException 처리 완료: {} - {}", errorCode.getCode(), errorCode.getMessage(), e);
        captureAndNotify(e, errorCode, request);
        ApiResponse<Void> response = ApiResponse.fail(errorCode);

        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(NoHandlerFoundException e, HttpServletRequest request) {
        ErrorCode errorCode = ErrorCode.REQUEST_API_NOT_FOUND;
        log.warn("존재하지 않는 API 요청: {} {}", e.getHttpMethod(), e.getRequestURL());
        captureAndNotify(e, errorCode, request);
        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotAllowed(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        ErrorCode errorCode = ErrorCode.REQUEST_METHOD_NOT_ALLOWED;
        log.warn("허용되지 않은 HTTP 메서드 요청: {}", e.getMessage());
        captureAndNotify(e, errorCode, request);
        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnhandledException(Exception e, HttpServletRequest request) {
        ErrorCode errorCode = ErrorCode.SERVER_EXCEPTION;
        log.error("처리되지 않은 예외가 발생했습니다", e);
        captureAndNotify(e, errorCode, request);
        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode));
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<Void>> handleOptimisticLock(ObjectOptimisticLockingFailureException e, HttpServletRequest request) {
        ErrorCode errorCode = ErrorCode.CHECKLIST_CONFLICT;
        log.warn("낙관적 락 충돌이 발생했습니다: {}", e.getMessage());
        captureAndNotify(e, errorCode, request);
        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotReadable(HttpMessageNotReadableException e, HttpServletRequest request) {
        ErrorCode errorCode = ErrorCode.REQUEST_BODY_INVALID_JSON;
        log.warn("JSON 파싱 오류가 발생했습니다: {}", e.getMessage());
        captureAndNotify(e, errorCode, request);
        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode));
    }

    private void captureAndNotify(Throwable throwable, ErrorCode errorCode, HttpServletRequest request) {
        Sentry.captureException(throwable);

        if (!isServerError(errorCode)) {
            return;
        }

        ErrorAlertContext context = ErrorAlertContext.of(
                request.getMethod(),
                request.getRequestURI(),
                resolveClientIp(request),
                errorCode,
                throwable
        );
        errorAlertNotifiers.forEach(notifier -> notifier.notify(context));
    }

    private boolean isServerError(ErrorCode errorCode) {
        return errorCode.getStatus().is5xxServerError();
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
