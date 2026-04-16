package or.hyu.ssd.document.application.command;

import or.hyu.ssd.common.exception.ErrorCode;
import or.hyu.ssd.common.exception.DocumentException;

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
