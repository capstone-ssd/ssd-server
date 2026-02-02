package or.hyu.ssd.domain.document.controller.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record EvaluatorReviewRequest(
        @NotNull @Min(0) @Max(100) Integer feasibility,
        @NotNull @Min(0) @Max(100) Integer differentiation,
        @NotNull @Min(0) @Max(100) Integer financial,
        String comment
) {}
