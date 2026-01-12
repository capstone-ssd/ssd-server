package or.hyu.ssd.domain.document.controller.dto;

import or.hyu.ssd.domain.document.entity.EvaluatorReview;

public record EvaluatorReviewItemResponse(
        Long reviewId,
        Long reviewerId,
        String reviewerName,
        int feasibility,
        int differentiation,
        int financial,
        double total,
        String comment
) {
    public static EvaluatorReviewItemResponse of(EvaluatorReview review) {
        Long reviewerId = review.getReviewer() != null ? review.getReviewer().getId() : null;
        String reviewerName = review.getReviewer() != null ? review.getReviewer().getName() : null;
        return new EvaluatorReviewItemResponse(
                review.getId(),
                reviewerId,
                reviewerName,
                review.getScoreFeasibility(),
                review.getScoreDifferentiation(),
                review.getScoreFinancial(),
                review.getScoreTotal(),
                review.getComment()
        );
    }
}
