package or.hyu.ssd.document.application.service;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.document.domain.model.Document;
import or.hyu.ssd.document.domain.model.DocumentAiCheckSnapshot;
import or.hyu.ssd.document.domain.model.DocumentParagraph;
import or.hyu.ssd.document.repository.DocumentAiCheckSnapshotRepository;
import or.hyu.ssd.document.repository.DocumentRepository;
import or.hyu.ssd.document.application.result.ExternalAiDocumentCheckResult;
import or.hyu.ssd.document.application.result.ExternalAiEvaluationCardResult;
import or.hyu.ssd.document.application.result.ExternalAiEvaluationMetricResult;
import or.hyu.ssd.document.application.result.ExternalAiKeywordResult;
import or.hyu.ssd.document.application.result.ExternalAiSummaryResult;
import or.hyu.ssd.common.exception.ErrorCode;
import or.hyu.ssd.common.exception.DocumentException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class ExternalAiPersistenceService {

    private final DocumentRepository documentRepository;
    private final DocumentAiCheckSnapshotRepository documentAiCheckSnapshotRepository;

    public ExternalAiEvaluationCardResult saveEvaluation(
            Long documentId,
            ExternalAiEvaluationMetricResult problemRecognition,
            ExternalAiEvaluationMetricResult feasibility,
            ExternalAiEvaluationMetricResult growthStrategy,
            ExternalAiEvaluationMetricResult businessModel,
            ExternalAiEvaluationMetricResult teamComposition,
            Integer totalScore,
            Map<String, Boolean> checkList,
            List<DocumentParagraph> currentParagraphs
    ) {
        Document doc = getDocument(documentId);
        doc.updateExternalEvaluationMetrics(
                totalScore,
                problemRecognition.score(),
                problemRecognition.review(),
                feasibility.score(),
                feasibility.review(),
                growthStrategy.score(),
                growthStrategy.review(),
                businessModel.score(),
                businessModel.review(),
                teamComposition.score(),
                teamComposition.review()
        );
        doc.overwriteExternalChecklist(checkList);
        refreshAiCheckSnapshots(doc, currentParagraphs);
        doc.updateEvaluation(buildEvaluationReport(
                problemRecognition,
                feasibility,
                growthStrategy,
                businessModel,
                teamComposition,
                doc.getExternalChecklistSnapshot()
        ));
        documentRepository.save(doc);
        return ExternalAiEvaluationCardResult.of(
                doc.getId(),
                doc.getExternalAiTotalScore(),
                problemRecognition,
                feasibility,
                growthStrategy,
                businessModel,
                teamComposition,
                doc.getExternalChecklistSnapshot()
        );
    }

    public ExternalAiSummaryResult saveSummary(Long documentId, String summary, String shortSummary) {
        Document doc = getDocument(documentId);
        doc.updateSummary(summary, shortSummary);
        documentRepository.save(doc);
        return ExternalAiSummaryResult.of(doc.getId(), doc.getSummary(), doc.getShortSummary());
    }

    public ExternalAiKeywordResult saveKeyword(Long documentId, String keyword) {
        Document doc = getDocument(documentId);
        doc.updateKeywords(keyword);
        documentRepository.save(doc);
        return ExternalAiKeywordResult.of(doc.getId(), doc.getKeywords());
    }

    public ExternalAiDocumentCheckResult mergeChecklist(
            Long documentId,
            List<Integer> changedBlockIds,
            Map<String, Boolean> checkList,
            List<DocumentParagraph> currentParagraphs
    ) {
        Document doc = getDocument(documentId);
        doc.mergeExternalChecklist(checkList);
        refreshAiCheckSnapshots(doc, currentParagraphs);
        documentRepository.save(doc);
        return ExternalAiDocumentCheckResult.of(doc.getId(), changedBlockIds, doc.getExternalChecklistSnapshot());
    }

    private Document getDocument(Long documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentException(ErrorCode.DOCUMENT_NOT_FOUND));
    }

    private void refreshAiCheckSnapshots(Document doc, List<DocumentParagraph> currentParagraphs) {
        documentAiCheckSnapshotRepository.deleteAllByDocument(doc);
        if (currentParagraphs == null || currentParagraphs.isEmpty()) {
            return;
        }
        List<DocumentAiCheckSnapshot> snapshots = currentParagraphs.stream()
                .map(paragraph -> DocumentAiCheckSnapshot.of(doc, paragraph.getBlockId(), paragraph.getContent()))
                .toList();
        documentAiCheckSnapshotRepository.saveAll(snapshots);
    }

    private String buildEvaluationReport(
            ExternalAiEvaluationMetricResult problemRecognition,
            ExternalAiEvaluationMetricResult feasibility,
            ExternalAiEvaluationMetricResult growthStrategy,
            ExternalAiEvaluationMetricResult businessModel,
            ExternalAiEvaluationMetricResult teamComposition,
            Map<String, Boolean> checkList
    ) {
        StringBuilder builder = new StringBuilder();
        appendMetric(builder, problemRecognition);
        appendMetric(builder, feasibility);
        appendMetric(builder, growthStrategy);
        appendMetric(builder, businessModel);
        appendMetric(builder, teamComposition);

        if (checkList != null && !checkList.isEmpty()) {
            if (!builder.isEmpty()) {
                builder.append("\n\n");
            }
            builder.append("## 체크리스트\n");
            checkList.forEach((key, value) ->
                    builder.append("- ").append(key).append(": ").append(Boolean.TRUE.equals(value) ? "충족" : "미충족").append("\n")
            );
        }
        return builder.toString().trim();
    }

    private void appendMetric(StringBuilder builder, ExternalAiEvaluationMetricResult metric) {
        if (metric == null) {
            return;
        }
        if (!builder.isEmpty()) {
            builder.append("\n\n");
        }
        builder.append("## ").append(metric.label()).append("\n");
        builder.append("- 점수: ").append(metric.score() == null ? "-" : metric.score()).append("\n");
        if (metric.review() != null && !metric.review().isBlank()) {
            builder.append(metric.review());
        }
    }
}
