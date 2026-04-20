package or.hyu.ssd.api.document.response;

import or.hyu.ssd.document.application.result.ExternalAiHealthResult;

public record ExternalAiHealthResponse(
        String status,
        boolean available,
        String message
) {
    public static ExternalAiHealthResponse from(ExternalAiHealthResult result) {
        return new ExternalAiHealthResponse(result.status(), result.available(), result.message());
    }
}
