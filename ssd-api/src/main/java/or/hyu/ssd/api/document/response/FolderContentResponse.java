package or.hyu.ssd.api.document.response;

import or.hyu.ssd.document.application.result.FolderContentResult;

import java.util.List;

public record FolderContentResponse(
        Long parentId,
        Long currentFolderId,
        List<FolderListItemResponse> folders,
        List<DocumentListItemResponse> documents
) {
    public static FolderContentResponse from(FolderContentResult result) {
        return new FolderContentResponse(
                result.parentId(),
                result.currentFolderId(),
                result.folders().stream().map(FolderListItemResponse::from).toList(),
                result.documents().stream().map(DocumentListItemResponse::from).toList()
        );
    }
}
