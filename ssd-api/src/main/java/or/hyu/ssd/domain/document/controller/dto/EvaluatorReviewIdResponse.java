package or.hyu.ssd.domain.document.controller.dto;

import or.hyu.ssd.domain.document.usecase.result.EvaluatorReviewIdResult;

public record EvaluatorReviewIdResponse(Long id) {
    public static EvaluatorReviewIdResponse from(EvaluatorReviewIdResult result) {
        return new EvaluatorReviewIdResponse(result.id());
    }
}
