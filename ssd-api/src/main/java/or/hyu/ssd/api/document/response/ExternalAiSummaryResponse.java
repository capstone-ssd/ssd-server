package or.hyu.ssd.api.document.response;

import or.hyu.ssd.domain.document.usecase.result.ExternalAiSummaryResult;

public record ExternalAiSummaryResponse(
        Long documentId,
        String summary,
        String shortSummary
) {
    public static ExternalAiSummaryResponse from(ExternalAiSummaryResult result) {
        return new ExternalAiSummaryResponse(result.documentId(), result.summary(), result.shortSummary());
    }
}
