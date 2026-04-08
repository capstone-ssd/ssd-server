package or.hyu.ssd.domain.document.usecase.result;

import or.hyu.ssd.domain.document.entity.Folder;

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
