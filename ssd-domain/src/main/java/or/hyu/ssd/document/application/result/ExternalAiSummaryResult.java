package or.hyu.ssd.document.application.result;

public record ExternalAiSummaryResult(
        Long documentId,
        String summary,
        String shortSummary
) {
    public static ExternalAiSummaryResult of(Long documentId, String summary, String shortSummary) {
        return new ExternalAiSummaryResult(
                documentId,
                summary == null ? "" : summary.trim(),
                shortSummary == null ? "" : shortSummary.trim()
        );
    }
}
