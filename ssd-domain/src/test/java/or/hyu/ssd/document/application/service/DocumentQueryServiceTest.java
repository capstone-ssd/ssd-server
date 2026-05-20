package or.hyu.ssd.document.application.service;

import or.hyu.ssd.document.domain.model.Document;
import or.hyu.ssd.document.domain.model.DocumentBlockType;
import or.hyu.ssd.document.domain.model.DocumentParagraph;
import or.hyu.ssd.document.domain.model.DocumentPurpose;
import or.hyu.ssd.document.domain.model.Folder;
import or.hyu.ssd.document.repository.DocumentParagraphRepository;
import or.hyu.ssd.document.repository.DocumentRepository;
import or.hyu.ssd.document.repository.DocumentSearchRepository;
import or.hyu.ssd.document.repository.FolderRepository;
import or.hyu.ssd.document.application.support.DocumentSort;
import or.hyu.ssd.document.application.result.DocumentDetailResult;
import or.hyu.ssd.document.application.result.DocumentListItemResult;
import or.hyu.ssd.document.application.result.DocumentSearchSuggestionResult;
import or.hyu.ssd.common.exception.DocumentException;
import or.hyu.ssd.member.domain.model.Member;
import or.hyu.ssd.member.domain.model.Role;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentQueryServiceTest {

    @Mock
    private DocumentRepository documentRepository;
    @Mock
    private DocumentSearchRepository documentSearchRepository;
    @Mock
    private DocumentParagraphRepository documentParagraphRepository;
    @Mock
    private FolderRepository folderRepository;

    @InjectMocks
    private DocumentQueryService documentQueryService;

    @Test
    @DisplayName("getDocument()는 소유한 문서와 블록 목록을 반환한다")
    void getDocument_returnsOwnedDocumentWithBlocks() {
        // given
        Member member = member(1L);
        Long user = member.getId();
        Document document = document(42L, "문서 제목", null, member);
        document.markExternalAiProcessed();

        // when
        when(documentRepository.findById(42L)).thenReturn(Optional.of(document));
        when(documentParagraphRepository.findBlocks(document)).thenReturn(List.of(
                DocumentParagraph.of(DocumentBlockType.PARAGRAPH, "문단", "#", 1, 1, document),
                DocumentParagraph.of(DocumentBlockType.IMAGE, "https://s3.example.com/image.png", null, 1, 2, document)
        ));

        // then
        DocumentDetailResult result = documentQueryService.getDocument(42L, member.getId());

        assertThat(result.id()).isEqualTo(42L);
        assertThat(result.authorId()).isEqualTo(1L);
        assertThat(result.purpose()).isEqualTo(DocumentPurpose.EVALUATION);
        assertThat(result.hasExternalAiResult()).isTrue();
        assertThat(result.blocks()).hasSize(2);
        assertThat(result.blocks().get(0).content()).isEqualTo("문단");
        assertThat(result.blocks().get(1).url()).isEqualTo("https://s3.example.com/image.png");
    }

    @Test
    @DisplayName("listDocuments()는 루트 폴더 문서만 조회할 수 있다")
    void listDocuments_returnsRootDocuments() {
        // given
        Member member = member(1L);
        Long user = member.getId();
        Document rootDocument = document(100L, "루트 문서", null, member, true);

        // when
        when(documentRepository.findAllByMember_IdAndFolderIsNull(1L, Sort.by(Sort.Order.desc("createdAt"))))
                .thenReturn(List.of(rootDocument));

        // then
        List<DocumentListItemResult> results = documentQueryService.listDocuments(member.getId(), DocumentSort.LATEST, 0L);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).id()).isEqualTo(100L);
        assertThat(results.get(0).purpose()).isEqualTo(DocumentPurpose.EVALUATION);
        assertThat(results.get(0).folderId()).isNull();
        assertThat(results.get(0).bookmark()).isTrue();
    }

    @Test
    @DisplayName("searchDocuments()는 회원의 문서 중 제목에 검색어가 포함된 문서를 조회한다")
    void searchDocuments_returnsMatchedDocumentsByTitle() {
        // given
        Member member = member(1L);
        Long user = member.getId();
        Document document = document(200L, "AI 사업계획서", null, member, true);
        Sort sort = Sort.by(Sort.Order.desc("updatedAt"));
        when(documentSearchRepository.searchDocuments(1L, "사업", sort))
                .thenReturn(List.of(document));

        // when
        List<DocumentListItemResult> results = documentQueryService.searchDocuments(user, " 사업 ", DocumentSort.MODIFIED);

        // then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).id()).isEqualTo(200L);
        assertThat(results.get(0).title()).isEqualTo("AI 사업계획서");
        assertThat(results.get(0).bookmark()).isTrue();
        verify(documentSearchRepository).searchDocuments(1L, "사업", sort);
    }

    @Test
    @DisplayName("searchDocumentsByTitlePrefix()는 회원의 문서 중 제목이 검색어로 시작하는 문서를 조회한다")
    void searchDocumentsByTitlePrefix_returnsMatchedDocumentsByTitlePrefix() {
        // given
        Member member = member(1L);
        Long user = member.getId();
        Document document = document(201L, "사업계획서 AI", null, member, true);
        Sort sort = Sort.by(Sort.Order.desc("updatedAt"));
        when(documentSearchRepository.searchDocumentsByTitlePrefix(1L, "사업", sort))
                .thenReturn(List.of(document));

        // when
        List<DocumentListItemResult> results = documentQueryService.searchDocumentsByTitlePrefix(user, " 사업 ", DocumentSort.MODIFIED);

        // then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).id()).isEqualTo(201L);
        assertThat(results.get(0).title()).isEqualTo("사업계획서 AI");
        assertThat(results.get(0).bookmark()).isTrue();
        verify(documentSearchRepository).searchDocumentsByTitlePrefix(1L, "사업", sort);
    }

    @Test
    @DisplayName("searchDocuments()는 검색어가 공백이면 예외를 던진다")
    void searchDocuments_rejectsBlankKeyword() {
        // given
        Member member = member(1L);

        // when
        assertThatThrownBy(() -> documentQueryService.searchDocuments(member.getId(), "   ", DocumentSort.LATEST))
                .isInstanceOf(DocumentException.class);

        // then
        verifyNoInteractions(documentSearchRepository);
    }

    @Test
    @DisplayName("suggestSearchKeywords()는 회원 문서 기반 관련 검색어를 조회한다")
    void suggestSearchKeywords_returnsRelatedKeywords() {
        // given
        Member member = member(1L);
        Long user = member.getId();
        when(documentSearchRepository.suggestSearchKeywords(1L, "사업", 5, 0.2))
                .thenReturn(List.of(
                        DocumentSearchSuggestionResult.of("사업계획서", 0.72),
                        DocumentSearchSuggestionResult.of("사업 모델", 0.61)
                ));

        // when
        List<DocumentSearchSuggestionResult> results = documentQueryService.suggestSearchKeywords(user, " 사업 ", 5);

        // then
        assertThat(results)
                .extracting(DocumentSearchSuggestionResult::keyword)
                .containsExactly("사업계획서", "사업 모델");
        verify(documentSearchRepository).suggestSearchKeywords(1L, "사업", 5, 0.2);
    }

    @Test
    @DisplayName("suggestSearchKeywords()는 한 글자 검색어면 빈 목록을 반환한다")
    void suggestSearchKeywords_returnsEmptyListWhenKeywordTooShort() {
        // given
        Member member = member(1L);

        // when
        List<DocumentSearchSuggestionResult> results = documentQueryService.suggestSearchKeywords(member.getId(), "사", 5);

        // then
        assertThat(results).isEmpty();
        verifyNoInteractions(documentSearchRepository);
    }

    private Document document(Long id, String title, Folder folder, Member member) {
        return document(id, title, folder, member, false);
    }

    private Document document(Long id, String title, Folder folder, Member member, boolean bookmark) {
        return Document.builder()
                .id(id)
                .title(title)
                .content("content")
                .folder(folder)
                .bookmark(bookmark)
                .purpose(DocumentPurpose.EVALUATION)
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
