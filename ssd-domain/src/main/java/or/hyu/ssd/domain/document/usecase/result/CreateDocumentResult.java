package or.hyu.ssd.domain.document.usecase.result;

public record CreateDocumentResult(Long id) {
    public static CreateDocumentResult of(Long id) {
        return new CreateDocumentResult(id);
    }
}
