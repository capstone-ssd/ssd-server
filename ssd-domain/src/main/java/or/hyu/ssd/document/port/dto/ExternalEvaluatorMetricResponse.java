package or.hyu.ssd.document.port.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ExternalEvaluatorMetricResponse(
        @JsonProperty("average_score")
        Double averageScore,
        @JsonProperty("final_review")
        String finalReview
) {
}
