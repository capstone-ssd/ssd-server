package or.hyu.ssd.domain.document.controller.dto;

import java.util.Map;
import java.util.LinkedHashMap;

public record ExternalAiEvaluationCardResponse(
        Long documentId,
        Integer totalScore,
        ExternalAiEvaluationMetricResponse problemRecognition,
        ExternalAiEvaluationMetricResponse feasibility,
        ExternalAiEvaluationMetricResponse growthStrategy,
        ExternalAiEvaluationMetricResponse businessModel,
        ExternalAiEvaluationMetricResponse teamComposition,
        Map<String, Boolean> checkList
) {
    public static ExternalAiEvaluationCardResponse of(
            Long documentId,
            Integer totalScore,
            ExternalAiEvaluationMetricResponse problemRecognition,
            ExternalAiEvaluationMetricResponse feasibility,
            ExternalAiEvaluationMetricResponse growthStrategy,
            ExternalAiEvaluationMetricResponse businessModel,
            ExternalAiEvaluationMetricResponse teamComposition,
            Map<String, Boolean> checkList
    ) {
        return new ExternalAiEvaluationCardResponse(
                documentId,
                totalScore,
                problemRecognition,
                feasibility,
                growthStrategy,
                businessModel,
                teamComposition,
                checkList == null ? Map.of() : new LinkedHashMap<>(checkList)
        );
    }
}
