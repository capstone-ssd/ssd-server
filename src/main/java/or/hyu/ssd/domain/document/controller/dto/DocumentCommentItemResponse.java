package or.hyu.ssd.domain.document.controller.dto;

import java.time.LocalDateTime;

public record DocumentCommentItemResponse(
        String username,
        String email,
        LocalDateTime createdAt,
        String content,
        String comment
) {
    public static DocumentCommentItemResponse of(String username, String email, LocalDateTime createdAt,
                                                 String content, String comment) {
        return new DocumentCommentItemResponse(username, email, createdAt, content, comment);
    }
}
