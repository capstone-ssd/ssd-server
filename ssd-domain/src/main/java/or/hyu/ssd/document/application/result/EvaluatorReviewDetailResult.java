package or.hyu.ssd.document.application.result;

import or.hyu.ssd.document.domain.entity.EvaluatorReview;

import java.time.LocalDateTime;

public record EvaluatorReviewDetailResult(
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
    public static EvaluatorReviewDetailResult of(EvaluatorReview review) {
        return new EvaluatorReviewDetailResult(
                review.getId(),
                review.getReviewer() == null ? null : review.getReviewer().getName(),
                review.getReviewer() == null ? null : review.getReviewer().getEmail(),
                review.getUpdatedAt(),
                review.getScoreFeasibility(),
                review.getScoreDifferentiation(),
                review.getScoreFinancial(),
                review.getScoreTotal(),
                review.getComment()
        );
    }
}
