package or.hyu.ssd.document.application.result;

public record MoveDocumentFolderResult(
        Long documentId,
        Long folderId
) {
    public static MoveDocumentFolderResult of(Long documentId, Long folderId) {
        return new MoveDocumentFolderResult(documentId, folderId);
    }
}
