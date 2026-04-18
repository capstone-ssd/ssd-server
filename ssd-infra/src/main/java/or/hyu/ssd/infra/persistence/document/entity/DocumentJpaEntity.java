package or.hyu.ssd.infra.persistence.document.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import or.hyu.ssd.infra.persistence.base.BaseJpaEntity;
import or.hyu.ssd.infra.persistence.member.entity.MemberJpaEntity;
import org.hibernate.annotations.Comment;

import java.util.LinkedHashMap;
import java.util.Map;

@Entity
@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "documents",
        indexes = {
                @Index(name = "idx_documents_member_folder_updated_at", columnList = "member_id, folder_id, updated_at"),
                @Index(name = "idx_documents_member_title", columnList = "member_id, title")
        }
)
public class DocumentJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("사업계획서 제목 (입력: title, 없으면 text/paragraphs에서 생성)")
    @Column(name = "title", nullable = false, columnDefinition = "TEXT")
    private String title;

    @Comment("사업계획서 본문 (입력: text)")
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Comment("문서가 속한 폴더 (없으면 루트)")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id")
    private FolderJpaEntity folder;

    @Comment("사업계획서 즐겨찾기 여부 (입력: bookmark)")
    @Column(name = "bookmark", nullable = false)
    private boolean bookmark;

    @Comment("ai가 생성한 사업계획서 세 줄 요약 (입력: summary)")
    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Comment("ai가 생성한 사업계획서 짧은 요약")
    @Column(name = "short_summary", columnDefinition = "TEXT")
    private String shortSummary;

    @Comment("ai가 생성한 사업계획서 상세 요약 (입력: details)")
    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @Comment("ai가 생성한 사업계획서 상세 평가")
    @Column(name = "evaluation", columnDefinition = "TEXT")
    private String evaluation;

    @Comment("ai가 생성한 사업계획서 키워드")
    @Column(name = "keywords", columnDefinition = "TEXT")
    private String keywords;

    @Comment("외부 AI 종합평가 총점")
    @Column(name = "external_ai_total_score")
    private Integer externalAiTotalScore;

    @Comment("외부 AI 종합평가 - 문제 인식 점수")
    @Column(name = "external_ai_problem_recognition_score")
    private Integer externalAiProblemRecognitionScore;

    @Comment("외부 AI 종합평가 - 문제 인식 리뷰")
    @Column(name = "external_ai_problem_recognition_review", columnDefinition = "TEXT")
    private String externalAiProblemRecognitionReview;

    @Comment("외부 AI 종합평가 - 실현 가능성 점수")
    @Column(name = "external_ai_feasibility_score")
    private Integer externalAiFeasibilityScore;

    @Comment("외부 AI 종합평가 - 실현 가능성 리뷰")
    @Column(name = "external_ai_feasibility_review", columnDefinition = "TEXT")
    private String externalAiFeasibilityReview;

    @Comment("외부 AI 종합평가 - 성장 전략 점수")
    @Column(name = "external_ai_growth_strategy_score")
    private Integer externalAiGrowthStrategyScore;

    @Comment("외부 AI 종합평가 - 성장 전략 리뷰")
    @Column(name = "external_ai_growth_strategy_review", columnDefinition = "TEXT")
    private String externalAiGrowthStrategyReview;

    @Comment("외부 AI 종합평가 - 비즈니스 모델 점수")
    @Column(name = "external_ai_business_model_score")
    private Integer externalAiBusinessModelScore;

    @Comment("외부 AI 종합평가 - 비즈니스 모델 리뷰")
    @Column(name = "external_ai_business_model_review", columnDefinition = "TEXT")
    private String externalAiBusinessModelReview;

    @Comment("외부 AI 종합평가 - 팀 구성 점수")
    @Column(name = "external_ai_team_composition_score")
    private Integer externalAiTeamCompositionScore;

    @Comment("외부 AI 종합평가 - 팀 구성 리뷰")
    @Column(name = "external_ai_team_composition_review", columnDefinition = "TEXT")
    private String externalAiTeamCompositionReview;

    @Comment("외부 AI 체크리스트 - 차별성 명확성")
    @Column(name = "checklist_differentiation_is_clear", nullable = false)
    private boolean checklistDifferentiationIsClear;

    @Comment("외부 AI 체크리스트 - 차별성 현실성")
    @Column(name = "checklist_differentiation_is_realistic", nullable = false)
    private boolean checklistDifferentiationIsRealistic;

    @Comment("외부 AI 체크리스트 - 타깃 특화 강점")
    @Column(name = "checklist_target_specific_advantage", nullable = false)
    private boolean checklistTargetSpecificAdvantage;

    @Comment("외부 AI 체크리스트 - 진입장벽 존재")
    @Column(name = "checklist_entry_barrier_exists", nullable = false)
    private boolean checklistEntryBarrierExists;

    @Comment("외부 AI 체크리스트 - 문제 명확성")
    @Column(name = "checklist_problem_is_clear", nullable = false)
    private boolean checklistProblemIsClear;

    @Comment("외부 AI 체크리스트 - 문제 실재성")
    @Column(name = "checklist_problem_is_real", nullable = false)
    private boolean checklistProblemIsReal;

    @Comment("외부 AI 체크리스트 - 타깃/상황 구체성")
    @Column(name = "checklist_target_and_context_are_specific", nullable = false)
    private boolean checklistTargetAndContextAreSpecific;

    @Comment("외부 AI 체크리스트 - 기존 해결책 한계")
    @Column(name = "checklist_existing_solution_has_limits", nullable = false)
    private boolean checklistExistingSolutionHasLimits;

    @Comment("외부 AI 체크리스트 - 시장 정의 적절성")
    @Column(name = "checklist_market_definition_is_correct", nullable = false)
    private boolean checklistMarketDefinitionIsCorrect;

    @Comment("외부 AI 체크리스트 - 시장 규모 현실성")
    @Column(name = "checklist_market_size_is_realistic", nullable = false)
    private boolean checklistMarketSizeIsRealistic;

    @Comment("외부 AI 체크리스트 - 지불의사 명확성")
    @Column(name = "checklist_willingness_to_pay_is_clear", nullable = false)
    private boolean checklistWillingnessToPayIsClear;

    @Comment("외부 AI 체크리스트 - 수익모델 명확성")
    @Column(name = "checklist_revenue_model_is_clear", nullable = false)
    private boolean checklistRevenueModelIsClear;

    @Comment("외부 AI 체크리스트 - 문제-창업자 적합성")
    @Column(name = "checklist_problem_founder_fit", nullable = false)
    private boolean checklistProblemFounderFit;

    @Comment("외부 AI 체크리스트 - 경험 정합성")
    @Column(name = "checklist_experience_alignment", nullable = false)
    private boolean checklistExperienceAlignment;

    @Comment("외부 AI 체크리스트 - 팀 구조 명확성")
    @Column(name = "checklist_team_structure_is_clear", nullable = false)
    private boolean checklistTeamStructureIsClear;

    @Comment("외부 AI 체크리스트 - 역량 공백 보완 계획")
    @Column(name = "checklist_capability_gap_plan_exists", nullable = false)
    private boolean checklistCapabilityGapPlanExists;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private MemberJpaEntity member;

    @Comment("평가자 리뷰 - 사업타당성 평균 점수")
    @Column(name = "review_feasibility_avg")
    private Double reviewFeasibilityAvg;

    @Comment("평가자 리뷰 - 사업차별성 평균 점수")
    @Column(name = "review_differentiation_avg")
    private Double reviewDifferentiationAvg;

    @Comment("평가자 리뷰 - 재무적정성 평균 점수")
    @Column(name = "review_financial_avg")
    private Double reviewFinancialAvg;

    @Comment("평가자 리뷰 - 전체 평균 점수")
    @Column(name = "review_total_avg")
    private Double reviewTotalAvg;

    @Comment("평가자 리뷰 - 참여 평가자 수")
    @Column(name = "review_count")
    private Integer reviewCount;

    @Version
    private Long version;

    public static DocumentJpaEntity of(
            String title,
            String content,
            FolderJpaEntity folder,
            boolean bookmark,
            MemberJpaEntity member
    ) {
        return DocumentJpaEntity.builder()
                .title(title)
                .content(content)
                .folder(folder)
                .bookmark(bookmark)
                .member(member)
                .build();
    }

    public void updateIfPresent(String title, String content, String summary, String details, Boolean bookmark) {
        if (title != null) {
            this.title = title;
        }
        if (content != null) {
            this.content = content;
        }
        if (summary != null) {
            this.summary = summary;
        }
        if (details != null) {
            this.details = details;
        }
        if (bookmark != null) {
            this.bookmark = bookmark;
        }
    }

    public void updateFolder(FolderJpaEntity folder) {
        this.folder = folder;
    }

    public void updateEvaluation(String evaluation) {
        this.evaluation = evaluation;
    }

    public void updateDetails(String details) {
        this.details = details;
    }

    public void updateSummary(String summary) {
        this.summary = summary;
    }

    public void updateSummary(String summary, String shortSummary) {
        this.summary = summary;
        this.shortSummary = shortSummary;
    }

    public void updateKeywords(String keywords) {
        this.keywords = keywords;
    }

    public void updateExternalEvaluationMetrics(
            Integer totalScore,
            Integer problemRecognitionScore,
            String problemRecognitionReview,
            Integer feasibilityScore,
            String feasibilityReview,
            Integer growthStrategyScore,
            String growthStrategyReview,
            Integer businessModelScore,
            String businessModelReview,
            Integer teamCompositionScore,
            String teamCompositionReview
    ) {
        this.externalAiTotalScore = totalScore;
        this.externalAiProblemRecognitionScore = problemRecognitionScore;
        this.externalAiProblemRecognitionReview = normalizeText(problemRecognitionReview);
        this.externalAiFeasibilityScore = feasibilityScore;
        this.externalAiFeasibilityReview = normalizeText(feasibilityReview);
        this.externalAiGrowthStrategyScore = growthStrategyScore;
        this.externalAiGrowthStrategyReview = normalizeText(growthStrategyReview);
        this.externalAiBusinessModelScore = businessModelScore;
        this.externalAiBusinessModelReview = normalizeText(businessModelReview);
        this.externalAiTeamCompositionScore = teamCompositionScore;
        this.externalAiTeamCompositionReview = normalizeText(teamCompositionReview);
    }

    public void overwriteExternalChecklist(Map<String, Boolean> checkList) {
        resetExternalChecklist();
        applyChecklist(checkList, false);
    }

    public void mergeExternalChecklist(Map<String, Boolean> checkList) {
        applyChecklist(checkList, true);
    }

    public Map<String, Boolean> getExternalChecklistSnapshot() {
        Map<String, Boolean> snapshot = new LinkedHashMap<>();
        snapshot.put("differentiation_is_clear", checklistDifferentiationIsClear);
        snapshot.put("differentiation_is_realistic", checklistDifferentiationIsRealistic);
        snapshot.put("target_specific_advantage", checklistTargetSpecificAdvantage);
        snapshot.put("entry_barrier_exists", checklistEntryBarrierExists);
        snapshot.put("problem_is_clear", checklistProblemIsClear);
        snapshot.put("problem_is_real", checklistProblemIsReal);
        snapshot.put("target_and_context_are_specific", checklistTargetAndContextAreSpecific);
        snapshot.put("existing_solution_has_limits", checklistExistingSolutionHasLimits);
        snapshot.put("market_definition_is_correct", checklistMarketDefinitionIsCorrect);
        snapshot.put("market_size_is_realistic", checklistMarketSizeIsRealistic);
        snapshot.put("willingness_to_pay_is_clear", checklistWillingnessToPayIsClear);
        snapshot.put("revenue_model_is_clear", checklistRevenueModelIsClear);
        snapshot.put("problem_founder_fit", checklistProblemFounderFit);
        snapshot.put("experience_alignment", checklistExperienceAlignment);
        snapshot.put("team_structure_is_clear", checklistTeamStructureIsClear);
        snapshot.put("capability_gap_plan_exists", checklistCapabilityGapPlanExists);
        return snapshot;
    }

    public void updateReviewSummary(
            Double feasibilityAvg,
            Double differentiationAvg,
            Double financialAvg,
            Double totalAvg,
            Integer count
    ) {
        this.reviewFeasibilityAvg = feasibilityAvg;
        this.reviewDifferentiationAvg = differentiationAvg;
        this.reviewFinancialAvg = financialAvg;
        this.reviewTotalAvg = totalAvg;
        this.reviewCount = count;
    }

    private void resetExternalChecklist() {
        checklistDifferentiationIsClear = false;
        checklistDifferentiationIsRealistic = false;
        checklistTargetSpecificAdvantage = false;
        checklistEntryBarrierExists = false;
        checklistProblemIsClear = false;
        checklistProblemIsReal = false;
        checklistTargetAndContextAreSpecific = false;
        checklistExistingSolutionHasLimits = false;
        checklistMarketDefinitionIsCorrect = false;
        checklistMarketSizeIsRealistic = false;
        checklistWillingnessToPayIsClear = false;
        checklistRevenueModelIsClear = false;
        checklistProblemFounderFit = false;
        checklistExperienceAlignment = false;
        checklistTeamStructureIsClear = false;
        checklistCapabilityGapPlanExists = false;
    }

    private void applyChecklist(Map<String, Boolean> checkList, boolean merge) {
        if (checkList == null || checkList.isEmpty()) {
            return;
        }
        checkList.forEach((key, value) -> applyChecklistValue(key, Boolean.TRUE.equals(value), merge));
    }

    private void applyChecklistValue(String key, boolean value, boolean merge) {
        boolean nextValue = value;
        switch (key) {
            case "differentiation_is_clear" -> checklistDifferentiationIsClear = merge ? checklistDifferentiationIsClear || value : nextValue;
            case "differentiation_is_realistic" -> checklistDifferentiationIsRealistic = merge ? checklistDifferentiationIsRealistic || value : nextValue;
            case "target_specific_advantage" -> checklistTargetSpecificAdvantage = merge ? checklistTargetSpecificAdvantage || value : nextValue;
            case "entry_barrier_exists" -> checklistEntryBarrierExists = merge ? checklistEntryBarrierExists || value : nextValue;
            case "problem_is_clear" -> checklistProblemIsClear = merge ? checklistProblemIsClear || value : nextValue;
            case "problem_is_real" -> checklistProblemIsReal = merge ? checklistProblemIsReal || value : nextValue;
            case "target_and_context_are_specific" -> checklistTargetAndContextAreSpecific = merge ? checklistTargetAndContextAreSpecific || value : nextValue;
            case "existing_solution_has_limits" -> checklistExistingSolutionHasLimits = merge ? checklistExistingSolutionHasLimits || value : nextValue;
            case "market_definition_is_correct" -> checklistMarketDefinitionIsCorrect = merge ? checklistMarketDefinitionIsCorrect || value : nextValue;
            case "market_size_is_realistic" -> checklistMarketSizeIsRealistic = merge ? checklistMarketSizeIsRealistic || value : nextValue;
            case "willingness_to_pay_is_clear" -> checklistWillingnessToPayIsClear = merge ? checklistWillingnessToPayIsClear || value : nextValue;
            case "revenue_model_is_clear" -> checklistRevenueModelIsClear = merge ? checklistRevenueModelIsClear || value : nextValue;
            case "problem_founder_fit" -> checklistProblemFounderFit = merge ? checklistProblemFounderFit || value : nextValue;
            case "experience_alignment" -> checklistExperienceAlignment = merge ? checklistExperienceAlignment || value : nextValue;
            case "team_structure_is_clear" -> checklistTeamStructureIsClear = merge ? checklistTeamStructureIsClear || value : nextValue;
            case "capability_gap_plan_exists" -> checklistCapabilityGapPlanExists = merge ? checklistCapabilityGapPlanExists || value : nextValue;
            default -> {
            }
        }
    }

    private String normalizeText(String value) {
        return value == null ? null : value.trim();
    }
}
