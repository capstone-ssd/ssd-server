package or.hyu.ssd.domain.document.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import or.hyu.ssd.domain.document.usecase.command.ExternalDocumentIdCommand;
import or.hyu.ssd.domain.document.usecase.result.ExternalAiBatchResult;
import or.hyu.ssd.domain.document.usecase.result.ExternalAiEvaluationCardResult;
import or.hyu.ssd.domain.document.usecase.result.ExternalAiEvaluationMetricResult;
import or.hyu.ssd.domain.document.usecase.result.ExternalAiKeywordResult;
import or.hyu.ssd.domain.document.usecase.result.ExternalAiSummaryResult;
import or.hyu.ssd.domain.member.entity.Member;
import or.hyu.ssd.domain.member.entity.Role;
import or.hyu.ssd.domain.member.service.CustomUserDetails;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExternalAiBatchServiceTest {

    @Mock
    private ExternalAiService externalAiService;

    @InjectMocks
    private ExternalAiBatchService externalAiBatchService;

    @Test
    @DisplayName("generateAll()은 평가, 요약, 키워드 순서로 호출한다")
    void generateAll_callsAiApisInOrder() {
        CustomUserDetails user = new CustomUserDetails(member(1L));
        ExternalDocumentIdCommand command = new ExternalDocumentIdCommand("7");

        ExternalAiEvaluationCardResult evaluation = ExternalAiEvaluationCardResult.of(
                7L,
                80,
                ExternalAiEvaluationMetricResult.of("문제 인식", 80, "문제"),
                ExternalAiEvaluationMetricResult.of("실현 가능성", 80, "실현"),
                ExternalAiEvaluationMetricResult.of("성장 전략", 80, "성장"),
                ExternalAiEvaluationMetricResult.of("Business Model", 80, "BM"),
                ExternalAiEvaluationMetricResult.of("팀 구성", 80, "팀"),
                Map.of("problem_is_clear", true)
        );
        ExternalAiSummaryResult summary = ExternalAiSummaryResult.of(7L, "긴 요약", "짧은 요약");
        ExternalAiKeywordResult keyword = ExternalAiKeywordResult.of(7L, "공실, SaaS");

        when(externalAiService.evaluate(command, user)).thenReturn(evaluation);
        when(externalAiService.summarizeBasic(command, user)).thenReturn(summary);
        when(externalAiService.summarizeKeyword(command, user)).thenReturn(keyword);

        ExternalAiBatchResult response = externalAiBatchService.generateAll(command, user);

        InOrder inOrder = inOrder(externalAiService);
        inOrder.verify(externalAiService).evaluate(command, user);
        inOrder.verify(externalAiService).summarizeBasic(command, user);
        inOrder.verify(externalAiService).summarizeKeyword(command, user);

        assertThat(response.documentId()).isEqualTo(7L);
        assertThat(response.evaluation()).isEqualTo(evaluation);
        assertThat(response.summary()).isEqualTo(summary);
        assertThat(response.keyword()).isEqualTo(keyword);
    }

    @Test
    @DisplayName("generateAll()은 요약 단계에서 실패하면 키워드는 호출하지 않는다")
    void generateAll_stopsWhenSummaryFails() {
        CustomUserDetails user = new CustomUserDetails(member(1L));
        ExternalDocumentIdCommand command = new ExternalDocumentIdCommand("7");

        when(externalAiService.evaluate(command, user)).thenReturn(ExternalAiEvaluationCardResult.of(
                7L,
                80,
                ExternalAiEvaluationMetricResult.of("문제 인식", 80, "문제"),
                ExternalAiEvaluationMetricResult.of("실현 가능성", 80, "실현"),
                ExternalAiEvaluationMetricResult.of("성장 전략", 80, "성장"),
                ExternalAiEvaluationMetricResult.of("Business Model", 80, "BM"),
                ExternalAiEvaluationMetricResult.of("팀 구성", 80, "팀"),
                Map.of()
        ));
        when(externalAiService.summarizeBasic(command, user)).thenThrow(new IllegalStateException("summary failed"));

        assertThatThrownBy(() -> externalAiBatchService.generateAll(command, user))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("summary failed");

        verify(externalAiService).evaluate(command, user);
        verify(externalAiService).summarizeBasic(command, user);
        verifyNoMoreInteractions(externalAiService);
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
