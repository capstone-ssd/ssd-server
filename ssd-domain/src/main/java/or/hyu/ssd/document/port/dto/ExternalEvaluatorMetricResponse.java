package or.hyu.ssd.document.port.dto;

public record ExternalEvaluatorMetricResponse(
        Double averageScore,
        String finalReview
) {
}
