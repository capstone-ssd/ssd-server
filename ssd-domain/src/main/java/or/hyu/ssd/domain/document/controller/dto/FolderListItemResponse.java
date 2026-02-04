package or.hyu.ssd.domain.document.controller.dto;

import or.hyu.ssd.domain.document.entity.Folder;

import java.time.LocalDateTime;

public record FolderListItemResponse(
        Long id,
        String name,
        String color,
        Long parentId,
        boolean hasChildren,
        LocalDateTime updatedAt
) {
    public static FolderListItemResponse of(Folder folder, boolean hasChildren) {
        return new FolderListItemResponse(
                folder.getId(),
                folder.getName(),
                folder.getColor(),
                folder.getParent() != null ? folder.getParent().getId() : null,
                hasChildren,
                folder.getUpdatedAt()
        );
    }
}
