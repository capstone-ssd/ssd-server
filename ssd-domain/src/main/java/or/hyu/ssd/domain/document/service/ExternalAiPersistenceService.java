package or.hyu.ssd.domain.document.service;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.domain.document.controller.dto.ExternalAiDocumentCheckResponse;
import or.hyu.ssd.domain.document.controller.dto.ExternalAiEvaluationCardResponse;
import or.hyu.ssd.domain.document.controller.dto.ExternalAiEvaluationMetricResponse;
import or.hyu.ssd.domain.document.controller.dto.ExternalAiKeywordResponse;
import or.hyu.ssd.domain.document.controller.dto.ExternalAiSummaryResponse;
import or.hyu.ssd.domain.document.entity.Document;
import or.hyu.ssd.domain.document.entity.DocumentAiCheckSnapshot;
import or.hyu.ssd.domain.document.entity.DocumentParagraph;
import or.hyu.ssd.domain.document.repository.DocumentAiCheckSnapshotRepository;
import or.hyu.ssd.domain.document.repository.DocumentRepository;
import or.hyu.ssd.global.api.ErrorCode;
import or.hyu.ssd.global.api.handler.DomainException;
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

    public ExternalAiEvaluationCardResponse saveEvaluation(
            Long documentId,
            ExternalAiEvaluationMetricResponse problemRecognition,
            ExternalAiEvaluationMetricResponse feasibility,
            ExternalAiEvaluationMetricResponse growthStrategy,
            ExternalAiEvaluationMetricResponse businessModel,
            ExternalAiEvaluationMetricResponse teamComposition,
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
        return ExternalAiEvaluationCardResponse.of(
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

    public ExternalAiSummaryResponse saveSummary(Long documentId, String summary, String shortSummary) {
        Document doc = getDocument(documentId);
        doc.updateSummary(summary, shortSummary);
        return ExternalAiSummaryResponse.of(doc.getId(), doc.getSummary(), doc.getShortSummary());
    }

    public ExternalAiKeywordResponse saveKeyword(Long documentId, String keyword) {
        Document doc = getDocument(documentId);
        doc.updateKeywords(keyword);
        return ExternalAiKeywordResponse.of(doc.getId(), doc.getKeywords());
    }

    public ExternalAiDocumentCheckResponse mergeChecklist(
            Long documentId,
            List<Integer> changedBlockIds,
            Map<String, Boolean> checkList,
            List<DocumentParagraph> currentParagraphs
    ) {
        Document doc = getDocument(documentId);
        doc.mergeExternalChecklist(checkList);
        refreshAiCheckSnapshots(doc, currentParagraphs);
        return ExternalAiDocumentCheckResponse.of(doc.getId(), changedBlockIds, doc.getExternalChecklistSnapshot());
    }

    private Document getDocument(Long documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new DomainException(ErrorCode.DOCUMENT_NOT_FOUND));
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
            ExternalAiEvaluationMetricResponse problemRecognition,
            ExternalAiEvaluationMetricResponse feasibility,
            ExternalAiEvaluationMetricResponse growthStrategy,
            ExternalAiEvaluationMetricResponse businessModel,
            ExternalAiEvaluationMetricResponse teamComposition,
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

    private void appendMetric(StringBuilder builder, ExternalAiEvaluationMetricResponse metric) {
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
