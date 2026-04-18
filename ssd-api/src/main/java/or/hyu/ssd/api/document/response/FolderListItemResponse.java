package or.hyu.ssd.api.document.response;

import or.hyu.ssd.document.application.result.FolderListItemResult;

import java.time.LocalDateTime;

public record FolderListItemResponse(
        Long id,
        String name,
        String color,
        Long parentId,
        boolean hasChildren,
        LocalDateTime updatedAt
) {
    public static FolderListItemResponse from(FolderListItemResult result) {
        return new FolderListItemResponse(
                result.id(),
                result.name(),
                result.color(),
                result.parentId(),
                result.hasChildren(),
                result.updatedAt()
        );
    }
}
