package or.hyu.ssd.domain.document.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import or.hyu.ssd.domain.document.controller.dto.ExternalAiBatchResponse;
import or.hyu.ssd.domain.document.controller.dto.ExternalAiDocumentCheckResponse;
import or.hyu.ssd.domain.document.controller.dto.ExternalAiEvaluationCardResponse;
import or.hyu.ssd.domain.document.controller.dto.ExternalAiKeywordResponse;
import or.hyu.ssd.domain.document.controller.dto.ExternalAiSummaryResponse;
import or.hyu.ssd.domain.document.controller.dto.ExternalDocumentIdRequest;

import static org.assertj.core.api.Assertions.assertThat;

class MockExternalAiServiceTest {

    private final MockExternalAiService mockExternalAiService = new MockExternalAiService();

    @Test
    @DisplayName("evaluate()는 요청 docId를 유지한 하드코딩 평가 응답을 반환한다")
    void evaluate_returnsMockResponse() {
        ExternalAiEvaluationCardResponse response = mockExternalAiService.evaluate(new ExternalDocumentIdRequest("7"));

        assertThat(response.documentId()).isEqualTo(7L);
        assertThat(response.totalScore()).isEqualTo(63);
        assertThat(response.problemRecognition().label()).isEqualTo("문제 인식");
        assertThat(response.checkList()).containsEntry("problem_is_clear", true);
    }

    @Test
    @DisplayName("summarizeBasic()는 요청 docId를 유지한 하드코딩 요약 응답을 반환한다")
    void summarizeBasic_returnsMockResponse() {
        ExternalAiSummaryResponse response = mockExternalAiService.summarizeBasic(new ExternalDocumentIdRequest("9"));

        assertThat(response.documentId()).isEqualTo(9L);
        assertThat(response.summary()).contains("공실 데이터");
        assertThat(response.shortSummary()).isEqualTo("공실 데이터 통합 서비스");
    }

    @Test
    @DisplayName("summarizeKeyword()는 요청 docId를 유지한 하드코딩 키워드 응답을 반환한다")
    void summarizeKeyword_returnsMockResponse() {
        ExternalAiKeywordResponse response = mockExternalAiService.summarizeKeyword(new ExternalDocumentIdRequest("11"));

        assertThat(response.documentId()).isEqualTo(11L);
        assertThat(response.keyword()).contains("공실");
    }

    @Test
    @DisplayName("checkNewText()는 변경 블록과 체크리스트를 하드코딩 응답으로 반환한다")
    void checkNewText_returnsMockResponse() {
        ExternalAiDocumentCheckResponse response = mockExternalAiService.checkNewText(new ExternalDocumentIdRequest("13"));

        assertThat(response.documentId()).isEqualTo(13L);
        assertThat(response.changedBlockIds()).containsExactly(1, 3, 5);
        assertThat(response.checkList()).containsEntry("team_structure_is_clear", true);
    }

    @Test
    @DisplayName("generateAll()은 평가, 요약, 키워드를 한 번에 반환한다")
    void generateAll_returnsMockBatchResponse() {
        ExternalAiBatchResponse response = mockExternalAiService.generateAll(new ExternalDocumentIdRequest("15"));

        assertThat(response.documentId()).isEqualTo(15L);
        assertThat(response.evaluation().documentId()).isEqualTo(15L);
        assertThat(response.summary().documentId()).isEqualTo(15L);
        assertThat(response.keyword().documentId()).isEqualTo(15L);
    }
}
