package or.hyu.ssd.domain.document.usecase.result;

public record EvaluatorReviewIdResult(Long id) {
    public static EvaluatorReviewIdResult of(Long id) {
        return new EvaluatorReviewIdResult(id);
    }
}
