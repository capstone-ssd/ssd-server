package or.hyu.ssd.document.application.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import or.hyu.ssd.document.domain.entity.Document;
import or.hyu.ssd.document.domain.entity.DocumentLog;
import or.hyu.ssd.document.repository.DocumentLogRepository;
import or.hyu.ssd.document.repository.DocumentRepository;
import or.hyu.ssd.document.application.result.DocumentLogResult;
import or.hyu.ssd.member.domain.entity.Member;
import or.hyu.ssd.member.domain.entity.Role;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentLogServiceTest {

    @Mock
    private DocumentRepository documentRepository;
    @Mock
    private DocumentLogRepository documentLogRepository;

    @InjectMocks
    private DocumentLogService documentLogService;

    @Test
    @DisplayName("문서 기록은 날짜별로 그룹핑되어 최신 날짜 순으로 조회된다")
    void list_groupsLogsByDate() {
        // given
        Member member = member(1L, "owner@example.com");
        Document document = document(10L, member);
        Long user = member.getId();
        DocumentLog first = DocumentLog.builder()
                .editorName("작성자")
                .editorEmail("owner@example.com")
                .deletedBlockCount(1)
                .createdBlockCount(2)
                .document(document)
                .build();
        setCreatedAt(first, LocalDateTime.of(2026, 3, 17, 15, 10));

        DocumentLog second = DocumentLog.builder()
                .editorName("작성자")
                .editorEmail("owner@example.com")
                .deletedBlockCount(0)
                .createdBlockCount(1)
                .document(document)
                .build();
        setCreatedAt(second, LocalDateTime.of(2026, 3, 17, 9, 5));

        DocumentLog third = DocumentLog.builder()
                .editorName("작성자")
                .editorEmail("owner@example.com")
                .deletedBlockCount(2)
                .createdBlockCount(0)
                .document(document)
                .build();
        setCreatedAt(third, LocalDateTime.of(2026, 3, 16, 18, 0));

        when(documentRepository.findById(10L)).thenReturn(Optional.of(document));
        when(documentLogRepository.findAllByDocumentOrderByCreatedAtDesc(document)).thenReturn(List.of(first, second, third));

        // when
        DocumentLogResult response = documentLogService.list(10L, member.getId());

        // then
        assertThat(response.documentId()).isEqualTo(10L);
        assertThat(response.records()).hasSize(2);
        assertThat(response.records().get(0).savedDate()).isEqualTo("2026-03-17");
        assertThat(response.records().get(0).logs()).hasSize(2);
        assertThat(response.records().get(0).logs().get(0).savedTime()).isEqualTo("15:10");
        assertThat(response.records().get(0).logs().get(0).deletedBlockCount()).isEqualTo(1);
        assertThat(response.records().get(0).logs().get(0).createdBlockCount()).isEqualTo(2);
        assertThat(response.records().get(1).savedDate()).isEqualTo("2026-03-16");
    }

    @Test
    @DisplayName("문서 기록은 문서 소유자가 아니면 조회할 수 없다")
    void list_forbiddenWhenNotOwner() {
        // given
        Member owner = member(1L, "owner@example.com");
        Member other = member(2L, "other@example.com");
        Document document = document(10L, owner);
        when(documentRepository.findById(10L)).thenReturn(Optional.of(document));

        // when
        // then
        assertThatThrownBy(() -> documentLogService.list(10L, other.getId()))
                .isInstanceOf(or.hyu.ssd.common.exception.DocumentException.class)
                .hasMessage("해당 문서를 수정할 권한이 없습니다");
    }

    private void setCreatedAt(DocumentLog log, LocalDateTime createdAt) {
        try {
            java.lang.reflect.Field field = log.getClass().getSuperclass().getDeclaredField("createdAt");
            field.setAccessible(true);
            field.set(log, createdAt);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private Document document(Long id, Member member) {
        return Document.builder()
                .id(id)
                .title("문서")
                .content("본문")
                .member(member)
                .bookmark(false)
                .build();
    }

    private Member member(Long id, String email) {
        return Member.builder()
                .id(id)
                .name("사용자" + id)
                .email(email)
                .profileImageUrl("")
                .profileImageKey(null)
                .role(Role.ROLE_AUTHOR)
                .build();
    }
}
