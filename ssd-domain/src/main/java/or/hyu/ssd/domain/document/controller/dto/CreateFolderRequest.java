package or.hyu.ssd.domain.document.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateFolderRequest(
        @NotBlank(message = "폴더명은 필수입니다")
        String name,
        String color,
        Long parentId
) {}
