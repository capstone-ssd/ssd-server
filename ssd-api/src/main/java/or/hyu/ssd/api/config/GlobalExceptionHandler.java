package or.hyu.ssd.api.config;


import io.sentry.Sentry;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.hyu.ssd.common.alert.ErrorAlertContext;
import or.hyu.ssd.common.alert.ErrorAlertNotifier;
import or.hyu.ssd.common.api.ApiResponse;
import or.hyu.ssd.common.exception.CustomException;
import or.hyu.ssd.common.exception.ErrorCode;
import or.hyu.ssd.common.logging.LoggingMdcKey;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;
import java.util.Optional;

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
        ApiResponse<Void> response = ApiResponse.fail(errorCode, e.getDetailMessage());

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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException e, HttpServletRequest request) {
        ErrorCode errorCode = ErrorCode.REQUEST_BODY_INVALID_VALUE;
        String message = resolveBindingMessage(e.getBindingResult().getFieldErrors())
                .orElse(errorCode.getMessage());
        log.warn("요청 본문 검증에 실패했습니다: {}", message);
        captureAndNotify(e, errorCode, request);
        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode, message));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Void>> handleBindException(BindException e, HttpServletRequest request) {
        ErrorCode errorCode = ErrorCode.REQUEST_BODY_INVALID_VALUE;
        String message = resolveBindingMessage(e.getBindingResult().getFieldErrors())
                .orElse(errorCode.getMessage());
        log.warn("요청 바인딩 검증에 실패했습니다: {}", message);
        captureAndNotify(e, errorCode, request);
        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode, message));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException e, HttpServletRequest request) {
        ErrorCode errorCode = ErrorCode.REQUEST_BODY_INVALID_VALUE;
        String message = e.getConstraintViolations().stream()
                .findFirst()
                .map(ConstraintViolation::getMessage)
                .filter(msg -> msg != null && !msg.isBlank())
                .orElse(errorCode.getMessage());
        log.warn("요청 제약 조건 검증에 실패했습니다: {}", message);
        captureAndNotify(e, errorCode, request);
        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode, message));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        ErrorCode errorCode = ErrorCode.REQUEST_PARAMETER_INVALID;
        String message = String.format("'%s' 파라미터 형식이 올바르지 않습니다", e.getName());
        log.warn("요청 파라미터 타입이 올바르지 않습니다: {}", e.getMessage());
        captureAndNotify(e, errorCode, request);
        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode, message));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParameter(MissingServletRequestParameterException e, HttpServletRequest request) {
        ErrorCode errorCode = ErrorCode.REQUEST_PARAMETER_MISSING;
        String message = String.format("'%s' 요청 파라미터가 누락되었습니다", e.getParameterName());
        log.warn("필수 요청 파라미터가 누락되었습니다: {}", e.getMessage());
        captureAndNotify(e, errorCode, request);
        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode, message));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingHeader(MissingRequestHeaderException e, HttpServletRequest request) {
        ErrorCode errorCode = ErrorCode.REQUEST_HEADER_MISSING;
        String message = String.format("'%s' 요청 헤더가 누락되었습니다", e.getHeaderName());
        log.warn("필수 요청 헤더가 누락되었습니다: {}", e.getMessage());
        captureAndNotify(e, errorCode, request);
        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode, message));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException e, HttpServletRequest request) {
        ErrorCode errorCode = ErrorCode.REQUEST_MEDIA_TYPE_NOT_SUPPORTED;
        String contentType = Optional.ofNullable(e.getContentType())
                .map(Object::toString)
                .orElse("알 수 없음");
        String message = String.format("지원하지 않는 Content-Type입니다: %s", contentType);
        log.warn("지원하지 않는 Content-Type 요청입니다: {}", e.getMessage());
        captureAndNotify(e, errorCode, request);
        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode, message));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException e, HttpServletRequest request) {
        ErrorCode errorCode = ErrorCode.REQUEST_ACCESS_DENIED;
        log.warn("인가되지 않은 요청입니다: {}", e.getMessage());
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
        String message = resolveJsonParseMessage(e);
        log.warn("JSON 파싱 오류가 발생했습니다: {}", e.getMessage());
        captureAndNotify(e, errorCode, request);
        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode, message));
    }

    private void captureAndNotify(Throwable throwable, ErrorCode errorCode, HttpServletRequest request) {
        MDC.put(LoggingMdcKey.LOG_TYPE, LoggingMdcKey.LOG_TYPE_SERVER_EXCEPTION);
        MDC.put(LoggingMdcKey.RESPONSE_STATUS, String.valueOf(errorCode.getStatus().value()));
        Sentry.captureException(throwable);

        if (!isServerError(errorCode)) {
            return;
        }

        ErrorAlertContext context = ErrorAlertContext.of(
                MDC.get(LoggingMdcKey.REQUEST_ID),
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

    private Optional<String> resolveBindingMessage(List<FieldError> fieldErrors) {
        return fieldErrors.stream()
                .findFirst()
                .map(fieldError -> {
                    String defaultMessage = fieldError.getDefaultMessage();
                    if (defaultMessage != null && !defaultMessage.isBlank()) {
                        return defaultMessage;
                    }
                    return String.format("'%s' 값이 올바르지 않습니다", fieldError.getField());
                });
    }

    private String resolveJsonParseMessage(HttpMessageNotReadableException e) {
        String rawMessage = Optional.ofNullable(e.getMostSpecificCause())
                .map(Throwable::getMessage)
                .orElse(e.getMessage());
        if (rawMessage == null || rawMessage.isBlank()) {
            return ErrorCode.REQUEST_BODY_INVALID_JSON.getMessage();
        }
        if (rawMessage.contains("Trailing token")) {
            return "요청 본문에는 JSON 객체 하나만 포함되어야 합니다";
        }
        if (rawMessage.contains("Required request body is missing")) {
            return "요청 본문이 비어 있습니다";
        }
        if (rawMessage.contains("Cannot deserialize value of type")) {
            return "요청 본문 필드 형식이 올바르지 않습니다";
        }
        return ErrorCode.REQUEST_BODY_INVALID_JSON.getMessage();
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
