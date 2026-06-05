package or.hyu.ssd.api.document.response;

import or.hyu.ssd.document.application.result.EvaluatorReviewListResult;

import java.util.List;

public record EvaluatorReviewListResponse(
        Long documentId,
        int averageTotalScore,
        int reviewCount,
        List<EvaluatorReviewListItemResponse> reviews
) {
    public static EvaluatorReviewListResponse from(EvaluatorReviewListResult result) {
        return new EvaluatorReviewListResponse(
                result.documentId(),
                roundScore(result.averageTotalScore()),
                result.reviewCount(),
                result.reviews().stream().map(EvaluatorReviewListItemResponse::from).toList()
        );
    }

    private static int roundScore(double score) {
        return (int) Math.round(score);
    }
}
