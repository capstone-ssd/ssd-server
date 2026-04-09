package or.hyu.ssd.domain.document.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DocumentImageMetaRequest(
        @NotBlank(message = "blobKey는 필수입니다")
        String blobKey,
        @NotNull(message = "blockId는 필수입니다")
        @Positive(message = "blockId는 1 이상이어야 합니다")
        Integer blockId
) {
}
