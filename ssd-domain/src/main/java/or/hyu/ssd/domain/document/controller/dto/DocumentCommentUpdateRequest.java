package or.hyu.ssd.domain.document.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record DocumentCommentUpdateRequest(
        @NotBlank String comment
) {}
