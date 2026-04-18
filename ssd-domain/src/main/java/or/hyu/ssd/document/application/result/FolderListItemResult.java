package or.hyu.ssd.document.application.result;

import or.hyu.ssd.document.domain.model.Folder;

import java.time.LocalDateTime;

public record FolderListItemResult(
        Long id,
        String name,
        String color,
        Long parentId,
        boolean hasChildren,
        LocalDateTime updatedAt
) {
    public static FolderListItemResult of(Folder folder, boolean hasChildren) {
        return new FolderListItemResult(
                folder.getId(),
                folder.getName(),
                folder.getColor(),
                folder.getParent() != null ? folder.getParent().getId() : 0L,
                hasChildren,
                folder.getUpdatedAt()
        );
    }
}
