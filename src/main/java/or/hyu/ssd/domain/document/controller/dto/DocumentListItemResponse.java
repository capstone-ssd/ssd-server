package or.hyu.ssd.domain.document.controller.dto;

import or.hyu.ssd.domain.document.entity.Document;
import java.time.LocalDateTime;

public record DocumentListItemResponse(
        Long id,
        String summary,
        boolean bookmark,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static DocumentListItemResponse of(Document doc) {
        return new DocumentListItemResponse(
                doc.getId(),
                doc.getSummary(),
                doc.isBookmark(),
                doc.getCreatedAt(),
                doc.getUpdatedAt()
        );
    }
}

