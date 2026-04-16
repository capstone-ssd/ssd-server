package or.hyu.ssd.api.document.response;

import or.hyu.ssd.domain.document.usecase.result.UpdateDocumentResult;

public record UpdateDocumentResponse(Long id) {
    public static UpdateDocumentResponse from(UpdateDocumentResult result) {
        return new UpdateDocumentResponse(result.id());
    }
}
