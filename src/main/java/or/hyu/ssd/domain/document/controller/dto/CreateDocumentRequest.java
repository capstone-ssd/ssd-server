package or.hyu.ssd.domain.document.controller.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record CreateDocumentRequest(
        @NotBlank(message = "제목은 필수입니다")
        String title,
        @NotBlank(message = "내용은 필수입니다")
        String text,
        List<DocumentParagraphDto> paragraphs,
        String path
) {}
