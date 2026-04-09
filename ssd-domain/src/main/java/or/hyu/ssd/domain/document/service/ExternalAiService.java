package or.hyu.ssd.domain.document.service;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.domain.document.client.ExternalAiPort;
import or.hyu.ssd.domain.document.client.dto.ExternalCheckNewTextBlockRequest;
import or.hyu.ssd.domain.document.client.dto.ExternalCheckNewTextRequest;
import or.hyu.ssd.domain.document.client.dto.ExternalCheckNewTextResponse;
import or.hyu.ssd.domain.document.client.dto.ExternalEvaluationRequest;
import or.hyu.ssd.domain.document.client.dto.ExternalEvaluationResponse;
import or.hyu.ssd.domain.document.client.dto.ExternalEvaluatorMetricResponse;
import or.hyu.ssd.domain.document.client.dto.ExternalSummarizationBasicRequest;
import or.hyu.ssd.domain.document.client.dto.ExternalSummarizationBasicResponse;
import or.hyu.ssd.domain.document.client.dto.ExternalSummarizationKeywordRequest;
import or.hyu.ssd.domain.document.client.dto.ExternalSummarizationKeywordResponse;
import or.hyu.ssd.domain.document.entity.Document;
import or.hyu.ssd.domain.document.entity.DocumentAiCheckSnapshot;
import or.hyu.ssd.domain.document.entity.DocumentParagraph;
import or.hyu.ssd.domain.document.repository.DocumentAiCheckSnapshotRepository;
import or.hyu.ssd.domain.document.repository.DocumentParagraphRepository;
import or.hyu.ssd.domain.document.repository.DocumentRepository;
import or.hyu.ssd.domain.document.usecase.command.ExternalDocumentIdCommand;
import or.hyu.ssd.domain.document.usecase.result.ExternalAiChecklistResult;
import or.hyu.ssd.domain.document.usecase.result.ExternalAiDocumentCheckResult;
import or.hyu.ssd.domain.document.usecase.result.ExternalAiEvaluationCardResult;
import or.hyu.ssd.domain.document.usecase.result.ExternalAiEvaluationMetricResult;
import or.hyu.ssd.domain.document.usecase.result.ExternalAiKeywordResult;
import or.hyu.ssd.domain.document.usecase.result.ExternalAiSummaryResult;
import or.hyu.ssd.domain.member.service.CustomUserDetails;
import or.hyu.ssd.global.api.ErrorCode;
import or.hyu.ssd.global.api.handler.DocumentException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ExternalAiService {

    private final ExternalAiPort externalAiPort;
    private final ExternalAiPersistenceService externalAiPersistenceService;
    private final DocumentRepository documentRepository;
    private final DocumentAiCheckSnapshotRepository documentAiCheckSnapshotRepository;
    private final DocumentParagraphRepository documentParagraphRepository;

    public ExternalAiEvaluationCardResult evaluate(ExternalDocumentIdCommand command, CustomUserDetails user) {
        Document doc = getOwnedDocument(command.docId(), user);
        List<DocumentParagraph> currentParagraphs = documentParagraphRepository.findParagraphBlocks(doc);

        ExternalEvaluationResponse response = externalAiPort.evaluate(new ExternalEvaluationRequest(
                String.valueOf(doc.getId()),
                doc.getContent()
        ));

        ExternalAiEvaluationMetricResult problemRecognition =
                toMetric("문제 인식", response.evaluationReport() != null ? response.evaluationReport().problemEvaluator() : null);
        ExternalAiEvaluationMetricResult feasibility =
                toMetric("실현 가능성", response.evaluationReport() != null ? response.evaluationReport().solEvaluator() : null);
        ExternalAiEvaluationMetricResult growthStrategy =
                toMetric("성장 전략", response.evaluationReport() != null ? response.evaluationReport().scaleUpEvaluator() : null);
        ExternalAiEvaluationMetricResult businessModel =
                toMetric("Business Model", response.evaluationReport() != null ? response.evaluationReport().businessModelEvaluator() : null);
        ExternalAiEvaluationMetricResult teamComposition =
                toMetric("팀 구성", response.evaluationReport() != null ? response.evaluationReport().teamEvaluator() : null);

        Integer totalScore = calculateAverageScore(
                problemRecognition.score(),
                feasibility.score(),
                growthStrategy.score(),
                businessModel.score(),
                teamComposition.score()
        );

        return externalAiPersistenceService.saveEvaluation(
                doc.getId(),
                problemRecognition,
                feasibility,
                growthStrategy,
                businessModel,
                teamComposition,
                totalScore,
                response.checkList(),
                currentParagraphs
        );
    }

    public ExternalAiSummaryResult summarizeBasic(ExternalDocumentIdCommand command, CustomUserDetails user) {
        Document doc = getOwnedDocument(command.docId(), user);
        ExternalSummarizationBasicResponse response = externalAiPort.summarizeBasic(new ExternalSummarizationBasicRequest(
                String.valueOf(doc.getId()),
                doc.getContent()
        ));

        String summary = firstNonBlank(response.summary(), response.small());
        String shortSummary = normalize(response.small());
        return externalAiPersistenceService.saveSummary(doc.getId(), summary, shortSummary);
    }

    public ExternalAiKeywordResult summarizeKeyword(ExternalDocumentIdCommand command, CustomUserDetails user) {
        Document doc = getOwnedDocument(command.docId(), user);
        ExternalSummarizationKeywordResponse response = externalAiPort.summarizeKeyword(new ExternalSummarizationKeywordRequest(
                String.valueOf(doc.getId()),
                doc.getContent()
        ));

        return externalAiPersistenceService.saveKeyword(doc.getId(), normalize(response.keyword()));
    }

    public ExternalAiDocumentCheckResult checkNewText(ExternalDocumentIdCommand command, CustomUserDetails user) {
        Document doc = getOwnedDocument(command.docId(), user);
        List<DocumentParagraph> currentParagraphs = documentParagraphRepository.findParagraphBlocks(doc);
        if (currentParagraphs.isEmpty()) {
            throw new DocumentException(ErrorCode.DOCUMENT_PARAGRAPH_NOT_FOUND);
        }

        List<DocumentParagraph> changedParagraphs = findChangedParagraphs(doc, currentParagraphs);
        if (changedParagraphs.isEmpty()) {
            return ExternalAiDocumentCheckResult.of(doc.getId(), List.of(), doc.getExternalChecklistSnapshot());
        }

        ExternalCheckNewTextResponse response = externalAiPort.checkNewText(new ExternalCheckNewTextRequest(
                String.valueOf(doc.getId()),
                changedParagraphs.stream()
                        .map(paragraph -> new ExternalCheckNewTextBlockRequest(
                                String.valueOf(paragraph.getBlockId()),
                                paragraph.getContent()
                        ))
                        .toList()
        ));

        return externalAiPersistenceService.mergeChecklist(
                doc.getId(),
                changedParagraphs.stream().map(DocumentParagraph::getBlockId).toList(),
                response.checkList(),
                currentParagraphs
        );
    }

    @Transactional(readOnly = true)
    public ExternalAiEvaluationCardResult getEvaluation(Long documentId, CustomUserDetails user) {
        Document doc = getOwnedDocument(documentId, getMemberId(user));
        return ExternalAiEvaluationCardResult.of(
                doc.getId(),
                doc.getExternalAiTotalScore(),
                ExternalAiEvaluationMetricResult.of("문제 인식", doc.getExternalAiProblemRecognitionScore(), doc.getExternalAiProblemRecognitionReview()),
                ExternalAiEvaluationMetricResult.of("실현 가능성", doc.getExternalAiFeasibilityScore(), doc.getExternalAiFeasibilityReview()),
                ExternalAiEvaluationMetricResult.of("성장 전략", doc.getExternalAiGrowthStrategyScore(), doc.getExternalAiGrowthStrategyReview()),
                ExternalAiEvaluationMetricResult.of("Business Model", doc.getExternalAiBusinessModelScore(), doc.getExternalAiBusinessModelReview()),
                ExternalAiEvaluationMetricResult.of("팀 구성", doc.getExternalAiTeamCompositionScore(), doc.getExternalAiTeamCompositionReview()),
                doc.getExternalChecklistSnapshot()
        );
    }

    @Transactional(readOnly = true)
    public ExternalAiSummaryResult getSummary(Long documentId, CustomUserDetails user) {
        Document doc = getOwnedDocument(documentId, getMemberId(user));
        return ExternalAiSummaryResult.of(doc.getId(), doc.getSummary(), doc.getShortSummary());
    }

    @Transactional(readOnly = true)
    public ExternalAiKeywordResult getKeyword(Long documentId, CustomUserDetails user) {
        Document doc = getOwnedDocument(documentId, getMemberId(user));
        return ExternalAiKeywordResult.of(doc.getId(), doc.getKeywords());
    }

    @Transactional(readOnly = true)
    public ExternalAiChecklistResult getChecklist(Long documentId, CustomUserDetails user) {
        Document doc = getOwnedDocument(documentId, getMemberId(user));
        return ExternalAiChecklistResult.of(doc.getId(), doc.getExternalChecklistSnapshot());
    }

    private Document getOwnedDocument(String rawDocId, CustomUserDetails user) {
        return getOwnedDocument(parseDocumentId(rawDocId), getMemberId(user));
    }

    private Document getOwnedDocument(Long docId, Long memberId) {
        Document doc = documentRepository.findById(docId)
                .orElseThrow(() -> new DocumentException(ErrorCode.DOCUMENT_NOT_FOUND));
        if (doc.getMember() == null || doc.getMember().getId() == null) {
            throw new DocumentException(ErrorCode.DOCUMENT_FORBIDDEN);
        }
        if (!doc.getMember().getId().equals(memberId)) {
            throw new DocumentException(ErrorCode.DOCUMENT_FORBIDDEN);
        }
        return doc;
    }

    private Long getMemberId(CustomUserDetails user) {
        if (user == null || user.getMember() == null || user.getMember().getId() == null) {
            throw new DocumentException(ErrorCode.DOCUMENT_FORBIDDEN);
        }
        return user.getMember().getId();
    }

    private Long parseDocumentId(String rawDocId) {
        try {
            Long docId = Long.parseLong(rawDocId);
            if (docId <= 0) {
                throw new NumberFormatException("doc_id must be positive");
            }
            return docId;
        } catch (Exception e) {
            throw new DocumentException(ErrorCode.REQUEST_BODY_INVALID_VALUE, "docId는 1 이상의 숫자여야 합니다");
        }
    }

    private List<DocumentParagraph> findChangedParagraphs(Document doc, List<DocumentParagraph> currentParagraphs) {
        Map<Integer, String> snapshotContentByBlockId = new HashMap<>();
        for (DocumentAiCheckSnapshot snapshot : documentAiCheckSnapshotRepository.findAllByDocument(doc)) {
            snapshotContentByBlockId.put(snapshot.getBlockId(), snapshot.getContent());
        }
        return currentParagraphs.stream()
                .filter(paragraph -> {
                    String previous = snapshotContentByBlockId.get(paragraph.getBlockId());
                    return previous == null || !Objects.equals(previous, paragraph.getContent());
                })
                .sorted(Comparator.comparingInt(DocumentParagraph::getBlockId))
                .toList();
    }

    private ExternalAiEvaluationMetricResult toMetric(String label, ExternalEvaluatorMetricResponse metric) {
        if (metric == null) {
            return ExternalAiEvaluationMetricResult.of(label, null, "");
        }
        return ExternalAiEvaluationMetricResult.of(label, toScore(metric.averageScore()), normalize(metric.finalReview()));
    }

    private Integer toScore(Double score) {
        if (score == null) {
            return null;
        }
        long rounded = Math.round(score);
        if (rounded < 0) {
            return 0;
        }
        if (rounded > 100) {
            return 100;
        }
        return (int) rounded;
    }

    private Integer calculateAverageScore(Integer... scores) {
        List<Integer> available = new ArrayList<>();
        for (Integer score : scores) {
            if (score != null) {
                available.add(score);
            }
        }
        if (available.isEmpty()) {
            return 0;
        }
        double average = available.stream().mapToInt(Integer::intValue).average().orElse(0);
        return (int) Math.round(average);
    }

    private String firstNonBlank(String first, String second) {
        String normalizedFirst = normalize(first);
        if (!normalizedFirst.isEmpty()) {
            return normalizedFirst;
        }
        return normalize(second);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
