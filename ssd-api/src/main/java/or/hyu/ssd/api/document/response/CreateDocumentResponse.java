package or.hyu.ssd.api.document.response;

import or.hyu.ssd.document.application.result.CreateDocumentResult;

public record CreateDocumentResponse(Long id) {
    public static CreateDocumentResponse from(CreateDocumentResult result) {
        return new CreateDocumentResponse(result.id());
    }
}
