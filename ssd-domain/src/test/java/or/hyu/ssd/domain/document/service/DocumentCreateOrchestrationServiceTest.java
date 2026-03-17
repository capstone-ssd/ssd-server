package or.hyu.ssd.domain.document.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import or.hyu.ssd.domain.document.controller.dto.CreateDocumentParagraphRequest;
import or.hyu.ssd.domain.document.controller.dto.CreateDocumentRequest;
import or.hyu.ssd.domain.document.controller.dto.CreateDocumentResponse;
import or.hyu.ssd.domain.member.entity.Member;
import or.hyu.ssd.domain.member.entity.Role;
import or.hyu.ssd.domain.member.service.CustomUserDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentCreateOrchestrationServiceTest {

    @Mock
    private DocumentService documentService;
    @Mock
    private ExternalAiService externalAiService;

    @InjectMocks
    private DocumentCreateOrchestrationService documentCreateOrchestrationService;

    @Test
    @DisplayName("문서 생성 성공 시 AI 평가, 요약, 키워드를 순차 호출한다")
    void createDocumentWithAi_callsExternalAiInOrder() {
        CustomUserDetails user = new CustomUserDetails(member(1L));
        CreateDocumentRequest request = request();
        when(documentService.createDocument(user, request)).thenReturn(CreateDocumentResponse.of(7L));

        CreateDocumentResponse response = documentCreateOrchestrationService.createDocumentWithAi(user, request);

        assertThat(response.id()).isEqualTo(7L);
        InOrder inOrder = inOrder(documentService, externalAiService);
        inOrder.verify(documentService).createDocument(user, request);
        inOrder.verify(externalAiService).evaluate(any(), any());
        inOrder.verify(externalAiService).summarizeBasic(any(), any());
        inOrder.verify(externalAiService).summarizeKeyword(any(), any());
        verify(documentService, never()).deleteDocument(7L, user);
    }

    @Test
    @DisplayName("AI 평가 실패 시 생성된 문서를 보상 삭제한다")
    void createDocumentWithAi_deletesDocumentWhenEvaluateFails() {
        CustomUserDetails user = new CustomUserDetails(member(1L));
        CreateDocumentRequest request = request();
        when(documentService.createDocument(user, request)).thenReturn(CreateDocumentResponse.of(7L));
        doThrow(new RuntimeException("evaluate failed")).when(externalAiService).evaluate(any(), any());

        assertThatThrownBy(() -> documentCreateOrchestrationService.createDocumentWithAi(user, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("evaluate failed");

        verify(documentService).deleteDocument(7L, user);
        verify(externalAiService, never()).summarizeBasic(any(), any());
        verify(externalAiService, never()).summarizeKeyword(any(), any());
    }

    @Test
    @DisplayName("AI 요약 실패 시 생성된 문서를 보상 삭제한다")
    void createDocumentWithAi_deletesDocumentWhenSummaryFails() {
        CustomUserDetails user = new CustomUserDetails(member(1L));
        CreateDocumentRequest request = request();
        when(documentService.createDocument(user, request)).thenReturn(CreateDocumentResponse.of(7L));
        doThrow(new RuntimeException("summary failed")).when(externalAiService).summarizeBasic(any(), any());

        assertThatThrownBy(() -> documentCreateOrchestrationService.createDocumentWithAi(user, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("summary failed");

        verify(documentService).deleteDocument(7L, user);
        verify(externalAiService).evaluate(any(), any());
        verify(externalAiService, never()).summarizeKeyword(any(), any());
    }

    @Test
    @DisplayName("AI 키워드 실패 시 생성된 문서를 보상 삭제한다")
    void createDocumentWithAi_deletesDocumentWhenKeywordFails() {
        CustomUserDetails user = new CustomUserDetails(member(1L));
        CreateDocumentRequest request = request();
        when(documentService.createDocument(user, request)).thenReturn(CreateDocumentResponse.of(7L));
        doThrow(new RuntimeException("keyword failed")).when(externalAiService).summarizeKeyword(any(), any());

        assertThatThrownBy(() -> documentCreateOrchestrationService.createDocumentWithAi(user, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("keyword failed");

        verify(documentService).deleteDocument(7L, user);
        verify(externalAiService).evaluate(any(), any());
        verify(externalAiService).summarizeBasic(any(), any());
    }

    private CreateDocumentRequest request() {
        return new CreateDocumentRequest(
                "문서 제목",
                "문서 본문",
                List.of(new CreateDocumentParagraphRequest("문단", "", 1)),
                0L
        );
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
