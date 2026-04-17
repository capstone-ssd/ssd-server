package or.hyu.ssd.infra.persistence.document.mapper;

import or.hyu.ssd.document.domain.entity.*;
import or.hyu.ssd.infra.persistence.base.BaseJpaEntity;
import or.hyu.ssd.infra.persistence.document.entity.*;
import or.hyu.ssd.infra.persistence.member.mapper.MemberPersistenceMapper;
import or.hyu.ssd.member.domain.entity.Member;
import or.hyu.ssd.shared.domain.AuditableDomainEntity;

import java.util.IdentityHashMap;
import java.util.Map;

public final class DocumentPersistenceMapper {

    private DocumentPersistenceMapper() {
    }

    public static Document toDomain(DocumentJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        Document document = new Document();
        copyAudit(entity, document);
        document.setId(entity.getId());
        document.setTitle(entity.getTitle());
        document.setContent(entity.getContent());
        document.setFolder(toDomain(entity.getFolder()));
        document.setBookmark(entity.isBookmark());
        document.setSummary(entity.getSummary());
        document.setShortSummary(entity.getShortSummary());
        document.setDetails(entity.getDetails());
        document.setEvaluation(entity.getEvaluation());
        document.setKeywords(entity.getKeywords());
        document.setExternalAiTotalScore(entity.getExternalAiTotalScore());
        document.setExternalAiProblemRecognitionScore(entity.getExternalAiProblemRecognitionScore());
        document.setExternalAiProblemRecognitionReview(entity.getExternalAiProblemRecognitionReview());
        document.setExternalAiFeasibilityScore(entity.getExternalAiFeasibilityScore());
        document.setExternalAiFeasibilityReview(entity.getExternalAiFeasibilityReview());
        document.setExternalAiGrowthStrategyScore(entity.getExternalAiGrowthStrategyScore());
        document.setExternalAiGrowthStrategyReview(entity.getExternalAiGrowthStrategyReview());
        document.setExternalAiBusinessModelScore(entity.getExternalAiBusinessModelScore());
        document.setExternalAiBusinessModelReview(entity.getExternalAiBusinessModelReview());
        document.setExternalAiTeamCompositionScore(entity.getExternalAiTeamCompositionScore());
        document.setExternalAiTeamCompositionReview(entity.getExternalAiTeamCompositionReview());
        document.setChecklistDifferentiationIsClear(entity.isChecklistDifferentiationIsClear());
        document.setChecklistDifferentiationIsRealistic(entity.isChecklistDifferentiationIsRealistic());
        document.setChecklistTargetSpecificAdvantage(entity.isChecklistTargetSpecificAdvantage());
        document.setChecklistEntryBarrierExists(entity.isChecklistEntryBarrierExists());
        document.setChecklistProblemIsClear(entity.isChecklistProblemIsClear());
        document.setChecklistProblemIsReal(entity.isChecklistProblemIsReal());
        document.setChecklistTargetAndContextAreSpecific(entity.isChecklistTargetAndContextAreSpecific());
        document.setChecklistExistingSolutionHasLimits(entity.isChecklistExistingSolutionHasLimits());
        document.setChecklistMarketDefinitionIsCorrect(entity.isChecklistMarketDefinitionIsCorrect());
        document.setChecklistMarketSizeIsRealistic(entity.isChecklistMarketSizeIsRealistic());
        document.setChecklistWillingnessToPayIsClear(entity.isChecklistWillingnessToPayIsClear());
        document.setChecklistRevenueModelIsClear(entity.isChecklistRevenueModelIsClear());
        document.setChecklistProblemFounderFit(entity.isChecklistProblemFounderFit());
        document.setChecklistExperienceAlignment(entity.isChecklistExperienceAlignment());
        document.setChecklistTeamStructureIsClear(entity.isChecklistTeamStructureIsClear());
        document.setChecklistCapabilityGapPlanExists(entity.isChecklistCapabilityGapPlanExists());
        document.setMember(MemberPersistenceMapper.toDomain(entity.getMember()));
        document.setReviewFeasibilityAvg(entity.getReviewFeasibilityAvg());
        document.setReviewDifferentiationAvg(entity.getReviewDifferentiationAvg());
        document.setReviewFinancialAvg(entity.getReviewFinancialAvg());
        document.setReviewTotalAvg(entity.getReviewTotalAvg());
        document.setReviewCount(entity.getReviewCount());
        document.setVersion(entity.getVersion());
        return document;
    }

    public static DocumentJpaEntity toJpa(Document document) {
        if (document == null) {
            return null;
        }

        DocumentJpaEntity entity = new DocumentJpaEntity();
        copyAudit(document, entity);
        entity.setId(document.getId());
        entity.setTitle(document.getTitle());
        entity.setContent(document.getContent());
        entity.setFolder(toFolderRef(document.getFolder()));
        entity.setBookmark(document.isBookmark());
        entity.setSummary(document.getSummary());
        entity.setShortSummary(document.getShortSummary());
        entity.setDetails(document.getDetails());
        entity.setEvaluation(document.getEvaluation());
        entity.setKeywords(document.getKeywords());
        entity.setExternalAiTotalScore(document.getExternalAiTotalScore());
        entity.setExternalAiProblemRecognitionScore(document.getExternalAiProblemRecognitionScore());
        entity.setExternalAiProblemRecognitionReview(document.getExternalAiProblemRecognitionReview());
        entity.setExternalAiFeasibilityScore(document.getExternalAiFeasibilityScore());
        entity.setExternalAiFeasibilityReview(document.getExternalAiFeasibilityReview());
        entity.setExternalAiGrowthStrategyScore(document.getExternalAiGrowthStrategyScore());
        entity.setExternalAiGrowthStrategyReview(document.getExternalAiGrowthStrategyReview());
        entity.setExternalAiBusinessModelScore(document.getExternalAiBusinessModelScore());
        entity.setExternalAiBusinessModelReview(document.getExternalAiBusinessModelReview());
        entity.setExternalAiTeamCompositionScore(document.getExternalAiTeamCompositionScore());
        entity.setExternalAiTeamCompositionReview(document.getExternalAiTeamCompositionReview());
        entity.setChecklistDifferentiationIsClear(document.isChecklistDifferentiationIsClear());
        entity.setChecklistDifferentiationIsRealistic(document.isChecklistDifferentiationIsRealistic());
        entity.setChecklistTargetSpecificAdvantage(document.isChecklistTargetSpecificAdvantage());
        entity.setChecklistEntryBarrierExists(document.isChecklistEntryBarrierExists());
        entity.setChecklistProblemIsClear(document.isChecklistProblemIsClear());
        entity.setChecklistProblemIsReal(document.isChecklistProblemIsReal());
        entity.setChecklistTargetAndContextAreSpecific(document.isChecklistTargetAndContextAreSpecific());
        entity.setChecklistExistingSolutionHasLimits(document.isChecklistExistingSolutionHasLimits());
        entity.setChecklistMarketDefinitionIsCorrect(document.isChecklistMarketDefinitionIsCorrect());
        entity.setChecklistMarketSizeIsRealistic(document.isChecklistMarketSizeIsRealistic());
        entity.setChecklistWillingnessToPayIsClear(document.isChecklistWillingnessToPayIsClear());
        entity.setChecklistRevenueModelIsClear(document.isChecklistRevenueModelIsClear());
        entity.setChecklistProblemFounderFit(document.isChecklistProblemFounderFit());
        entity.setChecklistExperienceAlignment(document.isChecklistExperienceAlignment());
        entity.setChecklistTeamStructureIsClear(document.isChecklistTeamStructureIsClear());
        entity.setChecklistCapabilityGapPlanExists(document.isChecklistCapabilityGapPlanExists());
        entity.setMember(MemberPersistenceMapper.toRef(document.getMember()));
        entity.setReviewFeasibilityAvg(document.getReviewFeasibilityAvg());
        entity.setReviewDifferentiationAvg(document.getReviewDifferentiationAvg());
        entity.setReviewFinancialAvg(document.getReviewFinancialAvg());
        entity.setReviewTotalAvg(document.getReviewTotalAvg());
        entity.setReviewCount(document.getReviewCount());
        entity.setVersion(document.getVersion());
        return entity;
    }

    public static Folder toDomain(FolderJpaEntity entity) {
        return toDomain(entity, new IdentityHashMap<>());
    }

    public static FolderJpaEntity toJpa(Folder folder) {
        if (folder == null) {
            return null;
        }
        FolderJpaEntity entity = new FolderJpaEntity();
        copyAudit(folder, entity);
        entity.setId(folder.getId());
        entity.setName(folder.getName());
        entity.setColor(folder.getColor());
        entity.setParent(toFolderRef(folder.getParent()));
        entity.setMember(MemberPersistenceMapper.toRef(folder.getMember()));
        entity.setVersion(folder.getVersion());
        return entity;
    }

    public static FolderJpaEntity toFolderRef(Folder folder) {
        if (folder == null || folder.getId() == null) {
            return null;
        }
        FolderJpaEntity entity = new FolderJpaEntity();
        entity.setId(folder.getId());
        return entity;
    }

    public static DocumentParagraph toDomain(DocumentParagraphJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        DocumentParagraph paragraph = new DocumentParagraph();
        copyAudit(entity, paragraph);
        paragraph.setId(entity.getId());
        paragraph.setContent(entity.getContent());
        paragraph.setType(entity.getType());
        paragraph.setRole(entity.getRole());
        paragraph.setPageNumber(entity.getPageNumber());
        paragraph.setBlockId(entity.getBlockId());
        paragraph.setDocument(toDocumentRef(entity.getDocument()));
        paragraph.setVersion(entity.getVersion());
        return paragraph;
    }

    public static DocumentParagraphJpaEntity toJpa(DocumentParagraph paragraph) {
        if (paragraph == null) {
            return null;
        }
        DocumentParagraphJpaEntity entity = new DocumentParagraphJpaEntity();
        copyAudit(paragraph, entity);
        entity.setId(paragraph.getId());
        entity.setContent(paragraph.getContent());
        entity.setType(paragraph.getType());
        entity.setRole(paragraph.getRole());
        entity.setPageNumber(paragraph.getPageNumber());
        entity.setBlockId(paragraph.getBlockId());
        entity.setDocument(toDocumentRef(paragraph.getDocument()));
        entity.setVersion(paragraph.getVersion());
        return entity;
    }

    public static CheckList toDomain(CheckListJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        CheckList checkList = new CheckList();
        copyAudit(entity, checkList);
        checkList.setId(entity.getId());
        checkList.setContent(entity.getContent());
        checkList.setChecked(entity.isChecked());
        checkList.setDocument(toDocumentRef(entity.getDocument()));
        checkList.setVersion(entity.getVersion());
        return checkList;
    }

    public static CheckListJpaEntity toJpa(CheckList checkList) {
        if (checkList == null) {
            return null;
        }
        CheckListJpaEntity entity = new CheckListJpaEntity();
        copyAudit(checkList, entity);
        entity.setId(checkList.getId());
        entity.setContent(checkList.getContent());
        entity.setChecked(checkList.isChecked());
        entity.setDocument(toDocumentRef(checkList.getDocument()));
        entity.setVersion(checkList.getVersion());
        return entity;
    }

    public static EvaluatorCheckList toDomain(EvaluatorCheckListJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        EvaluatorCheckList checkList = new EvaluatorCheckList();
        copyAudit(entity, checkList);
        checkList.setId(entity.getId());
        checkList.setContent(entity.getContent());
        checkList.setChecked(entity.isChecked());
        checkList.setDocument(toDocumentRef(entity.getDocument()));
        checkList.setVersion(entity.getVersion());
        return checkList;
    }

    public static EvaluatorCheckListJpaEntity toJpa(EvaluatorCheckList checkList) {
        if (checkList == null) {
            return null;
        }
        EvaluatorCheckListJpaEntity entity = new EvaluatorCheckListJpaEntity();
        copyAudit(checkList, entity);
        entity.setId(checkList.getId());
        entity.setContent(checkList.getContent());
        entity.setChecked(checkList.isChecked());
        entity.setDocument(toDocumentRef(checkList.getDocument()));
        entity.setVersion(checkList.getVersion());
        return entity;
    }

    public static DocumentAiCheckSnapshot toDomain(DocumentAiCheckSnapshotJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        DocumentAiCheckSnapshot snapshot = new DocumentAiCheckSnapshot();
        copyAudit(entity, snapshot);
        snapshot.setId(entity.getId());
        snapshot.setDocument(toDocumentRef(entity.getDocument()));
        snapshot.setBlockId(entity.getBlockId());
        snapshot.setContent(entity.getContent());
        return snapshot;
    }

    public static DocumentAiCheckSnapshotJpaEntity toJpa(DocumentAiCheckSnapshot snapshot) {
        if (snapshot == null) {
            return null;
        }
        DocumentAiCheckSnapshotJpaEntity entity = new DocumentAiCheckSnapshotJpaEntity();
        copyAudit(snapshot, entity);
        entity.setId(snapshot.getId());
        entity.setDocument(toDocumentRef(snapshot.getDocument()));
        entity.setBlockId(snapshot.getBlockId());
        entity.setContent(snapshot.getContent());
        return entity;
    }

    public static DocumentComment toDomain(DocumentCommentJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        DocumentComment comment = new DocumentComment();
        copyAudit(entity, comment);
        comment.setId(entity.getId());
        comment.setBlockId(entity.getBlockId());
        comment.setComment(entity.getComment());
        comment.setDocument(toDocumentRef(entity.getDocument()));
        comment.setMember(MemberPersistenceMapper.toDomain(entity.getMember()));
        return comment;
    }

    public static DocumentCommentJpaEntity toJpa(DocumentComment comment) {
        if (comment == null) {
            return null;
        }
        DocumentCommentJpaEntity entity = new DocumentCommentJpaEntity();
        copyAudit(comment, entity);
        entity.setId(comment.getId());
        entity.setBlockId(comment.getBlockId());
        entity.setComment(comment.getComment());
        entity.setDocument(toDocumentRef(comment.getDocument()));
        entity.setMember(MemberPersistenceMapper.toRef(comment.getMember()));
        return entity;
    }

    public static DocumentLog toDomain(DocumentLogJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        DocumentLog log = new DocumentLog();
        copyAudit(entity, log);
        log.setId(entity.getId());
        log.setEditorName(entity.getEditorName());
        log.setEditorEmail(entity.getEditorEmail());
        log.setDeletedBlockCount(entity.getDeletedBlockCount());
        log.setCreatedBlockCount(entity.getCreatedBlockCount());
        log.setDocument(toDocumentRef(entity.getDocument()));
        log.setVersion(entity.getVersion());
        return log;
    }

    public static DocumentLogJpaEntity toJpa(DocumentLog log) {
        if (log == null) {
            return null;
        }
        DocumentLogJpaEntity entity = new DocumentLogJpaEntity();
        copyAudit(log, entity);
        entity.setId(log.getId());
        entity.setEditorName(log.getEditorName());
        entity.setEditorEmail(log.getEditorEmail());
        entity.setDeletedBlockCount(log.getDeletedBlockCount());
        entity.setCreatedBlockCount(log.getCreatedBlockCount());
        entity.setDocument(toDocumentRef(log.getDocument()));
        entity.setVersion(log.getVersion());
        return entity;
    }

    public static EvaluatorReview toDomain(EvaluatorReviewJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        EvaluatorReview review = new EvaluatorReview();
        copyAudit(entity, review);
        review.setId(entity.getId());
        review.setScoreFeasibility(entity.getScoreFeasibility());
        review.setScoreDifferentiation(entity.getScoreDifferentiation());
        review.setScoreFinancial(entity.getScoreFinancial());
        review.setComment(entity.getComment());
        review.setScoreTotal(entity.getScoreTotal());
        review.setDocument(toDocumentRef(entity.getDocument()));
        review.setReviewer(MemberPersistenceMapper.toDomain(entity.getReviewer()));
        review.setVersion(entity.getVersion());
        return review;
    }

    public static EvaluatorReviewJpaEntity toJpa(EvaluatorReview review) {
        if (review == null) {
            return null;
        }
        EvaluatorReviewJpaEntity entity = new EvaluatorReviewJpaEntity();
        copyAudit(review, entity);
        entity.setId(review.getId());
        entity.setScoreFeasibility(review.getScoreFeasibility());
        entity.setScoreDifferentiation(review.getScoreDifferentiation());
        entity.setScoreFinancial(review.getScoreFinancial());
        entity.setComment(review.getComment());
        entity.setScoreTotal(review.getScoreTotal());
        entity.setDocument(toDocumentRef(review.getDocument()));
        entity.setReviewer(MemberPersistenceMapper.toRef(review.getReviewer()));
        entity.setVersion(review.getVersion());
        return entity;
    }

    public static DocumentJpaEntity toDocumentRef(Document document) {
        if (document == null || document.getId() == null) {
            return null;
        }
        DocumentJpaEntity entity = new DocumentJpaEntity();
        entity.setId(document.getId());
        return entity;
    }

    private static Document toDocumentRef(DocumentJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        Document document = new Document();
        document.setId(entity.getId());
        return document;
    }

    private static Folder toDomain(FolderJpaEntity entity, Map<FolderJpaEntity, Folder> cache) {
        if (entity == null) {
            return null;
        }
        Folder cached = cache.get(entity);
        if (cached != null) {
            return cached;
        }

        Folder folder = new Folder();
        cache.put(entity, folder);
        copyAudit(entity, folder);
        folder.setId(entity.getId());
        folder.setName(entity.getName());
        folder.setColor(entity.getColor());
        folder.setParent(toDomain(entity.getParent(), cache));
        folder.setMember(MemberPersistenceMapper.toDomain(entity.getMember()));
        folder.setVersion(entity.getVersion());
        return folder;
    }

    private static void copyAudit(BaseJpaEntity source, AuditableDomainEntity target) {
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
    }

    private static void copyAudit(AuditableDomainEntity source, BaseJpaEntity target) {
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
    }
}
