package or.hyu.ssd.domain.document.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import or.hyu.ssd.domain.document.controller.dto.ExternalAiDocumentCheckResponse;
import or.hyu.ssd.domain.document.controller.dto.ExternalAiEvaluationCardResponse;
import or.hyu.ssd.domain.document.controller.dto.ExternalAiEvaluationMetricResponse;
import or.hyu.ssd.domain.document.controller.dto.ExternalAiSummaryResponse;
import or.hyu.ssd.domain.document.entity.Document;
import or.hyu.ssd.domain.document.entity.DocumentBlockType;
import or.hyu.ssd.domain.document.entity.DocumentParagraph;
import or.hyu.ssd.domain.document.repository.DocumentAiCheckSnapshotRepository;
import or.hyu.ssd.domain.document.repository.DocumentRepository;
import or.hyu.ssd.domain.member.entity.Member;
import or.hyu.ssd.domain.member.entity.Role;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExternalAiPersistenceServiceTest {

    @Mock
    private DocumentRepository documentRepository;
    @Mock
    private DocumentAiCheckSnapshotRepository documentAiCheckSnapshotRepository;

    @InjectMocks
    private ExternalAiPersistenceService externalAiPersistenceService;

    @Test
    @DisplayName("saveSummary()는 summary와 shortSummary를 함께 저장한다")
    void saveSummary_savesSummaryAndShortSummary() {
        Document document = document(7L);
        when(documentRepository.findById(7L)).thenReturn(Optional.of(document));

        ExternalAiSummaryResponse response = externalAiPersistenceService.saveSummary(7L, "요약", "짧은 요약");

        assertThat(response.summary()).isEqualTo("요약");
        assertThat(response.shortSummary()).isEqualTo("짧은 요약");
        assertThat(document.getSummary()).isEqualTo("요약");
        assertThat(document.getShortSummary()).isEqualTo("짧은 요약");
    }

    @Test
    @DisplayName("saveEvaluation()는 평가 점수와 체크리스트를 저장하고 snapshot을 갱신한다")
    void saveEvaluation_updatesMetricsChecklistAndSnapshots() {
        Document document = document(7L);
        when(documentRepository.findById(7L)).thenReturn(Optional.of(document));

        ExternalAiEvaluationCardResponse response = externalAiPersistenceService.saveEvaluation(
                7L,
                ExternalAiEvaluationMetricResponse.of("문제 인식", 70, "문제 리뷰"),
                ExternalAiEvaluationMetricResponse.of("실현 가능성", 80, "실현 가능성 리뷰"),
                ExternalAiEvaluationMetricResponse.of("성장 전략", 50, "성장 리뷰"),
                ExternalAiEvaluationMetricResponse.of("Business Model", 60, "BM 리뷰"),
                ExternalAiEvaluationMetricResponse.of("팀 구성", 90, "팀 리뷰"),
                70,
                Map.of("problem_is_clear", true),
                List.of(DocumentParagraph.of(DocumentBlockType.PARAGRAPH, "본문", "", 1, 1, document))
        );

        assertThat(response.totalScore()).isEqualTo(70);
        assertThat(document.getExternalAiTotalScore()).isEqualTo(70);
        assertThat(document.getExternalAiProblemRecognitionScore()).isEqualTo(70);
        assertThat(document.getExternalAiProblemRecognitionReview()).isEqualTo("문제 리뷰");
        assertThat(document.isChecklistProblemIsClear()).isTrue();
        assertThat(document.getEvaluation()).contains("## 문제 인식");
        verify(documentAiCheckSnapshotRepository).deleteAllByDocument(document);
        verify(documentAiCheckSnapshotRepository).saveAll(any());
    }

    @Test
    @DisplayName("mergeChecklist()는 false->true만 누적 반영한다")
    void mergeChecklist_orMergesChecklist() {
        Document document = document(7L);
        document.overwriteExternalChecklist(Map.of(
                "problem_is_clear", false,
                "market_definition_is_correct", true
        ));
        when(documentRepository.findById(7L)).thenReturn(Optional.of(document));

        ExternalAiDocumentCheckResponse response = externalAiPersistenceService.mergeChecklist(
                7L,
                List.of(3),
                Map.of(
                        "problem_is_clear", true,
                        "market_definition_is_correct", false
                ),
                List.of(DocumentParagraph.of(DocumentBlockType.PARAGRAPH, "본문", "", 1, 3, document))
        );

        assertThat(response.changedBlockIds()).containsExactly(3);
        assertThat(document.isChecklistProblemIsClear()).isTrue();
        assertThat(document.isChecklistMarketDefinitionIsCorrect()).isTrue();
    }

    private Document document(Long id) {
        return Document.builder()
                .id(id)
                .title("문서 제목")
                .content("문서 본문")
                .bookmark(false)
                .member(Member.builder()
                        .id(1L)
                        .name("테스터")
                        .email("tester@example.com")
                        .profileImageUrl("")
                        .profileImageKey(null)
                        .role(Role.ROLE_AUTHOR)
                        .build())
                .build();
    }
}
