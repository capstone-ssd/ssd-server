package or.hyu.ssd.domain.document.controller.dto;

public record UpdateDocumentResponse(Long id) {

    public static UpdateDocumentResponse of(Long id) {
        return new UpdateDocumentResponse(id);
    }
}
