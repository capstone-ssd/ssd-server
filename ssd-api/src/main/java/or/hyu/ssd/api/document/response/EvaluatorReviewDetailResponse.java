package or.hyu.ssd.api.document.response;

import or.hyu.ssd.document.application.result.EvaluatorReviewDetailResult;

import java.time.LocalDateTime;

public record EvaluatorReviewDetailResponse(
        Long reviewId,
        String reviewerName,
        String reviewerEmail,
        LocalDateTime changedAt,
        int feasibility,
        int differentiation,
        int financial,
        double totalScore,
        String comment
) {
    public static EvaluatorReviewDetailResponse from(EvaluatorReviewDetailResult result) {
        return new EvaluatorReviewDetailResponse(
                result.reviewId(),
                result.reviewerName(),
                result.reviewerEmail(),
                result.changedAt(),
                result.feasibility(),
                result.differentiation(),
                result.financial(),
                result.totalScore(),
                result.comment()
        );
    }
}
