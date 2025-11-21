package or.hyu.ssd.domain.document.controller.dto;

public record CreateDocumentResponse(Long id) {

    public static CreateDocumentResponse of(Long id) {
        return new CreateDocumentResponse(id);
    }
}
