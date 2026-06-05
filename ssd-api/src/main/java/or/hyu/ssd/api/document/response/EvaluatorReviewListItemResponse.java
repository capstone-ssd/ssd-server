package or.hyu.ssd.api.document.response;

import or.hyu.ssd.document.application.result.EvaluatorReviewListItemResult;

import java.time.LocalDateTime;

public record EvaluatorReviewListItemResponse(
        Long reviewId,
        String reviewerName,
        String reviewerEmail,
        LocalDateTime changedAt,
        int feasibility,
        int differentiation,
        int financial,
        int totalScore,
        String comment
) {
    public static EvaluatorReviewListItemResponse from(EvaluatorReviewListItemResult result) {
        return new EvaluatorReviewListItemResponse(
                result.reviewId(),
                result.reviewerName(),
                result.reviewerEmail(),
                result.changedAt(),
                result.feasibility(),
                result.differentiation(),
                result.financial(),
                roundScore(result.totalScore()),
                result.comment()
        );
    }

    private static int roundScore(double score) {
        return (int) Math.round(score);
    }
}
