package or.hyu.ssd.document.application.result;

import or.hyu.ssd.document.domain.entity.Document;

import java.time.LocalDateTime;

public record DocumentListItemResult(
        Long id,
        String title,
        Long folderId,
        LocalDateTime updatedAt
) {
    public static DocumentListItemResult of(Document doc) {
        return new DocumentListItemResult(
                doc.getId(),
                doc.getTitle(),
                doc.getFolder() != null ? doc.getFolder().getId() : null,
                doc.getUpdatedAt()
        );
    }
}
