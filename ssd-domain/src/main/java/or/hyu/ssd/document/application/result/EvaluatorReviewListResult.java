package or.hyu.ssd.document.application.result;

import java.util.List;

public record EvaluatorReviewListResult(
        Long documentId,
        double averageTotalScore,
        int reviewCount,
        List<EvaluatorReviewListItemResult> reviews
) {
    public static EvaluatorReviewListResult of(Long documentId, double averageTotalScore, int reviewCount, List<EvaluatorReviewListItemResult> reviews) {
        return new EvaluatorReviewListResult(documentId, averageTotalScore, reviewCount, reviews == null ? List.of() : reviews);
    }
}
