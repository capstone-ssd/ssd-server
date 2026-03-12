package or.hyu.ssd.domain.document.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateDocumentParagraphRequest(
        @NotBlank(message = "문단 내용은 필수입니다")
        String content,
        @NotBlank(message = "문단 역할은 필수입니다")
        String role,
        Integer blockId
) {}
