package or.hyu.ssd.domain.document.controller.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record DocumentParagraphDto(
        @NotBlank(message = "문단 내용은 필수입니다")
        String content,
        @NotBlank(message = "문단 역할은 필수입니다")
        String role,
        @Min(value = 1, message = "문단 페이지 번호는 1 이상이어야 합니다")
        int pageNumber,
        Integer blockId
) {}
