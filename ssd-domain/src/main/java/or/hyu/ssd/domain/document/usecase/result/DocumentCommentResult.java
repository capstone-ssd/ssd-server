package or.hyu.ssd.domain.document.usecase.result;

public record DocumentCommentResult(Long id) {
    public static DocumentCommentResult of(Long id) {
        return new DocumentCommentResult(id);
    }
}
