package or.hyu.ssd.domain.document.controller.dto;

public record ExternalAiBatchResponse(
        Long documentId,
        ExternalAiEvaluationCardResponse evaluation,
        ExternalAiSummaryResponse summary,
        ExternalAiKeywordResponse keyword
) {
    public static ExternalAiBatchResponse of(
            Long documentId,
            ExternalAiEvaluationCardResponse evaluation,
            ExternalAiSummaryResponse summary,
            ExternalAiKeywordResponse keyword
    ) {
        return new ExternalAiBatchResponse(documentId, evaluation, summary, keyword);
    }
}
