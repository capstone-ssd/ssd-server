package or.hyu.ssd.api.document.response;

import or.hyu.ssd.domain.document.usecase.result.DocumentListItemResult;

import java.time.LocalDateTime;

public record DocumentListItemResponse(
        Long id,
        String title,
        Long folderId,
        LocalDateTime updatedAt
) {
    public static DocumentListItemResponse from(DocumentListItemResult result) {
        return new DocumentListItemResponse(
                result.id(),
                result.title(),
                result.folderId(),
                result.updatedAt()
        );
    }
}
