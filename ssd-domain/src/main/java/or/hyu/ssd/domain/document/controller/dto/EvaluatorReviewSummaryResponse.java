package or.hyu.ssd.domain.document.controller.dto;

public record EvaluatorReviewSummaryResponse(
        Long documentId,
        double feasibilityAvg,
        double differentiationAvg,
        double financialAvg,
        double totalAvg,
        int reviewCount
) {
    public static EvaluatorReviewSummaryResponse of(Long documentId, double feasibilityAvg, double differentiationAvg, double financialAvg, double totalAvg, int reviewCount) {
        return new EvaluatorReviewSummaryResponse(documentId, feasibilityAvg, differentiationAvg, financialAvg, totalAvg, reviewCount);
    }
}
