package or.hyu.ssd.domain.document.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import or.hyu.ssd.domain.document.usecase.command.ExternalDocumentIdCommand;
import or.hyu.ssd.domain.document.usecase.result.ExternalAiBatchResult;
import or.hyu.ssd.domain.document.usecase.result.ExternalAiDocumentCheckResult;
import or.hyu.ssd.domain.document.usecase.result.ExternalAiEvaluationCardResult;
import or.hyu.ssd.domain.document.usecase.result.ExternalAiKeywordResult;
import or.hyu.ssd.domain.document.usecase.result.ExternalAiSummaryResult;

import static org.assertj.core.api.Assertions.assertThat;

class MockExternalAiServiceTest {

    private final MockExternalAiService mockExternalAiService = new MockExternalAiService();

    @Test
    @DisplayName("evaluate()는 요청 docId를 유지한 하드코딩 평가 응답을 반환한다")
    void evaluate_returnsMockResponse() {
        // given
        ExternalAiEvaluationCardResult response = mockExternalAiService.evaluate(new ExternalDocumentIdCommand("7"));

        // when
        // then
        assertThat(response.documentId()).isEqualTo(7L);
        assertThat(response.totalScore()).isEqualTo(63);
        assertThat(response.problemRecognition().label()).isEqualTo("문제 인식");
        assertThat(response.checkList()).containsEntry("problem_is_clear", true);
    }

    @Test
    @DisplayName("summarizeBasic()는 요청 docId를 유지한 하드코딩 요약 응답을 반환한다")
    void summarizeBasic_returnsMockResponse() {
        // given
        ExternalAiSummaryResult response = mockExternalAiService.summarizeBasic(new ExternalDocumentIdCommand("9"));

        // when
        // then
        assertThat(response.documentId()).isEqualTo(9L);
        assertThat(response.summary()).contains("공실 데이터");
        assertThat(response.shortSummary()).isEqualTo("공실 데이터 통합 서비스");
    }

    @Test
    @DisplayName("summarizeKeyword()는 요청 docId를 유지한 하드코딩 키워드 응답을 반환한다")
    void summarizeKeyword_returnsMockResponse() {
        // given
        ExternalAiKeywordResult response = mockExternalAiService.summarizeKeyword(new ExternalDocumentIdCommand("11"));

        // when
        // then
        assertThat(response.documentId()).isEqualTo(11L);
        assertThat(response.keyword()).contains("공실");
    }

    @Test
    @DisplayName("checkNewText()는 변경 블록과 체크리스트를 하드코딩 응답으로 반환한다")
    void checkNewText_returnsMockResponse() {
        // given
        ExternalAiDocumentCheckResult response = mockExternalAiService.checkNewText(new ExternalDocumentIdCommand("13"));

        // when
        // then
        assertThat(response.documentId()).isEqualTo(13L);
        assertThat(response.changedBlockIds()).containsExactly(1, 3, 5);
        assertThat(response.checkList()).containsEntry("team_structure_is_clear", true);
    }

    @Test
    @DisplayName("generateAll()은 평가, 요약, 키워드를 한 번에 반환한다")
    void generateAll_returnsMockBatchResponse() {
        // given
        ExternalAiBatchResult response = mockExternalAiService.generateAll(new ExternalDocumentIdCommand("15"));

        // when
        // then
        assertThat(response.documentId()).isEqualTo(15L);
        assertThat(response.evaluation().documentId()).isEqualTo(15L);
        assertThat(response.summary().documentId()).isEqualTo(15L);
        assertThat(response.keyword().documentId()).isEqualTo(15L);
    }
}
