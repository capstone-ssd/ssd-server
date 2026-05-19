package or.hyu.ssd.api.document.response;

import or.hyu.ssd.document.application.result.MoveDocumentFolderResult;

public record MoveDocumentFolderResponse(
        Long documentId,
        Long folderId
) {
    public static MoveDocumentFolderResponse from(MoveDocumentFolderResult result) {
        return new MoveDocumentFolderResponse(result.documentId(), result.folderId());
    }
}
