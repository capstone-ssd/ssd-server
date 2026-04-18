package or.hyu.ssd.api.document.response;

import or.hyu.ssd.document.application.result.EvaluatorReviewListResult;

import java.util.List;

public record EvaluatorReviewListResponse(
        Long documentId,
        double averageTotalScore,
        int reviewCount,
        List<EvaluatorReviewListItemResponse> reviews
) {
    public static EvaluatorReviewListResponse from(EvaluatorReviewListResult result) {
        return new EvaluatorReviewListResponse(
                result.documentId(),
                result.averageTotalScore(),
                result.reviewCount(),
                result.reviews().stream().map(EvaluatorReviewListItemResponse::from).toList()
        );
    }
}
