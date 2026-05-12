package or.hyu.ssd.infra.persistence.document.mapper;

import or.hyu.ssd.document.domain.model.CheckList;
import or.hyu.ssd.document.domain.model.Document;
import or.hyu.ssd.document.domain.model.DocumentAiCheckSnapshot;
import or.hyu.ssd.document.domain.model.DocumentComment;
import or.hyu.ssd.document.domain.model.DocumentLog;
import or.hyu.ssd.document.domain.model.DocumentParagraph;
import or.hyu.ssd.document.domain.model.EvaluatorCheckList;
import or.hyu.ssd.document.domain.model.EvaluatorReview;
import or.hyu.ssd.document.domain.model.Folder;
import or.hyu.ssd.infra.persistence.base.BaseJpaEntity;
import or.hyu.ssd.infra.persistence.document.entity.CheckListJpaEntity;
import or.hyu.ssd.infra.persistence.document.entity.DocumentAiCheckSnapshotJpaEntity;
import or.hyu.ssd.infra.persistence.document.entity.DocumentCommentJpaEntity;
import or.hyu.ssd.infra.persistence.document.entity.DocumentJpaEntity;
import or.hyu.ssd.infra.persistence.document.entity.DocumentLogJpaEntity;
import or.hyu.ssd.infra.persistence.document.entity.DocumentParagraphJpaEntity;
import or.hyu.ssd.infra.persistence.document.entity.EvaluatorCheckListJpaEntity;
import or.hyu.ssd.infra.persistence.document.entity.EvaluatorReviewJpaEntity;
import or.hyu.ssd.infra.persistence.document.entity.FolderJpaEntity;
import or.hyu.ssd.infra.persistence.member.mapper.MemberPersistenceMapper;
import or.hyu.ssd.shared.domain.AuditableDomainEntity;

public final class DocumentPersistenceMapper {

    private DocumentPersistenceMapper() {
    }

    public static Document toDomain(DocumentJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        Document.DocumentBuilder<?, ?> builder = Document.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .folder(toDomain(entity.getFolder()))
                .bookmark(entity.isBookmark())
                .purpose(entity.getPurpose())
                .summary(entity.getSummary())
                .shortSummary(entity.getShortSummary())
                .details(entity.getDetails())
                .evaluation(entity.getEvaluation())
                .keywords(entity.getKeywords())
                .externalAiProcessed(entity.isExternalAiProcessed())
                .externalAiTotalScore(entity.getExternalAiTotalScore())
                .externalAiProblemRecognitionScore(entity.getExternalAiProblemRecognitionScore())
                .externalAiProblemRecognitionReview(entity.getExternalAiProblemRecognitionReview())
                .externalAiFeasibilityScore(entity.getExternalAiFeasibilityScore())
                .externalAiFeasibilityReview(entity.getExternalAiFeasibilityReview())
                .externalAiGrowthStrategyScore(entity.getExternalAiGrowthStrategyScore())
                .externalAiGrowthStrategyReview(entity.getExternalAiGrowthStrategyReview())
                .externalAiBusinessModelScore(entity.getExternalAiBusinessModelScore())
                .externalAiBusinessModelReview(entity.getExternalAiBusinessModelReview())
                .externalAiTeamCompositionScore(entity.getExternalAiTeamCompositionScore())
                .externalAiTeamCompositionReview(entity.getExternalAiTeamCompositionReview())
                .checklistDifferentiationIsClear(entity.isChecklistDifferentiationIsClear())
                .checklistDifferentiationIsRealistic(entity.isChecklistDifferentiationIsRealistic())
                .checklistTargetSpecificAdvantage(entity.isChecklistTargetSpecificAdvantage())
                .checklistEntryBarrierExists(entity.isChecklistEntryBarrierExists())
                .checklistProblemIsClear(entity.isChecklistProblemIsClear())
                .checklistProblemIsReal(entity.isChecklistProblemIsReal())
                .checklistTargetAndContextAreSpecific(entity.isChecklistTargetAndContextAreSpecific())
                .checklistExistingSolutionHasLimits(entity.isChecklistExistingSolutionHasLimits())
                .checklistMarketDefinitionIsCorrect(entity.isChecklistMarketDefinitionIsCorrect())
                .checklistMarketSizeIsRealistic(entity.isChecklistMarketSizeIsRealistic())
                .checklistWillingnessToPayIsClear(entity.isChecklistWillingnessToPayIsClear())
                .checklistRevenueModelIsClear(entity.isChecklistRevenueModelIsClear())
                .checklistProblemFounderFit(entity.isChecklistProblemFounderFit())
                .checklistExperienceAlignment(entity.isChecklistExperienceAlignment())
                .checklistTeamStructureIsClear(entity.isChecklistTeamStructureIsClear())
                .checklistCapabilityGapPlanExists(entity.isChecklistCapabilityGapPlanExists())
                .member(MemberPersistenceMapper.toDomain(entity.getMember()))
                .reviewFeasibilityAvg(entity.getReviewFeasibilityAvg())
                .reviewDifferentiationAvg(entity.getReviewDifferentiationAvg())
                .reviewFinancialAvg(entity.getReviewFinancialAvg())
                .reviewTotalAvg(entity.getReviewTotalAvg())
                .reviewCount(entity.getReviewCount())
                .version(entity.getVersion());
        applyAudit(entity, builder);
        return builder.build();
    }

    public static DocumentJpaEntity toJpa(Document document) {
        if (document == null) {
            return null;
        }

        DocumentJpaEntity.DocumentJpaEntityBuilder<?, ?> builder = DocumentJpaEntity.builder()
                .id(document.getId())
                .title(document.getTitle())
                .content(document.getContent())
                .folder(toFolderRef(document.getFolder()))
                .bookmark(document.isBookmark())
                .purpose(document.getPurpose())
                .summary(document.getSummary())
                .shortSummary(document.getShortSummary())
                .details(document.getDetails())
                .evaluation(document.getEvaluation())
                .keywords(document.getKeywords())
                .externalAiProcessed(document.isExternalAiProcessed())
                .externalAiTotalScore(document.getExternalAiTotalScore())
                .externalAiProblemRecognitionScore(document.getExternalAiProblemRecognitionScore())
                .externalAiProblemRecognitionReview(document.getExternalAiProblemRecognitionReview())
                .externalAiFeasibilityScore(document.getExternalAiFeasibilityScore())
                .externalAiFeasibilityReview(document.getExternalAiFeasibilityReview())
                .externalAiGrowthStrategyScore(document.getExternalAiGrowthStrategyScore())
                .externalAiGrowthStrategyReview(document.getExternalAiGrowthStrategyReview())
                .externalAiBusinessModelScore(document.getExternalAiBusinessModelScore())
                .externalAiBusinessModelReview(document.getExternalAiBusinessModelReview())
                .externalAiTeamCompositionScore(document.getExternalAiTeamCompositionScore())
                .externalAiTeamCompositionReview(document.getExternalAiTeamCompositionReview())
                .checklistDifferentiationIsClear(document.isChecklistDifferentiationIsClear())
                .checklistDifferentiationIsRealistic(document.isChecklistDifferentiationIsRealistic())
                .checklistTargetSpecificAdvantage(document.isChecklistTargetSpecificAdvantage())
                .checklistEntryBarrierExists(document.isChecklistEntryBarrierExists())
                .checklistProblemIsClear(document.isChecklistProblemIsClear())
                .checklistProblemIsReal(document.isChecklistProblemIsReal())
                .checklistTargetAndContextAreSpecific(document.isChecklistTargetAndContextAreSpecific())
                .checklistExistingSolutionHasLimits(document.isChecklistExistingSolutionHasLimits())
                .checklistMarketDefinitionIsCorrect(document.isChecklistMarketDefinitionIsCorrect())
                .checklistMarketSizeIsRealistic(document.isChecklistMarketSizeIsRealistic())
                .checklistWillingnessToPayIsClear(document.isChecklistWillingnessToPayIsClear())
                .checklistRevenueModelIsClear(document.isChecklistRevenueModelIsClear())
                .checklistProblemFounderFit(document.isChecklistProblemFounderFit())
                .checklistExperienceAlignment(document.isChecklistExperienceAlignment())
                .checklistTeamStructureIsClear(document.isChecklistTeamStructureIsClear())
                .checklistCapabilityGapPlanExists(document.isChecklistCapabilityGapPlanExists())
                .member(MemberPersistenceMapper.toRef(document.getMember()))
                .reviewFeasibilityAvg(document.getReviewFeasibilityAvg())
                .reviewDifferentiationAvg(document.getReviewDifferentiationAvg())
                .reviewFinancialAvg(document.getReviewFinancialAvg())
                .reviewTotalAvg(document.getReviewTotalAvg())
                .reviewCount(document.getReviewCount())
                .version(document.getVersion());
        applyAudit(document, builder);
        return builder.build();
    }

    public static Folder toDomain(FolderJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        Folder.FolderBuilder<?, ?> builder = Folder.builder()
                .id(entity.getId())
                .name(entity.getName())
                .color(entity.getColor())
                .parent(toDomain(entity.getParent()))
                .member(MemberPersistenceMapper.toDomain(entity.getMember()))
                .version(entity.getVersion());
        applyAudit(entity, builder);
        return builder.build();
    }

    public static FolderJpaEntity toJpa(Folder folder) {
        if (folder == null) {
            return null;
        }

        FolderJpaEntity.FolderJpaEntityBuilder<?, ?> builder = FolderJpaEntity.builder()
                .id(folder.getId())
                .name(folder.getName())
                .color(folder.getColor())
                .parent(toFolderRef(folder.getParent()))
                .member(MemberPersistenceMapper.toRef(folder.getMember()))
                .version(folder.getVersion());
        applyAudit(folder, builder);
        return builder.build();
    }

    public static FolderJpaEntity toFolderRef(Folder folder) {
        if (folder == null || folder.getId() == null) {
            return null;
        }

        return FolderJpaEntity.builder()
                .id(folder.getId())
                .version(folder.getVersion())
                .build();
    }

    public static DocumentParagraph toDomain(DocumentParagraphJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        DocumentParagraph.DocumentParagraphBuilder<?, ?> builder = DocumentParagraph.builder()
                .id(entity.getId())
                .content(entity.getContent())
                .type(entity.getType())
                .role(entity.getRole())
                .pageNumber(entity.getPageNumber())
                .blockId(entity.getBlockId())
                .document(toDocumentRef(entity.getDocument()))
                .version(entity.getVersion());
        applyAudit(entity, builder);
        return builder.build();
    }

    public static DocumentParagraphJpaEntity toJpa(DocumentParagraph paragraph) {
        if (paragraph == null) {
            return null;
        }

        DocumentParagraphJpaEntity.DocumentParagraphJpaEntityBuilder<?, ?> builder = DocumentParagraphJpaEntity.builder()
                .id(paragraph.getId())
                .content(paragraph.getContent())
                .type(paragraph.getType())
                .role(paragraph.getRole())
                .pageNumber(paragraph.getPageNumber())
                .blockId(paragraph.getBlockId())
                .document(toDocumentRef(paragraph.getDocument()))
                .version(paragraph.getVersion());
        applyAudit(paragraph, builder);
        return builder.build();
    }

    public static CheckList toDomain(CheckListJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        CheckList.CheckListBuilder<?, ?> builder = CheckList.builder()
                .id(entity.getId())
                .content(entity.getContent())
                .checked(entity.isChecked())
                .document(toDocumentRef(entity.getDocument()))
                .version(entity.getVersion());
        applyAudit(entity, builder);
        return builder.build();
    }

    public static CheckListJpaEntity toJpa(CheckList checkList) {
        if (checkList == null) {
            return null;
        }

        CheckListJpaEntity.CheckListJpaEntityBuilder<?, ?> builder = CheckListJpaEntity.builder()
                .id(checkList.getId())
                .content(checkList.getContent())
                .checked(checkList.isChecked())
                .document(toDocumentRef(checkList.getDocument()))
                .version(checkList.getVersion());
        applyAudit(checkList, builder);
        return builder.build();
    }

    public static EvaluatorCheckList toDomain(EvaluatorCheckListJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        EvaluatorCheckList.EvaluatorCheckListBuilder<?, ?> builder = EvaluatorCheckList.builder()
                .id(entity.getId())
                .content(entity.getContent())
                .checked(entity.isChecked())
                .document(toDocumentRef(entity.getDocument()))
                .version(entity.getVersion());
        applyAudit(entity, builder);
        return builder.build();
    }

    public static EvaluatorCheckListJpaEntity toJpa(EvaluatorCheckList checkList) {
        if (checkList == null) {
            return null;
        }

        EvaluatorCheckListJpaEntity.EvaluatorCheckListJpaEntityBuilder<?, ?> builder = EvaluatorCheckListJpaEntity.builder()
                .id(checkList.getId())
                .content(checkList.getContent())
                .checked(checkList.isChecked())
                .document(toDocumentRef(checkList.getDocument()))
                .version(checkList.getVersion());
        applyAudit(checkList, builder);
        return builder.build();
    }

    public static DocumentAiCheckSnapshot toDomain(DocumentAiCheckSnapshotJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        DocumentAiCheckSnapshot.DocumentAiCheckSnapshotBuilder<?, ?> builder = DocumentAiCheckSnapshot.builder()
                .id(entity.getId())
                .document(toDocumentRef(entity.getDocument()))
                .blockId(entity.getBlockId())
                .content(entity.getContent());
        applyAudit(entity, builder);
        return builder.build();
    }

    public static DocumentAiCheckSnapshotJpaEntity toJpa(DocumentAiCheckSnapshot snapshot) {
        if (snapshot == null) {
            return null;
        }

        DocumentAiCheckSnapshotJpaEntity.DocumentAiCheckSnapshotJpaEntityBuilder<?, ?> builder = DocumentAiCheckSnapshotJpaEntity.builder()
                .id(snapshot.getId())
                .document(toDocumentRef(snapshot.getDocument()))
                .blockId(snapshot.getBlockId())
                .content(snapshot.getContent());
        applyAudit(snapshot, builder);
        return builder.build();
    }

    public static DocumentComment toDomain(DocumentCommentJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        DocumentComment.DocumentCommentBuilder<?, ?> builder = DocumentComment.builder()
                .id(entity.getId())
                .blockId(entity.getBlockId())
                .comment(entity.getComment())
                .document(toDocumentRef(entity.getDocument()))
                .member(MemberPersistenceMapper.toDomain(entity.getMember()));
        applyAudit(entity, builder);
        return builder.build();
    }

    public static DocumentCommentJpaEntity toJpa(DocumentComment comment) {
        if (comment == null) {
            return null;
        }

        DocumentCommentJpaEntity.DocumentCommentJpaEntityBuilder<?, ?> builder = DocumentCommentJpaEntity.builder()
                .id(comment.getId())
                .blockId(comment.getBlockId())
                .comment(comment.getComment())
                .document(toDocumentRef(comment.getDocument()))
                .member(MemberPersistenceMapper.toRef(comment.getMember()));
        applyAudit(comment, builder);
        return builder.build();
    }

    public static DocumentLog toDomain(DocumentLogJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        DocumentLog.DocumentLogBuilder<?, ?> builder = DocumentLog.builder()
                .id(entity.getId())
                .editorName(entity.getEditorName())
                .editorEmail(entity.getEditorEmail())
                .deletedBlockCount(entity.getDeletedBlockCount())
                .createdBlockCount(entity.getCreatedBlockCount())
                .document(toDocumentRef(entity.getDocument()))
                .version(entity.getVersion());
        applyAudit(entity, builder);
        return builder.build();
    }

    public static DocumentLogJpaEntity toJpa(DocumentLog log) {
        if (log == null) {
            return null;
        }

        DocumentLogJpaEntity.DocumentLogJpaEntityBuilder<?, ?> builder = DocumentLogJpaEntity.builder()
                .id(log.getId())
                .editorName(log.getEditorName())
                .editorEmail(log.getEditorEmail())
                .deletedBlockCount(log.getDeletedBlockCount())
                .createdBlockCount(log.getCreatedBlockCount())
                .document(toDocumentRef(log.getDocument()))
                .version(log.getVersion());
        applyAudit(log, builder);
        return builder.build();
    }

    public static EvaluatorReview toDomain(EvaluatorReviewJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        EvaluatorReview.EvaluatorReviewBuilder<?, ?> builder = EvaluatorReview.builder()
                .id(entity.getId())
                .scoreFeasibility(entity.getScoreFeasibility())
                .scoreDifferentiation(entity.getScoreDifferentiation())
                .scoreFinancial(entity.getScoreFinancial())
                .comment(entity.getComment())
                .scoreTotal(entity.getScoreTotal())
                .document(toDocumentRef(entity.getDocument()))
                .reviewer(MemberPersistenceMapper.toDomain(entity.getReviewer()))
                .version(entity.getVersion());
        applyAudit(entity, builder);
        return builder.build();
    }

    public static EvaluatorReviewJpaEntity toJpa(EvaluatorReview review) {
        if (review == null) {
            return null;
        }

        EvaluatorReviewJpaEntity.EvaluatorReviewJpaEntityBuilder<?, ?> builder = EvaluatorReviewJpaEntity.builder()
                .id(review.getId())
                .scoreFeasibility(review.getScoreFeasibility())
                .scoreDifferentiation(review.getScoreDifferentiation())
                .scoreFinancial(review.getScoreFinancial())
                .comment(review.getComment())
                .scoreTotal(review.getScoreTotal())
                .document(toDocumentRef(review.getDocument()))
                .reviewer(MemberPersistenceMapper.toRef(review.getReviewer()))
                .version(review.getVersion());
        applyAudit(review, builder);
        return builder.build();
    }

    public static DocumentJpaEntity toDocumentRef(Document document) {
        if (document == null || document.getId() == null) {
            return null;
        }

        return DocumentJpaEntity.builder()
                .id(document.getId())
                .version(document.getVersion())
                .build();
    }

    private static Document toDocumentRef(DocumentJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return Document.builder()
                .id(entity.getId())
                .version(entity.getVersion())
                .build();
    }

    private static void applyAudit(
            BaseJpaEntity source,
            AuditableDomainEntity.AuditableDomainEntityBuilder<?, ?> builder
    ) {
        builder.createdAt(source.getCreatedAt());
        builder.updatedAt(source.getUpdatedAt());
    }

    private static void applyAudit(
            AuditableDomainEntity source,
            BaseJpaEntity.BaseJpaEntityBuilder<?, ?> builder
    ) {
        builder.createdAt(source.getCreatedAt());
        builder.updatedAt(source.getUpdatedAt());
    }
}
