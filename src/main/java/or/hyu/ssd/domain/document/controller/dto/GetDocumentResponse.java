package or.hyu.ssd.domain.document.controller.dto;

import or.hyu.ssd.domain.document.entity.Document;
import java.util.List;

public record GetDocumentResponse(
        Long id,
        String title,
        String text,
        List<DocumentParagraphDto> paragraphs,
        String summary,
        String details,
        String path,
        boolean bookmark,
        Long authorId,
        String authorName
) {
    public static GetDocumentResponse of(Document doc, List<DocumentParagraphDto> paragraphs) {
        Long authorId = doc.getMember() != null ? doc.getMember().getId() : null;
        String authorName = doc.getMember() != null ? doc.getMember().getName() : null;
        return new GetDocumentResponse(
                doc.getId(),
                doc.getTitle(),
                doc.getContent(),
                paragraphs,
                doc.getSummary(),
                doc.getDetails(),
                doc.getPath(),
                doc.isBookmark(),
                authorId,
                authorName
        );
    }
}
