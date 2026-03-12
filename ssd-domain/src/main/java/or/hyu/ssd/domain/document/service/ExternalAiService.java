package or.hyu.ssd.domain.document.service;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.domain.document.client.ExternalAiPort;
import or.hyu.ssd.domain.document.controller.dto.ExternalAiBlockCheckRequest;
import or.hyu.ssd.domain.document.controller.dto.ExternalAiBlockCheckResponse;
import or.hyu.ssd.domain.document.controller.dto.ExternalAiEvaluationCardResponse;
import or.hyu.ssd.domain.document.controller.dto.ExternalAiEvaluationMetricResponse;
import or.hyu.ssd.domain.document.controller.dto.ExternalAiKeywordResponse;
import or.hyu.ssd.domain.document.controller.dto.ExternalAiSummaryResponse;
import or.hyu.ssd.domain.document.controller.dto.ExternalCheckNewTextRequest;
import or.hyu.ssd.domain.document.controller.dto.ExternalCheckNewTextResponse;
import or.hyu.ssd.domain.document.controller.dto.ExternalDocumentIdRequest;
import or.hyu.ssd.domain.document.controller.dto.ExternalEvaluationRequest;
import or.hyu.ssd.domain.document.controller.dto.ExternalEvaluationResponse;
import or.hyu.ssd.domain.document.controller.dto.ExternalEvaluatorMetricResponse;
import or.hyu.ssd.domain.document.controller.dto.ExternalSummarizationBasicRequest;
import or.hyu.ssd.domain.document.controller.dto.ExternalSummarizationBasicResponse;
import or.hyu.ssd.domain.document.controller.dto.ExternalSummarizationKeywordRequest;
import or.hyu.ssd.domain.document.controller.dto.ExternalSummarizationKeywordResponse;
import or.hyu.ssd.domain.document.entity.Document;
import or.hyu.ssd.domain.document.entity.DocumentParagraph;
import or.hyu.ssd.domain.document.repository.DocumentParagraphRepository;
import or.hyu.ssd.domain.document.repository.DocumentRepository;
import or.hyu.ssd.domain.member.service.CustomUserDetails;
import or.hyu.ssd.global.api.ErrorCode;
import or.hyu.ssd.global.api.handler.UserExceptionHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class ExternalAiService {

    private final ExternalAiPort externalAiPort;
    private final DocumentRepository documentRepository;
    private final DocumentParagraphRepository documentParagraphRepository;

    public ExternalAiEvaluationCardResponse evaluate(ExternalDocumentIdRequest request, CustomUserDetails user) {
        Document doc = getOwnedDocument(request.docId(), user);
        ExternalEvaluationRequest externalRequest = new ExternalEvaluationRequest(
                String.valueOf(doc.getId()),
                doc.getContent()
        );
        ExternalEvaluationResponse response = externalAiPort.evaluate(externalRequest);

        ExternalAiEvaluationMetricResponse problemRecognition =
                toMetric("문제 인식", response.evaluationReport() != null ? response.evaluationReport().problemEvaluator() : null);
        ExternalAiEvaluationMetricResponse feasibility =
                toMetric("실현 가능성", response.evaluationReport() != null ? response.evaluationReport().solEvaluator() : null);
        ExternalAiEvaluationMetricResponse growthStrategy =
                toMetric("성장 전략", response.evaluationReport() != null ? response.evaluationReport().scaleUpEvaluator() : null);
        ExternalAiEvaluationMetricResponse businessModel =
                toMetric("Business Model", response.evaluationReport() != null ? response.evaluationReport().businessModelEvaluator() : null);
        ExternalAiEvaluationMetricResponse teamComposition =
                toMetric("팀 구성", response.evaluationReport() != null ? response.evaluationReport().teamEvaluator() : null);

        Integer totalScore = calculateAverageScore(
                problemRecognition.score(),
                feasibility.score(),
                growthStrategy.score(),
                businessModel.score(),
                teamComposition.score()
        );

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
        doc.overwriteExternalChecklist(response.checkList());
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
                totalScore,
                problemRecognition,
                feasibility,
                growthStrategy,
                businessModel,
                teamComposition,
                doc.getExternalChecklistSnapshot()
        );
    }

    public ExternalAiSummaryResponse summarizeBasic(ExternalDocumentIdRequest request, CustomUserDetails user) {
        Document doc = getOwnedDocument(request.docId(), user);
        ExternalSummarizationBasicRequest externalRequest = new ExternalSummarizationBasicRequest(
                String.valueOf(doc.getId()),
                doc.getContent()
        );
        ExternalSummarizationBasicResponse response = externalAiPort.summarizeBasic(externalRequest);

        String summary = firstNonBlank(response.summary(), response.small());
        doc.updateSummary(summary);

        return ExternalAiSummaryResponse.of(doc.getId(), summary, response.small());
    }

    public ExternalAiKeywordResponse summarizeKeyword(ExternalDocumentIdRequest request, CustomUserDetails user) {
        Document doc = getOwnedDocument(request.docId(), user);
        ExternalSummarizationKeywordRequest externalRequest = new ExternalSummarizationKeywordRequest(
                String.valueOf(doc.getId()),
                doc.getContent()
        );
        ExternalSummarizationKeywordResponse response = externalAiPort.summarizeKeyword(externalRequest);
        String keyword = normalize(response.keyword());
        doc.updateKeywords(keyword);
        return ExternalAiKeywordResponse.of(doc.getId(), keyword);
    }

    public ExternalAiBlockCheckResponse checkNewText(ExternalAiBlockCheckRequest request, CustomUserDetails user) {
        Document doc = getOwnedDocument(request.docId(), user);
        int blockId = parseBlockId(request.blockId());
        DocumentParagraph paragraph = documentParagraphRepository.findByDocumentAndBlockId(doc, blockId)
                .orElseThrow(() -> new UserExceptionHandler(ErrorCode.DOCUMENT_PARAGRAPH_NOT_FOUND));

        ExternalCheckNewTextRequest externalRequest = new ExternalCheckNewTextRequest(
                String.valueOf(paragraph.getBlockId()),
                request.block()
        );
        ExternalCheckNewTextResponse response = externalAiPort.checkNewText(externalRequest);
        doc.mergeExternalChecklist(response.checkList());
        return ExternalAiBlockCheckResponse.of(blockId, doc.getExternalChecklistSnapshot());
    }

    private Document getOwnedDocument(String rawDocId, CustomUserDetails user) {
        Long docId = parseDocumentId(rawDocId);
        Document doc = documentRepository.findById(docId)
                .orElseThrow(() -> new UserExceptionHandler(ErrorCode.DOCUMENT_NOT_FOUND));
        Long memberId = getMemberId(user);
        if (doc.getMember() == null || doc.getMember().getId() == null) {
            throw new UserExceptionHandler(ErrorCode.DOCUMENT_FORBIDDEN);
        }
        if (!doc.getMember().getId().equals(memberId)) {
            throw new UserExceptionHandler(ErrorCode.DOCUMENT_FORBIDDEN);
        }
        return doc;
    }

    private Long getMemberId(CustomUserDetails user) {
        if (user == null || user.getMember() == null || user.getMember().getId() == null) {
            throw new UserExceptionHandler(ErrorCode.DOCUMENT_FORBIDDEN);
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
            throw new UserExceptionHandler(ErrorCode.DOCUMENT_NOT_FOUND);
        }
    }

    private int parseBlockId(String rawBlockId) {
        try {
            int blockId = Integer.parseInt(rawBlockId);
            if (blockId <= 0) {
                throw new NumberFormatException("block_id must be positive");
            }
            return blockId;
        } catch (Exception e) {
            throw new UserExceptionHandler(ErrorCode.DOCUMENT_PARAGRAPH_NOT_FOUND);
        }
    }

    private ExternalAiEvaluationMetricResponse toMetric(String label, ExternalEvaluatorMetricResponse metric) {
        if (metric == null) {
            return ExternalAiEvaluationMetricResponse.of(label, null, "");
        }
        return ExternalAiEvaluationMetricResponse.of(label, toScore(metric.averageScore()), normalize(metric.finalReview()));
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
        if (!normalize(metric.review()).isEmpty()) {
            builder.append(metric.review());
        }
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
