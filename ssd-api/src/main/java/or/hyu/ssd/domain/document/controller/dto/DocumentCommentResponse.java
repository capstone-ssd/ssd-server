package or.hyu.ssd.domain.document.controller.dto;

import or.hyu.ssd.domain.document.usecase.result.DocumentCommentResult;

public record DocumentCommentResponse(Long id) {
    public static DocumentCommentResponse from(DocumentCommentResult result) {
        return new DocumentCommentResponse(result.id());
    }
}
