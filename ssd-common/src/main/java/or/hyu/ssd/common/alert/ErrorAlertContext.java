package or.hyu.ssd.common.alert;

import or.hyu.ssd.common.exception.ErrorCode;

public record ErrorAlertContext(
        String requestId,
        String method,
        String uri,
        String clientIp,
        String errorCode,
        int status,
        String exceptionClass,
        String message
) {
    public static ErrorAlertContext of(
            String requestId,
            String method,
            String uri,
            String clientIp,
            ErrorCode errorCode,
            Throwable throwable
    ) {
        return new ErrorAlertContext(
                safeRequestId(requestId),
                method,
                uri,
                clientIp,
                errorCode.getCode(),
                errorCode.getStatus().value(),
                throwable == null ? "(unknown)" : throwable.getClass().getSimpleName(),
                throwable == null ? "(omitted)" : safeMessage(throwable.getMessage())
        );
    }

    private static String safeRequestId(String requestId) {
        if (requestId == null || requestId.isBlank()) {
            return "(omitted)";
        }
        return requestId.replaceAll("[\\r\\n\\t]+", " ").trim();
    }

    private static String safeMessage(String message) {
        if (message == null || message.isBlank()) {
            return "(omitted)";
        }
        return message.replaceAll("[\\r\\n\\t]+", " ").trim();
    }
}
