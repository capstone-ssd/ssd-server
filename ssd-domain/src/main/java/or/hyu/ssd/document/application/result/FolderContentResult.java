package or.hyu.ssd.document.application.result;

import java.util.List;

public record FolderContentResult(
        Long parentId,
        Long currentFolderId,
        List<FolderListItemResult> folders,
        List<DocumentListItemResult> documents
) {
    public static FolderContentResult of(
            Long parentId,
            Long currentFolderId,
            List<FolderListItemResult> folders,
            List<DocumentListItemResult> documents
    ) {
        return new FolderContentResult(parentId, currentFolderId, folders, documents);
    }
}
