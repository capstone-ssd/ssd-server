package or.hyu.ssd.api.document.response;

import or.hyu.ssd.document.application.result.DocumentCommentResult;

public record DocumentCommentResponse(Long id) {
    public static DocumentCommentResponse from(DocumentCommentResult result) {
        return new DocumentCommentResponse(result.id());
    }
}
