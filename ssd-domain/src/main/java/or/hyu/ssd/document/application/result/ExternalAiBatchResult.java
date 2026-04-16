package or.hyu.ssd.document.application.result;

public record ExternalAiBatchResult(
        Long documentId,
        ExternalAiEvaluationCardResult evaluation,
        ExternalAiSummaryResult summary,
        ExternalAiKeywordResult keyword
) {
    public static ExternalAiBatchResult of(
            Long documentId,
            ExternalAiEvaluationCardResult evaluation,
            ExternalAiSummaryResult summary,
            ExternalAiKeywordResult keyword
    ) {
        return new ExternalAiBatchResult(documentId, evaluation, summary, keyword);
    }
}
