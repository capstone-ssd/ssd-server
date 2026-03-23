package or.hyu.ssd.domain.document.controller.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record UpdateDocumentRequest(
        String title,
        @NotBlank(message = "내용은 필수입니다")
        String text,
        List<@Valid CreateDocumentParagraphRequest> paragraphs
) {}
