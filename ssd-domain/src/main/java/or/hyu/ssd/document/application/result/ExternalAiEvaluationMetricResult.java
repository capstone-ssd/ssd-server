package or.hyu.ssd.document.application.result;

public record ExternalAiEvaluationMetricResult(
        String label,
        Integer score,
        String review
) {
    public static ExternalAiEvaluationMetricResult of(String label, Integer score, String review) {
        return new ExternalAiEvaluationMetricResult(label, score, review == null ? "" : review.trim());
    }
}
