package or.hyu.ssd.domain.document.controller.dto;

import java.util.List;

public record FolderContentResponse(
        Long parentId,
        List<FolderListItemResponse> folders,
        List<DocumentListItemResponse> documents
) {
    public static FolderContentResponse of(
            Long parentId,
            List<FolderListItemResponse> folders,
            List<DocumentListItemResponse> documents
    ) {
        return new FolderContentResponse(parentId, folders, documents);
    }
}
