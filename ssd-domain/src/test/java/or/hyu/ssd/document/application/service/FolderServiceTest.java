package or.hyu.ssd.document.application.service;

import or.hyu.ssd.document.domain.entity.Document;
import or.hyu.ssd.document.domain.entity.Folder;
import or.hyu.ssd.document.repository.DocumentRepository;
import or.hyu.ssd.document.repository.FolderRepository;
import or.hyu.ssd.document.application.command.CreateFolderCommand;
import or.hyu.ssd.document.application.command.UpdateFolderCommand;
import or.hyu.ssd.document.application.result.FolderContentResult;
import or.hyu.ssd.member.domain.entity.Member;
import or.hyu.ssd.member.domain.entity.Role;
import or.hyu.ssd.member.application.service.CustomUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FolderServiceTest {

    @Mock
    private FolderRepository folderRepository;
    @Mock
    private DocumentRepository documentRepository;
    @Mock
    private DocumentCommandService documentCommandService;

    @InjectMocks
    private FolderService folderService;

    @Test
    @DisplayName("listContent()는 현재 탐색 폴더 식별자를 포함해 직계 폴더/문서를 반환한다")
    void listContent_includeCurrentFolderId() {
        // given
        Member member = member(1L);
        CustomUserDetails user = new CustomUserDetails(member);

        // when
        Folder rootA = folder(1L, "작성하기", null, member);
        Folder rootB = folder(2L, "평가하기", null, member);
        Document rootDoc = document(101L, "루트 문서", null, member);

        // then
        when(folderRepository.findAllByMember_IdAndParentIsNull(1L)).thenReturn(List.of(rootA, rootB));
        when(documentRepository.findAllByMember_IdAndFolderIsNull(any(Long.class), any(Sort.class)))
                .thenReturn(List.of(rootDoc));
        when(folderRepository.existsByMember_IdAndParent_Id(1L, 1L)).thenReturn(true);
        when(folderRepository.existsByMember_IdAndParent_Id(1L, 2L)).thenReturn(false);

        FolderContentResult result = folderService.listContent(user, null);

        assertThat(result.parentId()).isEqualTo(0L);
        assertThat(result.currentFolderId()).isEqualTo(0L);
        assertThat(result.folders()).hasSize(2);
        assertThat(result.documents()).hasSize(1);
        assertThat(result.folders().get(0).parentId()).isEqualTo(0L);
        assertThat(result.folders().get(0).hasChildren()).isTrue();
    }

    @Test
    @DisplayName("listContent()는 현재 폴더와 상위 폴더 식별자를 구분해 반환한다")
    void listContent_distinguishParentIdAndCurrentFolderId() {
        // given
        Member member = member(1L);
        CustomUserDetails user = new CustomUserDetails(member);
        Folder parent = folder(10L, "상위", null, member);
        Folder current = folder(20L, "현재", parent, member);
        Folder child = folder(21L, "하위", current, member);
        Document doc = document(101L, "현재 폴더 문서", current, member);

        // when
        when(folderRepository.findById(20L)).thenReturn(Optional.of(current));
        when(folderRepository.findAllByMember_IdAndParent_Id(1L, 20L)).thenReturn(List.of(child));
        when(documentRepository.findAllByMember_IdAndFolder_Id(1L, 20L, Sort.by(Sort.Order.desc("updatedAt"))))
                .thenReturn(List.of(doc));
        when(folderRepository.existsByMember_IdAndParent_Id(1L, 21L)).thenReturn(false);

        // then
        FolderContentResult result = folderService.listContent(user, 20L);

        assertThat(result.parentId()).isEqualTo(10L);
        assertThat(result.currentFolderId()).isEqualTo(20L);
        assertThat(result.folders()).hasSize(1);
        assertThat(result.documents()).hasSize(1);
    }

    @Test
    @DisplayName("listAllContent()는 회원의 전체 폴더/문서를 반환한다")
    void listAllContent_returnsAllFoldersAndDocuments() {
        // given
        Member member = member(1L);
        CustomUserDetails user = new CustomUserDetails(member);

        // when
        Folder root = folder(1L, "작성하기", null, member);
        Folder child = folder(11L, "1분기 계획서", root, member);
        Document docInRoot = document(101L, "사업계획서_v1.pdf", root, member);
        Document docInChild = document(102L, "사업계획서_v2_최종.pdf", child, member);

        // then
        when(folderRepository.findAllByMember_Id(any(Long.class), any(Sort.class)))
                .thenReturn(List.of(root, child));
        when(documentRepository.findAllByMember_Id(any(Long.class), any(Sort.class)))
                .thenReturn(List.of(docInRoot, docInChild));
        when(folderRepository.existsByMember_IdAndParent_Id(1L, 1L)).thenReturn(true);
        when(folderRepository.existsByMember_IdAndParent_Id(1L, 11L)).thenReturn(false);

        FolderContentResult result = folderService.listAllContent(user);

        assertThat(result.parentId()).isEqualTo(0L);
        assertThat(result.currentFolderId()).isEqualTo(0L);
        assertThat(result.folders()).hasSize(2);
        assertThat(result.documents()).hasSize(2);
        assertThat(result.folders().get(0).parentId()).isEqualTo(0L);
        assertThat(result.folders().get(1).parentId()).isEqualTo(1L);
        assertThat(result.documents().get(0).folderId()).isEqualTo(1L);
        assertThat(result.documents().get(1).folderId()).isEqualTo(11L);
    }

    @Test
    @DisplayName("update()는 수정할 값이 하나도 없으면 예외를 던진다")
    void update_throwsWhenNoChangesProvided() {
        // given
        Member member = member(1L);
        CustomUserDetails user = new CustomUserDetails(member);
        Folder folder = folder(1L, "작성하기", null, member);
        when(folderRepository.findById(1L)).thenReturn(Optional.of(folder));

        // when
        // then
        assertThatThrownBy(() -> folderService.update(
                1L,
                user,
                new UpdateFolderCommand(null, null, null)
        ))
                .isInstanceOf(or.hyu.ssd.common.exception.DocumentException.class)
                .hasMessage("수정할 값을 하나 이상 입력해 주세요");
    }

    @Test
    @DisplayName("update()는 공백 폴더명을 거부한다")
    void update_rejectsBlankName() {
        // given
        Member member = member(1L);
        CustomUserDetails user = new CustomUserDetails(member);
        Folder folder = folder(1L, "작성하기", null, member);
        when(folderRepository.findById(1L)).thenReturn(Optional.of(folder));

        // when
        // then
        assertThatThrownBy(() -> folderService.update(
                1L,
                user,
                new UpdateFolderCommand("   ", null, null)
        ))
                .isInstanceOf(or.hyu.ssd.common.exception.DocumentException.class)
                .hasMessage("폴더명은 공백일 수 없습니다");
    }

    @Test
    @DisplayName("create()는 공백 폴더명을 거부한다")
    void create_rejectsBlankName() {
        // given
        Member member = member(1L);
        CustomUserDetails user = new CustomUserDetails(member);

        // when
        // then
        assertThatThrownBy(() -> folderService.create(user, new CreateFolderCommand("   ", null, 0L)))
                .isInstanceOf(or.hyu.ssd.common.exception.DocumentException.class)
                .hasMessage("폴더명은 공백일 수 없습니다");
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

    private Folder folder(Long id, String name, Folder parent, Member member) {
        return Folder.builder()
                .id(id)
                .name(name)
                .color("")
                .parent(parent)
                .member(member)
                .build();
    }

    private Document document(Long id, String title, Folder folder, Member member) {
        return Document.builder()
                .id(id)
                .title(title)
                .content("content")
                .folder(folder)
                .bookmark(false)
                .member(member)
                .build();
    }
}
