package or.hyu.ssd.global.api;


import lombok.extern.slf4j.Slf4j;
import or.hyu.ssd.global.api.handler.CustomException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.warn("Handled CustomException: {} - {}", errorCode.getCode(), errorCode.getMessage(), e);
        ApiResponse<Void> response = ApiResponse.fail(errorCode);

        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(NoHandlerFoundException e) {
        ErrorCode errorCode = ErrorCode.REQUEST_API_NOT_FOUND;
        log.warn("API not found: {} {}", e.getHttpMethod(), e.getRequestURL());
        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotAllowed(HttpRequestMethodNotSupportedException e) {
        ErrorCode errorCode = ErrorCode.REQUEST_METHOD_NOT_ALLOWED;
        log.warn("Method not allowed: {}", e.getMessage());
        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnhandledException(Exception e) {
        ErrorCode errorCode = ErrorCode.SERVER_EXCEPTION;
        log.error("Unhandled exception", e);
        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode));
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<Void>> handleOptimisticLock(ObjectOptimisticLockingFailureException e) {
        ErrorCode errorCode = ErrorCode.CHECKLIST_CONFLICT;
        log.warn("Optimistic locking failure: {}", e.getMessage());
        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotReadable(HttpMessageNotReadableException e) {
        ErrorCode errorCode = ErrorCode.REQUEST_BODY_INVALID_JSON;
        log.warn("JSON parse error: {}", e.getMessage());
        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode));
    }
}
