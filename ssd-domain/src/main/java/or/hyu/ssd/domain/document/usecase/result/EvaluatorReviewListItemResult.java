package or.hyu.ssd.domain.document.usecase.result;

import or.hyu.ssd.domain.document.entity.EvaluatorReview;

import java.time.LocalDateTime;

public record EvaluatorReviewListItemResult(
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
    public static EvaluatorReviewListItemResult of(EvaluatorReview review) {
        if (review == null) {
            throw new IllegalArgumentException("EvaluatorReview must not be null");
        }
        return new EvaluatorReviewListItemResult(
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
