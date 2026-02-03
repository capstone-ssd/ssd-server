package or.hyu.ssd.domain.document.controller.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DocumentCommentRequest(
        @NotNull @Min(1) Integer blockId,
        @NotBlank String comment
) {}
