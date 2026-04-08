package or.hyu.ssd.domain.document.usecase.result;

import or.hyu.ssd.domain.document.entity.Document;

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
