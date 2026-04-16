package or.hyu.ssd.domain.document.usecase.result;

import or.hyu.ssd.domain.document.entity.EvaluatorReview;
import or.hyu.ssd.common.exception.ErrorCode;
import or.hyu.ssd.common.exception.DocumentException;

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
            throw new DocumentException(ErrorCode.SERVER_EXCEPTION, "리뷰 결과 항목이 비어 있습니다");
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
