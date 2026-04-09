package or.hyu.ssd.domain.document.service;

import org.springframework.stereotype.Service;
import or.hyu.ssd.domain.document.usecase.command.ExternalDocumentIdCommand;
import or.hyu.ssd.domain.document.usecase.result.ExternalAiBatchResult;
import or.hyu.ssd.domain.document.usecase.result.ExternalAiChecklistResult;
import or.hyu.ssd.domain.document.usecase.result.ExternalAiDocumentCheckResult;
import or.hyu.ssd.domain.document.usecase.result.ExternalAiEvaluationCardResult;
import or.hyu.ssd.domain.document.usecase.result.ExternalAiEvaluationMetricResult;
import or.hyu.ssd.domain.document.usecase.result.ExternalAiKeywordResult;
import or.hyu.ssd.domain.document.usecase.result.ExternalAiSummaryResult;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class MockExternalAiService {

    public ExternalAiEvaluationCardResult evaluate(ExternalDocumentIdCommand command) {
        return buildEvaluation(parseDocumentId(command.docId()));
    }

    public ExternalAiEvaluationCardResult getEvaluation(Long documentId) {
        return buildEvaluation(documentId);
    }

    public ExternalAiSummaryResult summarizeBasic(ExternalDocumentIdCommand command) {
        return buildSummary(parseDocumentId(command.docId()));
    }

    public ExternalAiSummaryResult getSummary(Long documentId) {
        return buildSummary(documentId);
    }

    public ExternalAiKeywordResult summarizeKeyword(ExternalDocumentIdCommand command) {
        return buildKeyword(parseDocumentId(command.docId()));
    }

    public ExternalAiKeywordResult getKeyword(Long documentId) {
        return buildKeyword(documentId);
    }

    public ExternalAiDocumentCheckResult checkNewText(ExternalDocumentIdCommand command) {
        Long documentId = parseDocumentId(command.docId());
        return ExternalAiDocumentCheckResult.of(documentId, List.of(1, 3, 5), buildChecklist());
    }

    public ExternalAiChecklistResult getChecklist(Long documentId) {
        return ExternalAiChecklistResult.of(documentId, buildChecklist());
    }

    public ExternalAiBatchResult generateAll(ExternalDocumentIdCommand command) {
        Long documentId = parseDocumentId(command.docId());
        return ExternalAiBatchResult.of(
                documentId,
                buildEvaluation(documentId),
                buildSummary(documentId),
                buildKeyword(documentId)
        );
    }

    private ExternalAiEvaluationCardResult buildEvaluation(Long documentId) {
        return ExternalAiEvaluationCardResult.of(
                documentId,
                63,
                ExternalAiEvaluationMetricResult.of(
                        "문제 인식",
                        70,
                        "문제 정의는 비교적 명확하지만 시장 근거와 고객 세분화 데이터가 더 필요합니다."
                ),
                ExternalAiEvaluationMetricResult.of(
                        "실현 가능성",
                        60,
                        "솔루션 방향은 타당하지만 구현 범위와 실행 일정이 구체적으로 보강되어야 합니다."
                ),
                ExternalAiEvaluationMetricResult.of(
                        "성장 전략",
                        55,
                        "초기 확장 전략은 보이지만 채널별 성과 가설과 단계별 계획이 부족합니다."
                ),
                ExternalAiEvaluationMetricResult.of(
                        "Business Model",
                        65,
                        "수익 구조는 존재하지만 단가 근거와 반복 매출 구조 검증이 더 필요합니다."
                ),
                ExternalAiEvaluationMetricResult.of(
                        "팀 구성",
                        65,
                        "핵심 역할은 정의되어 있으나 도메인 전문성과 실행 인력 보강 계획이 필요합니다."
                ),
                buildChecklist()
        );
    }

    private ExternalAiSummaryResult buildSummary(Long documentId) {
        return ExternalAiSummaryResult.of(
                documentId,
                "공실 데이터를 통합해 예비 창업자의 비교 비용을 줄이는 사업계획서입니다.",
                "공실 데이터 통합 서비스"
        );
    }

    private ExternalAiKeywordResult buildKeyword(Long documentId) {
        return ExternalAiKeywordResult.of(
                documentId,
                "공실, 상권분석, SaaS, 소상공인, 부동산데이터"
        );
    }

    private Map<String, Boolean> buildChecklist() {
        Map<String, Boolean> checklist = new LinkedHashMap<>();
        checklist.put("differentiation_is_clear", true);
        checklist.put("differentiation_is_realistic", false);
        checklist.put("target_specific_advantage", true);
        checklist.put("entry_barrier_exists", false);
        checklist.put("problem_is_clear", true);
        checklist.put("problem_is_real", true);
        checklist.put("target_and_context_are_specific", true);
        checklist.put("existing_solution_has_limits", true);
        checklist.put("market_definition_is_correct", false);
        checklist.put("market_size_is_realistic", false);
        checklist.put("willingness_to_pay_is_clear", false);
        checklist.put("revenue_model_is_clear", true);
        checklist.put("problem_founder_fit", false);
        checklist.put("experience_alignment", false);
        checklist.put("team_structure_is_clear", true);
        checklist.put("capability_gap_plan_exists", false);
        return checklist;
    }

    private Long parseDocumentId(String rawDocId) {
        return Long.parseLong(rawDocId);
    }
}
