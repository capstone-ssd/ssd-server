package or.hyu.ssd.domain.document.service;

import or.hyu.ssd.domain.document.controller.dto.FolderContentResponse;
import or.hyu.ssd.domain.document.entity.Document;
import or.hyu.ssd.domain.document.entity.Folder;
import or.hyu.ssd.domain.document.repository.DocumentRepository;
import or.hyu.ssd.domain.document.repository.FolderRepository;
import or.hyu.ssd.domain.member.entity.Member;
import or.hyu.ssd.domain.member.entity.Role;
import or.hyu.ssd.domain.member.service.CustomUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FolderServiceTest {

    @Mock
    private FolderRepository folderRepository;
    @Mock
    private DocumentRepository documentRepository;
    @Mock
    private DocumentService documentService;

    @InjectMocks
    private FolderService folderService;

    @Test
    @DisplayName("listContent()는 현재 탐색 폴더 식별자를 포함해 직계 폴더/문서를 반환한다")
    void listContent_includeCurrentFolderId() {
        Member member = member(1L);
        CustomUserDetails user = new CustomUserDetails(member);

        Folder rootA = folder(1L, "작성하기", null, member);
        Folder rootB = folder(2L, "평가하기", null, member);
        Document rootDoc = document(101L, "루트 문서", null, member);

        when(folderRepository.findAllByMember_IdAndParentIsNull(1L)).thenReturn(List.of(rootA, rootB));
        when(documentRepository.findAllByMember_IdAndFolderIsNull(any(Long.class), any(Sort.class)))
                .thenReturn(List.of(rootDoc));
        when(folderRepository.existsByMember_IdAndParent_Id(1L, 1L)).thenReturn(true);
        when(folderRepository.existsByMember_IdAndParent_Id(1L, 2L)).thenReturn(false);

        FolderContentResponse result = folderService.listContent(user, null);

        assertThat(result.parentId()).isEqualTo(0L);
        assertThat(result.currentFolderId()).isEqualTo(0L);
        assertThat(result.folders()).hasSize(2);
        assertThat(result.documents()).hasSize(1);
        assertThat(result.folders().get(0).parentId()).isEqualTo(0L);
        assertThat(result.folders().get(0).hasChildren()).isTrue();
    }

    @Test
    @DisplayName("listAllContent()는 회원의 전체 폴더/문서를 반환한다")
    void listAllContent_returnsAllFoldersAndDocuments() {
        Member member = member(1L);
        CustomUserDetails user = new CustomUserDetails(member);

        Folder root = folder(1L, "작성하기", null, member);
        Folder child = folder(11L, "1분기 계획서", root, member);
        Document docInRoot = document(101L, "사업계획서_v1.pdf", root, member);
        Document docInChild = document(102L, "사업계획서_v2_최종.pdf", child, member);

        when(folderRepository.findAllByMember_Id(any(Long.class), any(Sort.class)))
                .thenReturn(List.of(root, child));
        when(documentRepository.findAllByMember_Id(any(Long.class), any(Sort.class)))
                .thenReturn(List.of(docInRoot, docInChild));
        when(folderRepository.existsByMember_IdAndParent_Id(1L, 1L)).thenReturn(true);
        when(folderRepository.existsByMember_IdAndParent_Id(1L, 11L)).thenReturn(false);

        FolderContentResponse result = folderService.listAllContent(user);

        assertThat(result.parentId()).isEqualTo(0L);
        assertThat(result.currentFolderId()).isEqualTo(0L);
        assertThat(result.folders()).hasSize(2);
        assertThat(result.documents()).hasSize(2);
        assertThat(result.folders().get(0).parentId()).isEqualTo(0L);
        assertThat(result.folders().get(1).parentId()).isEqualTo(1L);
        assertThat(result.documents().get(0).folderId()).isEqualTo(1L);
        assertThat(result.documents().get(1).folderId()).isEqualTo(11L);
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
