package or.hyu.ssd.domain.document.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import or.hyu.ssd.domain.document.client.ExternalAiPort;
import or.hyu.ssd.domain.document.client.dto.ExternalCheckNewTextResponse;
import or.hyu.ssd.domain.document.client.dto.ExternalEvaluationReportResponse;
import or.hyu.ssd.domain.document.client.dto.ExternalEvaluationResponse;
import or.hyu.ssd.domain.document.client.dto.ExternalEvaluatorMetricResponse;
import or.hyu.ssd.domain.document.client.dto.ExternalSummarizationBasicResponse;
import or.hyu.ssd.domain.document.entity.Document;
import or.hyu.ssd.domain.document.entity.DocumentAiCheckSnapshot;
import or.hyu.ssd.domain.document.entity.DocumentBlockType;
import or.hyu.ssd.domain.document.entity.DocumentParagraph;
import or.hyu.ssd.domain.document.repository.DocumentAiCheckSnapshotRepository;
import or.hyu.ssd.domain.document.repository.DocumentParagraphRepository;
import or.hyu.ssd.domain.document.repository.DocumentRepository;
import or.hyu.ssd.domain.document.usecase.command.ExternalDocumentIdCommand;
import or.hyu.ssd.domain.document.usecase.result.ExternalAiChecklistResult;
import or.hyu.ssd.domain.document.usecase.result.ExternalAiDocumentCheckResult;
import or.hyu.ssd.domain.document.usecase.result.ExternalAiEvaluationCardResult;
import or.hyu.ssd.domain.document.usecase.result.ExternalAiEvaluationMetricResult;
import or.hyu.ssd.domain.document.usecase.result.ExternalAiSummaryResult;
import or.hyu.ssd.member.domain.entity.Member;
import or.hyu.ssd.member.domain.entity.Role;
import or.hyu.ssd.member.application.service.CustomUserDetails;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExternalAiServiceTest {

    @Mock
    private ExternalAiPort externalAiPort;
    @Mock
    private ExternalAiPersistenceService externalAiPersistenceService;
    @Mock
    private DocumentRepository documentRepository;
    @Mock
    private DocumentAiCheckSnapshotRepository documentAiCheckSnapshotRepository;
    @Mock
    private DocumentParagraphRepository documentParagraphRepository;

    @InjectMocks
    private ExternalAiService externalAiService;

    @Test
    @DisplayName("summarizeBasic()는 외부 요약 결과를 DB 반영 서비스에 위임한다")
    void summarizeBasic_delegatesPersistence() {
        // given
        Member member = member(1L);
        CustomUserDetails user = new CustomUserDetails(member);
        Document document = document(7L, member);
        when(documentRepository.findById(7L)).thenReturn(Optional.of(document));
        when(externalAiPort.summarizeBasic(any())).thenReturn(
                new ExternalSummarizationBasicResponse("7", "핵심 요약", "짧은 요약")
        );
        when(externalAiPersistenceService.saveSummary(7L, "핵심 요약", "짧은 요약"))
                .thenReturn(ExternalAiSummaryResult.of(7L, "핵심 요약", "짧은 요약"));

        // when
        ExternalAiSummaryResult response = externalAiService.summarizeBasic(new ExternalDocumentIdCommand("7"), user);

        // then
        assertThat(response.documentId()).isEqualTo(7L);
        assertThat(response.summary()).isEqualTo("핵심 요약");
        assertThat(response.shortSummary()).isEqualTo("짧은 요약");
    }

    @Test
    @DisplayName("evaluate()는 외부 평가 결과를 DB 반영 서비스에 위임한다")
    void evaluate_delegatesPersistence() {
        // given
        Member member = member(1L);
        CustomUserDetails user = new CustomUserDetails(member);
        Document document = document(7L, member);
        List<DocumentParagraph> currentParagraphs = List.of(
                DocumentParagraph.of(DocumentBlockType.PARAGRAPH, "문단1", "", 1, 1, document)
        );
        when(documentRepository.findById(7L)).thenReturn(Optional.of(document));
        when(documentParagraphRepository.findParagraphBlocks(document))
                .thenReturn(currentParagraphs);
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
        when(externalAiPersistenceService.saveEvaluation(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(ExternalAiEvaluationCardResult.of(
                        7L,
                        70,
                        ExternalAiEvaluationMetricResult.of("문제 인식", 70, "문제 리뷰"),
                        ExternalAiEvaluationMetricResult.of("실현 가능성", 80, "실현 가능성 리뷰"),
                        ExternalAiEvaluationMetricResult.of("성장 전략", 50, "성장 리뷰"),
                        ExternalAiEvaluationMetricResult.of("Business Model", 60, "BM 리뷰"),
                        ExternalAiEvaluationMetricResult.of("팀 구성", 90, "팀 리뷰"),
                        Map.of("problem_is_clear", true)
                ));

        // when
        ExternalAiEvaluationCardResult response = externalAiService.evaluate(new ExternalDocumentIdCommand("7"), user);

        // then
        assertThat(response.documentId()).isEqualTo(7L);
        assertThat(response.totalScore()).isEqualTo(70);
        assertThat(response.checkList()).containsEntry("problem_is_clear", true);
    }

    @Test
    @DisplayName("checkNewText()는 변경 블록이 없으면 외부 AI를 호출하지 않는다")
    void checkNewText_returnsStoredChecklistWhenNoChangedBlock() {
        // given
        Member member = member(1L);
        CustomUserDetails user = new CustomUserDetails(member);
        Document document = document(7L, member);
        document.overwriteExternalChecklist(Map.of("problem_is_clear", true));
        when(documentRepository.findById(7L)).thenReturn(Optional.of(document));
        when(documentParagraphRepository.findParagraphBlocks(document))
                .thenReturn(List.of(DocumentParagraph.of(DocumentBlockType.PARAGRAPH, "본문", "", 1, 1, document)));
        when(documentAiCheckSnapshotRepository.findAllByDocument(document))
                .thenReturn(List.of(DocumentAiCheckSnapshot.of(document, 1, "본문")));

        // when
        ExternalAiDocumentCheckResult response = externalAiService.checkNewText(new ExternalDocumentIdCommand("7"), user);

        // then
        assertThat(response.changedBlockIds()).isEmpty();
        assertThat(response.checkList()).containsEntry("problem_is_clear", true);
        verifyNoInteractions(externalAiPort);
    }

    @Test
    @DisplayName("getSummary()는 저장된 summary와 shortSummary를 반환한다")
    void getSummary_returnsStoredValues() {
        // given
        Member member = member(1L);
        CustomUserDetails user = new CustomUserDetails(member);
        Document document = document(7L, member);
        document.updateSummary("저장된 요약", "짧은 요약");
        when(documentRepository.findById(7L)).thenReturn(Optional.of(document));

        // when
        ExternalAiSummaryResult response = externalAiService.getSummary(7L, user);

        // then
        assertThat(response.summary()).isEqualTo("저장된 요약");
        assertThat(response.shortSummary()).isEqualTo("짧은 요약");
    }

    @Test
    @DisplayName("getChecklist()는 저장된 체크리스트만 반환한다")
    void getChecklist_returnsStoredChecklist() {
        // given
        Member member = member(1L);
        CustomUserDetails user = new CustomUserDetails(member);
        Document document = document(7L, member);
        document.overwriteExternalChecklist(Map.of("problem_is_clear", true));
        when(documentRepository.findById(7L)).thenReturn(Optional.of(document));

        // when
        ExternalAiChecklistResult response = externalAiService.getChecklist(7L, user);

        // then
        assertThat(response.documentId()).isEqualTo(7L);
        assertThat(response.checkList()).containsEntry("problem_is_clear", true);
    }

    @Test
    @DisplayName("evaluate()는 숫자가 아닌 docId를 400 예외로 거부한다")
    void evaluate_rejectsInvalidDocId() {
        // given
        Member member = member(1L);
        CustomUserDetails user = new CustomUserDetails(member);

        // when
        // then
        assertThatThrownBy(() -> externalAiService.evaluate(new ExternalDocumentIdCommand("abc"), user))
                .isInstanceOf(or.hyu.ssd.common.exception.DocumentException.class)
                .hasMessage("docId는 1 이상의 숫자여야 합니다");
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
