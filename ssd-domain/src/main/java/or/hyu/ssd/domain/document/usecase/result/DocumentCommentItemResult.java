package or.hyu.ssd.domain.document.usecase.result;

import java.time.LocalDateTime;

public record DocumentCommentItemResult(
        String username,
        String email,
        LocalDateTime createdAt,
        String content,
        String comment
) {
    public static DocumentCommentItemResult of(String username, String email, LocalDateTime createdAt, String content, String comment) {
        return new DocumentCommentItemResult(username, email, createdAt, content, comment);
    }
}
