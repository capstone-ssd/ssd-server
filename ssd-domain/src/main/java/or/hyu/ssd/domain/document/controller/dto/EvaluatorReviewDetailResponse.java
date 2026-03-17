package or.hyu.ssd.domain.document.controller.dto;

import or.hyu.ssd.domain.document.entity.EvaluatorReview;

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
    public static EvaluatorReviewDetailResponse of(EvaluatorReview review) {
        return new EvaluatorReviewDetailResponse(
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
