package or.hyu.ssd.api.document.response;

import or.hyu.ssd.document.application.result.ExternalAiBatchResult;

public record ExternalAiBatchResponse(
        Long documentId,
        ExternalAiEvaluationCardResponse evaluation,
        ExternalAiSummaryResponse summary,
        ExternalAiKeywordResponse keyword
) {
    public static ExternalAiBatchResponse from(ExternalAiBatchResult result) {
        return new ExternalAiBatchResponse(
                result.documentId(),
                ExternalAiEvaluationCardResponse.from(result.evaluation()),
                ExternalAiSummaryResponse.from(result.summary()),
                ExternalAiKeywordResponse.from(result.keyword())
        );
    }
}
