package or.hyu.ssd.domain.document.controller.dto;

import or.hyu.ssd.domain.document.usecase.result.ExternalAiBatchResult;

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
