package or.hyu.ssd.domain.document.controller.dto;

import or.hyu.ssd.domain.document.usecase.result.ExternalAiEvaluationCardResult;

import java.util.LinkedHashMap;
import java.util.Map;

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
    public static ExternalAiEvaluationCardResponse from(ExternalAiEvaluationCardResult result) {
        return new ExternalAiEvaluationCardResponse(
                result.documentId(),
                result.totalScore(),
                ExternalAiEvaluationMetricResponse.from(result.problemRecognition()),
                ExternalAiEvaluationMetricResponse.from(result.feasibility()),
                ExternalAiEvaluationMetricResponse.from(result.growthStrategy()),
                ExternalAiEvaluationMetricResponse.from(result.businessModel()),
                ExternalAiEvaluationMetricResponse.from(result.teamComposition()),
                result.checkList() == null ? Map.of() : new LinkedHashMap<>(result.checkList())
        );
    }
}
