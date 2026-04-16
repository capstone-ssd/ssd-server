package or.hyu.ssd.document.port.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ExternalEvaluationReportResponse(
        @JsonProperty("TeamEvaluator")
        ExternalEvaluatorMetricResponse teamEvaluator,
        @JsonProperty("SolEvaluator")
        ExternalEvaluatorMetricResponse solEvaluator,
        @JsonProperty("ProblemEvaluator")
        ExternalEvaluatorMetricResponse problemEvaluator,
        @JsonProperty("BusinessModelEvaluator")
        ExternalEvaluatorMetricResponse businessModelEvaluator,
        @JsonProperty("ScaleUpEvaluator")
        ExternalEvaluatorMetricResponse scaleUpEvaluator
) {
}
