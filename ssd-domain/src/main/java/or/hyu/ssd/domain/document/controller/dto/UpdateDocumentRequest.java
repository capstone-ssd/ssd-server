package or.hyu.ssd.domain.document.controller.dto;

import java.util.List;

public record UpdateDocumentRequest(
        String title,
        String text,
        String summary,
        String details,
        String path,
        Boolean bookmark,
        List<DocumentParagraphDto> paragraphs
) {}
