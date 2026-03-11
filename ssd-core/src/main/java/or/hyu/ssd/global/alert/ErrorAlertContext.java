package or.hyu.ssd.global.alert;

import or.hyu.ssd.global.api.ErrorCode;

public record ErrorAlertContext(
        String method,
        String uri,
        String clientIp,
        String errorCode,
        int status,
        String exceptionClass,
        String message
) {
    public static ErrorAlertContext of(
            String method,
            String uri,
            String clientIp,
            ErrorCode errorCode,
            Throwable throwable
    ) {
        return new ErrorAlertContext(
                method,
                uri,
                clientIp,
                errorCode.getCode(),
                errorCode.getStatus().value(),
                throwable == null ? "(unknown)" : throwable.getClass().getSimpleName(),
                throwable == null ? "(omitted)" : safeMessage(throwable.getMessage())
        );
    }

    private static String safeMessage(String message) {
        if (message == null || message.isBlank()) {
            return "(omitted)";
        }
        return message.replaceAll("[\\r\\n\\t]+", " ").trim();
    }
}
