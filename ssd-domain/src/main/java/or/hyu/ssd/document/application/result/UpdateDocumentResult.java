package or.hyu.ssd.document.application.result;

public record UpdateDocumentResult(Long id) {
    public static UpdateDocumentResult of(Long id) {
        return new UpdateDocumentResult(id);
    }
}
