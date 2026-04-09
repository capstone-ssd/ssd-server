package or.hyu.ssd.domain.document.controller.dto;

import or.hyu.ssd.domain.document.usecase.result.EvaluatorReviewListItemResult;

import java.time.LocalDateTime;

public record EvaluatorReviewListItemResponse(
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
    public static EvaluatorReviewListItemResponse from(EvaluatorReviewListItemResult result) {
        return new EvaluatorReviewListItemResponse(
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
