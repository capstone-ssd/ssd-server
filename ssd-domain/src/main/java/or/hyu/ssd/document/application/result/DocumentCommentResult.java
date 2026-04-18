package or.hyu.ssd.document.application.result;

public record DocumentCommentResult(Long id) {
    public static DocumentCommentResult of(Long id) {
        return new DocumentCommentResult(id);
    }
}
