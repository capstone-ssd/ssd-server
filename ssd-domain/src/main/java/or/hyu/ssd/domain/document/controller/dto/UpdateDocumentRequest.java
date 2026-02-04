package or.hyu.ssd.domain.document.controller.dto;

import java.util.List;

public record UpdateDocumentRequest(
        String title,
        String text,
        String summary,
        String details,
        Long folderId,
        Boolean bookmark,
        List<DocumentParagraphDto> paragraphs
) {}
