package or.hyu.ssd.api.document.response;

import or.hyu.ssd.document.application.result.ExternalAiEvaluationCardResult;
import or.hyu.ssd.common.exception.ErrorCode;
import or.hyu.ssd.common.exception.DocumentException;

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
        if (result == null) {
            throw new DocumentException(ErrorCode.EXTERNAL_AI_RESPONSE_INVALID, "평가 카드 결과가 비어 있습니다");
        }
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
