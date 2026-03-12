package or.hyu.ssd.domain.document.controller.dto;

import jakarta.validation.constraints.PositiveOrZero;

public record UpdateFolderRequest(
        String name,
        String color,
        @PositiveOrZero(message = "상위 폴더 ID는 0 이상이어야 합니다")
        Long parentId
) {}
