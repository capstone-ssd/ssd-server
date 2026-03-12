package or.hyu.ssd.domain.document.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;

public record CreateDocumentRequest(
        String title,
        @NotBlank(message = "내용은 필수입니다")
        String text,
        List<@Valid CreateDocumentParagraphRequest> paragraphs,
        @PositiveOrZero(message = "폴더 ID는 0 이상이어야 합니다")
        Long folderId
) {}
