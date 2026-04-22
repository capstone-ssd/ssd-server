package or.hyu.ssd.api.document.response;

import or.hyu.ssd.document.application.result.DocumentListItemResult;
import or.hyu.ssd.document.domain.model.DocumentPurpose;

import java.time.LocalDateTime;

public record DocumentListItemResponse(
        Long id,
        String title,
        DocumentPurpose purpose,
        Long folderId,
        LocalDateTime updatedAt
) {
    public static DocumentListItemResponse from(DocumentListItemResult result) {
        return new DocumentListItemResponse(
                result.id(),
                result.title(),
                result.purpose(),
                result.folderId(),
                result.updatedAt()
        );
    }
}
