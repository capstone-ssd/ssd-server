package or.hyu.ssd.domain.document.controller.dto;

import java.util.List;

public record EvaluatorReviewListResponse(
        Long documentId,
        double averageTotalScore,
        int reviewCount,
        List<EvaluatorReviewListItemResponse> reviews
) {
    public static EvaluatorReviewListResponse of(Long documentId, double averageTotalScore, int reviewCount, List<EvaluatorReviewListItemResponse> reviews) {
        return new EvaluatorReviewListResponse(documentId, averageTotalScore, reviewCount, reviews == null ? List.of() : reviews);
    }
}
