package or.hyu.ssd.document.application.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import or.hyu.ssd.document.domain.entity.Document;
import or.hyu.ssd.document.repository.DocumentCommentRepository;
import or.hyu.ssd.document.repository.DocumentParagraphRepository;
import or.hyu.ssd.document.repository.DocumentRepository;
import or.hyu.ssd.document.application.command.CreateDocumentCommentCommand;
import or.hyu.ssd.member.domain.entity.Member;
import or.hyu.ssd.member.domain.entity.Role;
import or.hyu.ssd.member.application.service.CustomUserDetails;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentCommentServiceTest {

    @Mock
    private DocumentRepository documentRepository;
    @Mock
    private DocumentParagraphRepository documentParagraphRepository;
    @Mock
    private DocumentCommentRepository documentCommentRepository;

    @InjectMocks
    private DocumentCommentService documentCommentService;

    @Test
    @DisplayName("create()는 문서 소유자가 아니면 주석 생성을 거부한다")
    void create_rejectsNonOwner() {
        // given
        Member owner = member(1L, "owner@example.com");
        Member other = member(2L, "other@example.com");
        Document document = document(10L, owner);
        when(documentRepository.findById(10L)).thenReturn(Optional.of(document));

        // when
        // then
        assertThatThrownBy(() -> documentCommentService.create(
                10L,
                new CustomUserDetails(other),
                new CreateDocumentCommentCommand(1, "주석")
        ))
                .isInstanceOf(or.hyu.ssd.common.exception.DocumentException.class)
                .hasMessage("해당 문서를 수정할 권한이 없습니다");
    }

    @Test
    @DisplayName("list()는 문서 소유자가 아니면 주석 조회를 거부한다")
    void list_rejectsNonOwner() {
        // given
        Member owner = member(1L, "owner@example.com");
        Member other = member(2L, "other@example.com");
        Document document = document(10L, owner);
        when(documentRepository.findById(10L)).thenReturn(Optional.of(document));

        // when
        // then
        assertThatThrownBy(() -> documentCommentService.list(
                10L,
                new CustomUserDetails(other)
        ))
                .isInstanceOf(or.hyu.ssd.common.exception.DocumentException.class)
                .hasMessage("해당 문서를 수정할 권한이 없습니다");
    }

    private Document document(Long id, Member member) {
        return Document.builder()
                .id(id)
                .title("문서")
                .content("본문")
                .bookmark(false)
                .member(member)
                .build();
    }

    private Member member(Long id, String email) {
        return Member.builder()
                .id(id)
                .name("테스터")
                .email(email)
                .profileImageUrl("")
                .profileImageKey(null)
                .role(Role.ROLE_AUTHOR)
                .build();
    }
}
