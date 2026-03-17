package or.hyu.ssd.domain.document.controller.dto;

import or.hyu.ssd.domain.document.entity.EvaluatorReview;

public record EvaluatorReviewListItemResponse(
        String reviewerName,
        double totalScore
) {
    public static EvaluatorReviewListItemResponse of(EvaluatorReview review) {
        return new EvaluatorReviewListItemResponse(
                review.getReviewer() == null ? null : review.getReviewer().getName(),
                review.getScoreTotal()
        );
    }
}
