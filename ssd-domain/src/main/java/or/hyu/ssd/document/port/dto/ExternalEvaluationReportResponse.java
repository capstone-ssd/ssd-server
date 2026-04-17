package or.hyu.ssd.document.port.dto;

public record ExternalEvaluationReportResponse(
        ExternalEvaluatorMetricResponse teamEvaluator,
        ExternalEvaluatorMetricResponse solEvaluator,
        ExternalEvaluatorMetricResponse problemEvaluator,
        ExternalEvaluatorMetricResponse businessModelEvaluator,
        ExternalEvaluatorMetricResponse scaleUpEvaluator
) {
}
