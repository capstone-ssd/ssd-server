package or.hyu.ssd.domain.document.controller.dto;

import or.hyu.ssd.domain.document.usecase.result.CreateDocumentResult;

public record CreateDocumentResponse(Long id) {
    public static CreateDocumentResponse from(CreateDocumentResult result) {
        return new CreateDocumentResponse(result.id());
    }
}
