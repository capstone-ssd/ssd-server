package or.hyu.ssd.domain.document.usecase.result;

public record UpdateDocumentResult(Long id) {
    public static UpdateDocumentResult of(Long id) {
        return new UpdateDocumentResult(id);
    }
}
