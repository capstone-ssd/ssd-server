package or.hyu.ssd.api.document.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import or.hyu.ssd.domain.document.usecase.command.CreateEvaluatorReviewCommand;

public record EvaluatorReviewCreateRequest(
        @NotNull(message = "사업타당성 점수는 필수입니다")
        @Min(value = 0, message = "사업타당성 점수는 0 이상이어야 합니다")
        @Max(value = 100, message = "사업타당성 점수는 100 이하여야 합니다")
        Integer feasibility,

        @NotNull(message = "사업차별성 점수는 필수입니다")
        @Min(value = 0, message = "사업차별성 점수는 0 이상이어야 합니다")
        @Max(value = 100, message = "사업차별성 점수는 100 이하여야 합니다")
        Integer differentiation,

        @NotNull(message = "재무적정성 점수는 필수입니다")
        @Min(value = 0, message = "재무적정성 점수는 0 이상이어야 합니다")
        @Max(value = 100, message = "재무적정성 점수는 100 이하여야 합니다")
        Integer financial,

        @NotBlank(message = "상세의견은 필수입니다")
        String comment
) {
    public CreateEvaluatorReviewCommand toCommand() {
        return new CreateEvaluatorReviewCommand(feasibility, differentiation, financial, comment);
    }
}
