package or.hyu.ssd.domain.document.controller.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;

public record UpdateDocumentRequest(
        String title,
        String text,
        String summary,
        String details,
        @PositiveOrZero(message = "폴더 ID는 0 이상이어야 합니다")
        Long folderId,
        Boolean bookmark,
        List<@Valid DocumentParagraphDto> paragraphs
) {}
