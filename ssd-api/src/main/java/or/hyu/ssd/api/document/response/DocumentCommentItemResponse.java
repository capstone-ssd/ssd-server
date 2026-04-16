package or.hyu.ssd.api.document.response;

import or.hyu.ssd.domain.document.usecase.result.DocumentCommentItemResult;

import java.time.LocalDateTime;

public record DocumentCommentItemResponse(
        String username,
        String email,
        LocalDateTime createdAt,
        String content,
        String comment
) {
    public static DocumentCommentItemResponse from(DocumentCommentItemResult result) {
        return new DocumentCommentItemResponse(
                result.username(),
                result.email(),
                result.createdAt(),
                result.content(),
                result.comment()
        );
    }
}
