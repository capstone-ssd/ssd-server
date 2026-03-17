package or.hyu.ssd.domain.document.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import or.hyu.ssd.domain.document.controller.dto.ExternalAiBatchResponse;
import or.hyu.ssd.domain.document.controller.dto.ExternalAiEvaluationCardResponse;
import or.hyu.ssd.domain.document.controller.dto.ExternalAiEvaluationMetricResponse;
import or.hyu.ssd.domain.document.controller.dto.ExternalAiKeywordResponse;
import or.hyu.ssd.domain.document.controller.dto.ExternalAiSummaryResponse;
import or.hyu.ssd.domain.document.controller.dto.ExternalDocumentIdRequest;
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
        ExternalDocumentIdRequest request = new ExternalDocumentIdRequest("7");

        ExternalAiEvaluationCardResponse evaluation = ExternalAiEvaluationCardResponse.of(
                7L,
                80,
                ExternalAiEvaluationMetricResponse.of("문제 인식", 80, "문제"),
                ExternalAiEvaluationMetricResponse.of("실현 가능성", 80, "실현"),
                ExternalAiEvaluationMetricResponse.of("성장 전략", 80, "성장"),
                ExternalAiEvaluationMetricResponse.of("Business Model", 80, "BM"),
                ExternalAiEvaluationMetricResponse.of("팀 구성", 80, "팀"),
                Map.of("problem_is_clear", true)
        );
        ExternalAiSummaryResponse summary = ExternalAiSummaryResponse.of(7L, "긴 요약", "짧은 요약");
        ExternalAiKeywordResponse keyword = ExternalAiKeywordResponse.of(7L, "공실, SaaS");

        when(externalAiService.evaluate(request, user)).thenReturn(evaluation);
        when(externalAiService.summarizeBasic(request, user)).thenReturn(summary);
        when(externalAiService.summarizeKeyword(request, user)).thenReturn(keyword);

        ExternalAiBatchResponse response = externalAiBatchService.generateAll(request, user);

        InOrder inOrder = inOrder(externalAiService);
        inOrder.verify(externalAiService).evaluate(request, user);
        inOrder.verify(externalAiService).summarizeBasic(request, user);
        inOrder.verify(externalAiService).summarizeKeyword(request, user);

        assertThat(response.documentId()).isEqualTo(7L);
        assertThat(response.evaluation()).isEqualTo(evaluation);
        assertThat(response.summary()).isEqualTo(summary);
        assertThat(response.keyword()).isEqualTo(keyword);
    }

    @Test
    @DisplayName("generateAll()은 요약 단계에서 실패하면 키워드는 호출하지 않는다")
    void generateAll_stopsWhenSummaryFails() {
        CustomUserDetails user = new CustomUserDetails(member(1L));
        ExternalDocumentIdRequest request = new ExternalDocumentIdRequest("7");

        when(externalAiService.evaluate(request, user)).thenReturn(ExternalAiEvaluationCardResponse.of(
                7L,
                80,
                ExternalAiEvaluationMetricResponse.of("문제 인식", 80, "문제"),
                ExternalAiEvaluationMetricResponse.of("실현 가능성", 80, "실현"),
                ExternalAiEvaluationMetricResponse.of("성장 전략", 80, "성장"),
                ExternalAiEvaluationMetricResponse.of("Business Model", 80, "BM"),
                ExternalAiEvaluationMetricResponse.of("팀 구성", 80, "팀"),
                Map.of()
        ));
        when(externalAiService.summarizeBasic(request, user)).thenThrow(new IllegalStateException("summary failed"));

        assertThatThrownBy(() -> externalAiBatchService.generateAll(request, user))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("summary failed");

        verify(externalAiService).evaluate(request, user);
        verify(externalAiService).summarizeBasic(request, user);
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
