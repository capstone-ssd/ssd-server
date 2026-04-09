package or.hyu.ssd.domain.document.usecase.command;

import or.hyu.ssd.global.api.ErrorCode;
import or.hyu.ssd.global.api.handler.DocumentException;

public record UpdateEvaluatorReviewCommand(
        Integer feasibility,
        Integer differentiation,
        Integer financial,
        String comment
) {
    public UpdateEvaluatorReviewCommand {
        if (feasibility == null || differentiation == null || financial == null) {
            throw new DocumentException(
                    ErrorCode.REQUEST_BODY_INVALID_VALUE,
                    "리뷰 점수는 모두 필수입니다"
            );
        }
    }
}
