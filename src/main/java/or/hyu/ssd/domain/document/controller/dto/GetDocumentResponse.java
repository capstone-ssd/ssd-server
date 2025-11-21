package or.hyu.ssd.domain.document.controller.dto;

import or.hyu.ssd.domain.document.entity.Document;
import java.time.LocalDateTime;

public record GetDocumentResponse(
        Long id,
        String content,
        String summary,
        String details,
        boolean bookmark,
        Long authorId,
        String authorName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static GetDocumentResponse of(Document doc) {
        Long authorId = doc.getMember() != null ? doc.getMember().getId() : null;
        String authorName = doc.getMember() != null ? doc.getMember().getName() : null;
        return new GetDocumentResponse(
                doc.getId(),
                doc.getContent(),
                doc.getSummary(),
                doc.getDetails(),
                doc.isBookmark(),
                authorId,
                authorName,
                doc.getCreatedAt(),
                doc.getUpdatedAt()
        );
    }
}
