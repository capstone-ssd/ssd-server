package or.hyu.ssd.domain.document.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record CreateFolderRequest(
        @NotBlank(message = "폴더명은 필수입니다")
        String name,
        String color,
        @PositiveOrZero(message = "상위 폴더 ID는 0 이상이어야 합니다")
        Long parentId
) {}
