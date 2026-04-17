package or.hyu.ssd.document.domain.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import or.hyu.ssd.member.domain.entity.Member;
import or.hyu.ssd.shared.domain.AuditableDomainEntity;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Document extends AuditableDomainEntity {

    private Long id;
    private String title;
    private String content;
    private Folder folder;
    private boolean bookmark;
    private String summary;
    private String shortSummary;
    private String details;
    private String evaluation;
    private String keywords;
    private Integer externalAiTotalScore;
    private Integer externalAiProblemRecognitionScore;
    private String externalAiProblemRecognitionReview;
    private Integer externalAiFeasibilityScore;
    private String externalAiFeasibilityReview;
    private Integer externalAiGrowthStrategyScore;
    private String externalAiGrowthStrategyReview;
    private Integer externalAiBusinessModelScore;
    private String externalAiBusinessModelReview;
    private Integer externalAiTeamCompositionScore;
    private String externalAiTeamCompositionReview;
    private boolean checklistDifferentiationIsClear;
    private boolean checklistDifferentiationIsRealistic;
    private boolean checklistTargetSpecificAdvantage;
    private boolean checklistEntryBarrierExists;
    private boolean checklistProblemIsClear;
    private boolean checklistProblemIsReal;
    private boolean checklistTargetAndContextAreSpecific;
    private boolean checklistExistingSolutionHasLimits;
    private boolean checklistMarketDefinitionIsCorrect;
    private boolean checklistMarketSizeIsRealistic;
    private boolean checklistWillingnessToPayIsClear;
    private boolean checklistRevenueModelIsClear;
    private boolean checklistProblemFounderFit;
    private boolean checklistExperienceAlignment;
    private boolean checklistTeamStructureIsClear;
    private boolean checklistCapabilityGapPlanExists;
    private Member member;
    private Double reviewFeasibilityAvg;
    private Double reviewDifferentiationAvg;
    private Double reviewFinancialAvg;
    private Double reviewTotalAvg;
    private Integer reviewCount;
    private Long version;

    public static Document of(String title, String content, Folder folder, boolean bookmark, Member member) {
        return Document.builder()
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

    public void updateFolder(Folder folder) {
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
