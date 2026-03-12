package or.hyu.ssd.domain.document.controller.dto;

public record ExternalAiEvaluationMetricResponse(
        String label,
        Integer score,
        String review
) {
    public static ExternalAiEvaluationMetricResponse of(String label, Integer score, String review) {
        return new ExternalAiEvaluationMetricResponse(
                label,
                score,
                review == null ? "" : review.trim()
        );
    }
}
