package or.hyu.ssd.document.application.result;

public record EvaluatorReviewIdResult(Long id) {
    public static EvaluatorReviewIdResult of(Long id) {
        return new EvaluatorReviewIdResult(id);
    }
}
