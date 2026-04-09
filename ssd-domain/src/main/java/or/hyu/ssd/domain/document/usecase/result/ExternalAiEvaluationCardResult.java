package or.hyu.ssd.domain.document.usecase.result;

import java.util.LinkedHashMap;
import java.util.Map;

public record ExternalAiEvaluationCardResult(
        Long documentId,
        Integer totalScore,
        ExternalAiEvaluationMetricResult problemRecognition,
        ExternalAiEvaluationMetricResult feasibility,
        ExternalAiEvaluationMetricResult growthStrategy,
        ExternalAiEvaluationMetricResult businessModel,
        ExternalAiEvaluationMetricResult teamComposition,
        Map<String, Boolean> checkList
) {
    public static ExternalAiEvaluationCardResult of(
            Long documentId,
            Integer totalScore,
            ExternalAiEvaluationMetricResult problemRecognition,
            ExternalAiEvaluationMetricResult feasibility,
            ExternalAiEvaluationMetricResult growthStrategy,
            ExternalAiEvaluationMetricResult businessModel,
            ExternalAiEvaluationMetricResult teamComposition,
            Map<String, Boolean> checkList
    ) {
        return new ExternalAiEvaluationCardResult(
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
