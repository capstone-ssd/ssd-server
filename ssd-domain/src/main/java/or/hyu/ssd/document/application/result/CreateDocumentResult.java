package or.hyu.ssd.document.application.result;

public record CreateDocumentResult(Long id) {
    public static CreateDocumentResult of(Long id) {
        return new CreateDocumentResult(id);
    }
}
