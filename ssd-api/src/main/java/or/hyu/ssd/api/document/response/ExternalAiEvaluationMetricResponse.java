package or.hyu.ssd.api.document.response;

import or.hyu.ssd.document.application.result.ExternalAiEvaluationMetricResult;

public record ExternalAiEvaluationMetricResponse(
        String label,
        Integer score,
        String review
) {
    public static ExternalAiEvaluationMetricResponse from(ExternalAiEvaluationMetricResult result) {
        return new ExternalAiEvaluationMetricResponse(result.label(), result.score(), result.review());
    }
}
