package or.hyu.ssd.api.document.response;

import or.hyu.ssd.document.application.result.UpdateDocumentResult;

public record UpdateDocumentResponse(Long id) {
    public static UpdateDocumentResponse from(UpdateDocumentResult result) {
        return new UpdateDocumentResponse(result.id());
    }
}
