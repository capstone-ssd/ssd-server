package or.hyu.ssd.domain.document.usecase.result;

import java.util.List;

public record FolderContentResult(
        Long parentId,
        Long currentFolderId,
        List<FolderListItemResult> folders,
        List<DocumentListItemResult> documents
) {
    public static FolderContentResult of(
            Long parentId,
            List<FolderListItemResult> folders,
            List<DocumentListItemResult> documents
    ) {
        return new FolderContentResult(parentId, parentId, folders, documents);
    }
}
