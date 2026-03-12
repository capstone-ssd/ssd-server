package or.hyu.ssd.domain.document.controller.dto;

public record ExternalAiSummaryResponse(
        Long documentId,
        String summary,
        String shortSummary
) {
    public static ExternalAiSummaryResponse of(Long documentId, String summary, String shortSummary) {
        return new ExternalAiSummaryResponse(
                documentId,
                summary == null ? "" : summary.trim(),
                shortSummary == null ? "" : shortSummary.trim()
        );
    }
}
