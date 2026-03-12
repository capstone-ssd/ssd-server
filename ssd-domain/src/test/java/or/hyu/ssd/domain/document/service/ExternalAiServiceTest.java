package or.hyu.ssd.domain.document.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import or.hyu.ssd.domain.document.client.ExternalAiPort;
import or.hyu.ssd.domain.document.controller.dto.ExternalAiDocumentCheckResponse;
import or.hyu.ssd.domain.document.controller.dto.ExternalAiEvaluationCardResponse;
import or.hyu.ssd.domain.document.controller.dto.ExternalAiKeywordResponse;
import or.hyu.ssd.domain.document.controller.dto.ExternalAiSummaryResponse;
import or.hyu.ssd.domain.document.controller.dto.ExternalCheckNewTextResponse;
import or.hyu.ssd.domain.document.controller.dto.ExternalDocumentIdRequest;
import or.hyu.ssd.domain.document.controller.dto.ExternalEvaluationReportResponse;
import or.hyu.ssd.domain.document.controller.dto.ExternalEvaluationResponse;
import or.hyu.ssd.domain.document.controller.dto.ExternalEvaluatorMetricResponse;
import or.hyu.ssd.domain.document.controller.dto.ExternalSummarizationBasicResponse;
import or.hyu.ssd.domain.document.controller.dto.ExternalSummarizationKeywordResponse;
import or.hyu.ssd.domain.document.entity.Document;
import or.hyu.ssd.domain.document.entity.DocumentAiCheckSnapshot;
import or.hyu.ssd.domain.document.entity.DocumentParagraph;
import or.hyu.ssd.domain.document.repository.DocumentAiCheckSnapshotRepository;
import or.hyu.ssd.domain.document.repository.DocumentParagraphRepository;
import or.hyu.ssd.domain.document.repository.DocumentRepository;
import or.hyu.ssd.domain.member.entity.Member;
import or.hyu.ssd.domain.member.entity.Role;
import or.hyu.ssd.domain.member.service.CustomUserDetails;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExternalAiServiceTest {

    @Mock
    private ExternalAiPort externalAiPort;
    @Mock
    private DocumentRepository documentRepository;
    @Mock
    private DocumentAiCheckSnapshotRepository documentAiCheckSnapshotRepository;
    @Mock
    private DocumentParagraphRepository documentParagraphRepository;

    @InjectMocks
    private ExternalAiService externalAiService;

    @Test
    @DisplayName("summarizeBasic()는 외부 요약 결과를 문서 summary에 저장한다")
    void summarizeBasic_savesSummary() {
        Member member = member(1L);
        CustomUserDetails user = new CustomUserDetails(member);
        Document document = document(7L, member);
        when(documentRepository.findById(7L)).thenReturn(Optional.of(document));
        when(externalAiPort.summarizeBasic(any())).thenReturn(
                new ExternalSummarizationBasicResponse("7", "핵심 요약", "짧은 요약")
        );

        ExternalAiSummaryResponse response = externalAiService.summarizeBasic(new ExternalDocumentIdRequest("7"), user);

        assertThat(response.documentId()).isEqualTo(7L);
        assertThat(response.summary()).isEqualTo("핵심 요약");
        assertThat(document.getSummary()).isEqualTo("핵심 요약");
    }

    @Test
    @DisplayName("summarizeKeyword()는 외부 키워드 결과를 문서 keywords에 저장한다")
    void summarizeKeyword_savesKeyword() {
        Member member = member(1L);
        CustomUserDetails user = new CustomUserDetails(member);
        Document document = document(7L, member);
        when(documentRepository.findById(7L)).thenReturn(Optional.of(document));
        when(externalAiPort.summarizeKeyword(any())).thenReturn(
                new ExternalSummarizationKeywordResponse("7", "AI, 물류, 자동화")
        );

        ExternalAiKeywordResponse response = externalAiService.summarizeKeyword(new ExternalDocumentIdRequest("7"), user);

        assertThat(response.documentId()).isEqualTo(7L);
        assertThat(response.keyword()).isEqualTo("AI, 물류, 자동화");
        assertThat(document.getKeywords()).isEqualTo("AI, 물류, 자동화");
    }

    @Test
    @DisplayName("evaluate()는 평가 축을 점수 카드 응답으로 가공하고 evaluation 필드에 저장한다")
    void evaluate_mapsMetricsAndStoresEvaluation() {
        Member member = member(1L);
        CustomUserDetails user = new CustomUserDetails(member);
        Document document = document(7L, member);
        when(documentRepository.findById(7L)).thenReturn(Optional.of(document));
        when(externalAiPort.evaluate(any())).thenReturn(
                new ExternalEvaluationResponse(
                        "7",
                        new ExternalEvaluationReportResponse(
                                new ExternalEvaluatorMetricResponse(90.0, "팀 리뷰"),
                                new ExternalEvaluatorMetricResponse(80.0, "실현 가능성 리뷰"),
                                new ExternalEvaluatorMetricResponse(70.0, "문제 리뷰"),
                                new ExternalEvaluatorMetricResponse(60.0, "BM 리뷰"),
                                new ExternalEvaluatorMetricResponse(50.0, "성장 리뷰")
                        ),
                        Map.of("problem_is_clear", true)
                )
        );

        ExternalAiEvaluationCardResponse response = externalAiService.evaluate(new ExternalDocumentIdRequest("7"), user);

        assertThat(response.documentId()).isEqualTo(7L);
        assertThat(response.totalScore()).isEqualTo(70);
        assertThat(response.problemRecognition().score()).isEqualTo(70);
        assertThat(response.feasibility().score()).isEqualTo(80);
        assertThat(response.growthStrategy().score()).isEqualTo(50);
        assertThat(response.businessModel().score()).isEqualTo(60);
        assertThat(response.teamComposition().score()).isEqualTo(90);
        assertThat(document.getExternalAiTotalScore()).isEqualTo(70);
        assertThat(document.getExternalAiProblemRecognitionScore()).isEqualTo(70);
        assertThat(document.getExternalAiProblemRecognitionReview()).isEqualTo("문제 리뷰");
        assertThat(document.isChecklistDifferentiationIsClear()).isFalse();
        assertThat(document.isChecklistProblemIsClear()).isTrue();
        assertThat(document.getEvaluation()).contains("## 문제 인식");
        assertThat(document.getEvaluation()).contains("problem_is_clear: 충족");
    }

    @Test
    @DisplayName("checkNewText()는 문서 기준으로 블록을 검증하고 체크리스트를 OR-merge 한다")
    void checkNewText_mergesChecklist() {
        Member member = member(1L);
        CustomUserDetails user = new CustomUserDetails(member);
        Document document = document(7L, member);
        document.overwriteExternalChecklist(Map.of(
                "problem_is_clear", false,
                "market_definition_is_correct", true
        ));
        when(documentRepository.findById(7L)).thenReturn(Optional.of(document));
        when(documentParagraphRepository.findAllByDocumentOrderByPageNumberAscBlockIdAscIdAsc(document))
                .thenReturn(List.of(
                        DocumentParagraph.of("블록1", "BODY", 1, 1, document),
                        DocumentParagraph.of("본문", "BODY", 1, 3, document)
                ));
        when(documentAiCheckSnapshotRepository.findAllByDocument(document))
                .thenReturn(List.of(
                        DocumentAiCheckSnapshot.of(document, 1, "블록1"),
                        DocumentAiCheckSnapshot.of(document, 3, "기존 본문")
                ));
        when(externalAiPort.checkNewText(any())).thenReturn(
                new ExternalCheckNewTextResponse("3", Map.of(
                        "problem_is_clear", true,
                        "market_definition_is_correct", false
                ))
        );

        ExternalAiDocumentCheckResponse response = externalAiService.checkNewText(
                new ExternalDocumentIdRequest("7"),
                user
        );

        assertThat(response.documentId()).isEqualTo(7L);
        assertThat(response.changedBlockIds()).containsExactly(3);
        assertThat(response.checkList()).containsEntry("problem_is_clear", true);
        assertThat(response.checkList()).containsEntry("market_definition_is_correct", true);
        assertThat(document.isChecklistProblemIsClear()).isTrue();
        assertThat(document.isChecklistMarketDefinitionIsCorrect()).isTrue();
    }

    private Document document(Long id, Member member) {
        return Document.builder()
                .id(id)
                .title("문서 제목")
                .content("문서 본문")
                .bookmark(false)
                .member(member)
                .build();
    }

    private Member member(Long id) {
        return Member.builder()
                .id(id)
                .name("테스터")
                .email("tester@example.com")
                .profileImageUrl("")
                .profileImageKey(null)
                .role(Role.ROLE_AUTHOR)
                .build();
    }
}
