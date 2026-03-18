package or.hyu.ssd.domain.document.service;

import or.hyu.ssd.domain.document.controller.dto.CreateDocumentParagraphRequest;
import or.hyu.ssd.domain.document.controller.dto.CreateDocumentRequest;
import or.hyu.ssd.domain.document.controller.dto.CreateDocumentResponse;
import or.hyu.ssd.domain.document.entity.Document;
import or.hyu.ssd.domain.document.entity.DocumentParagraph;
import or.hyu.ssd.domain.document.repository.CheckListRepository;
import or.hyu.ssd.domain.document.repository.DocumentAiCheckSnapshotRepository;
import or.hyu.ssd.domain.document.repository.DocumentCommentRepository;
import or.hyu.ssd.domain.document.repository.DocumentLogRepository;
import or.hyu.ssd.domain.document.repository.DocumentParagraphRepository;
import or.hyu.ssd.domain.document.repository.DocumentRepository;
import or.hyu.ssd.domain.document.repository.EvaluatorCheckListRepository;
import or.hyu.ssd.domain.document.repository.EvaluatorReviewRepository;
import or.hyu.ssd.domain.document.repository.FolderRepository;
import or.hyu.ssd.domain.member.entity.Member;
import or.hyu.ssd.domain.member.entity.Role;
import or.hyu.ssd.domain.member.service.CustomUserDetails;
import or.hyu.ssd.global.util.OptimisticRetryExecutor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;
    @Mock
    private CheckListRepository checkListRepository;
    @Mock
    private EvaluatorCheckListRepository evaluatorCheckListRepository;
    @Mock
    private DocumentAiCheckSnapshotRepository documentAiCheckSnapshotRepository;
    @Mock
    private DocumentParagraphRepository documentParagraphRepository;
    @Mock
    private DocumentCommentRepository documentCommentRepository;
    @Mock
    private DocumentLogRepository documentLogRepository;
    @Mock
    private EvaluatorReviewRepository evaluatorReviewRepository;
    @Mock
    private FolderRepository folderRepository;
    @Mock
    private OptimisticRetryExecutor optimisticRetryExecutor;

    @InjectMocks
    private DocumentService documentService;

    @Test
    @DisplayName("createDocument()는 생성 요청 문단의 pageNumber를 1로 저장하고 첫 문단으로 제목을 만든다")
    void createDocument_defaultsPageNumberAndResolvesTitleFromParagraph() {
        Member member = member(1L);
        CustomUserDetails user = new CustomUserDetails(member);
        CreateDocumentRequest request = new CreateDocumentRequest(
                null,
                "   ",
                List.of(
                        new CreateDocumentParagraphRequest("첫 문단 제목", "#", 99),
                        new CreateDocumentParagraphRequest("둘째 문단", "", 100)
                ),
                0L
        );

        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> {
            Document doc = invocation.getArgument(0);
            return Document.builder()
                    .id(42L)
                    .title(doc.getTitle())
                    .content(doc.getContent())
                    .folder(doc.getFolder())
                    .bookmark(doc.isBookmark())
                    .member(doc.getMember())
                    .build();
        });
        when(documentParagraphRepository.saveAll(any())).thenAnswer(invocation -> {
            Iterable<DocumentParagraph> paragraphs = invocation.getArgument(0);
            List<DocumentParagraph> saved = new ArrayList<>();
            paragraphs.forEach(saved::add);
            return saved;
        });

        CreateDocumentResponse response = documentService.createDocument(user, request);

        ArgumentCaptor<Document> documentCaptor = ArgumentCaptor.forClass(Document.class);
        verify(documentRepository).save(documentCaptor.capture());
        assertThat(documentCaptor.getValue().getTitle()).isEqualTo("첫 문단 제목");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Iterable<DocumentParagraph>> paragraphCaptor =
                (ArgumentCaptor<Iterable<DocumentParagraph>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(Iterable.class);
        verify(documentParagraphRepository).saveAll(paragraphCaptor.capture());
        List<DocumentParagraph> savedParagraphs = new ArrayList<>();
        paragraphCaptor.getValue().forEach(savedParagraphs::add);

        assertThat(savedParagraphs)
                .extracting(DocumentParagraph::getPageNumber)
                .containsExactly(1, 1);
        assertThat(savedParagraphs)
                .extracting(DocumentParagraph::getBlockId)
                .containsExactly(1, 2);
        assertThat(response.id()).isEqualTo(42L);
    }

    @Test
    @DisplayName("updateDocument()는 공백 제목이나 공백 본문을 거부한다")
    void updateDocument_rejectsBlankFields() {
        Member member = member(1L);
        CustomUserDetails user = new CustomUserDetails(member);
        Document document = document(42L, member);
        when(documentRepository.findById(42L)).thenReturn(Optional.of(document));

        assertThatThrownBy(() -> documentService.updateDocument(
                42L,
                user,
                new CreateDocumentRequest(
                        "   ", "본문", null, null
                )
        ))
                .isInstanceOf(or.hyu.ssd.global.api.handler.DomainException.class)
                .hasMessage("제목은 공백일 수 없습니다");

        assertThatThrownBy(() -> documentService.updateDocument(
                42L,
                user,
                new CreateDocumentRequest(
                        null, "   ", null, null
                )
        ))
                .isInstanceOf(or.hyu.ssd.global.api.handler.DomainException.class)
                .hasMessage("내용은 공백일 수 없습니다");
    }


    @Test
    @DisplayName("updateDocument()는 요청 blockId를 유지하고 삭제된 block 주석을 정리하며 변경 개수를 기록한다")
    void updateDocument_preservesBlockIdsAndDeletesRemovedComments() {
        Member member = member(1L);
        CustomUserDetails user = new CustomUserDetails(member);
        Document document = document(42L, member);
        when(documentRepository.findById(42L)).thenReturn(Optional.of(document));
        when(documentParagraphRepository.findAllByDocumentOrderByPageNumberAscBlockIdAscIdAsc(document))
                .thenReturn(List.of(
                        DocumentParagraph.of("기존 문단 1", "#", 1, 1, document),
                        DocumentParagraph.of("기존 문단 2", "##", 1, 2, document)
                ));
        when(documentParagraphRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        documentService.updateDocument(
                42L,
                user,
                new CreateDocumentRequest(
                        null,
                        "새 본문",
                        List.of(
                                new CreateDocumentParagraphRequest("기존 문단 1 수정", "#", 1),
                                new CreateDocumentParagraphRequest("새 문단", "", 3)
                        ),
                        0L
                )
        );

        assertThat(document.getTitle()).isEqualTo("문서 제목");
        assertThat(document.getContent()).isEqualTo("새 본문");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Iterable<DocumentParagraph>> paragraphCaptor =
                (ArgumentCaptor<Iterable<DocumentParagraph>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(Iterable.class);
        verify(documentParagraphRepository).deleteAllByDocument(document);
        verify(documentParagraphRepository).saveAll(paragraphCaptor.capture());

        List<DocumentParagraph> savedParagraphs = new ArrayList<>();
        paragraphCaptor.getValue().forEach(savedParagraphs::add);
        assertThat(savedParagraphs)
                .extracting(DocumentParagraph::getPageNumber)
                .containsExactly(1, 1);
        assertThat(savedParagraphs)
                .extracting(DocumentParagraph::getBlockId)
                .containsExactly(1, 3);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<Integer>> removedBlockIdsCaptor =
                (ArgumentCaptor<Collection<Integer>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(Collection.class);
        verify(documentCommentRepository).deleteAllByDocumentAndBlockIdIn(eq(document), removedBlockIdsCaptor.capture());
        assertThat(removedBlockIdsCaptor.getValue()).containsExactly(2);

        ArgumentCaptor<or.hyu.ssd.domain.document.entity.DocumentLog> documentLogCaptor =
                ArgumentCaptor.forClass(or.hyu.ssd.domain.document.entity.DocumentLog.class);
        verify(documentLogRepository).save(documentLogCaptor.capture());
        assertThat(documentLogCaptor.getValue().getDeletedBlockCount()).isEqualTo(1);
        assertThat(documentLogCaptor.getValue().getCreatedBlockCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("updateDocument()는 수정 요청 문단의 blockId가 없거나 중복되면 거부한다")
    void updateDocument_rejectsInvalidBlockIds() {
        Member member = member(1L);
        CustomUserDetails user = new CustomUserDetails(member);
        Document document = document(42L, member);
        when(documentRepository.findById(42L)).thenReturn(Optional.of(document));
        when(documentParagraphRepository.findAllByDocumentOrderByPageNumberAscBlockIdAscIdAsc(document)).thenReturn(List.of());

        assertThatThrownBy(() -> documentService.updateDocument(
                42L,
                user,
                new CreateDocumentRequest(
                        null,
                        "본문",
                        List.of(new CreateDocumentParagraphRequest("문단", "#", null)),
                        0L
                )
        ))
                .isInstanceOf(or.hyu.ssd.global.api.handler.DomainException.class)
                .hasMessage("수정 요청의 모든 문단에는 blockId가 필요합니다");

        assertThatThrownBy(() -> documentService.updateDocument(
                42L,
                user,
                new CreateDocumentRequest(
                        null,
                        "본문",
                        List.of(
                                new CreateDocumentParagraphRequest("문단 1", "#", 1),
                                new CreateDocumentParagraphRequest("문단 2", "##", 1)
                        ),
                        0L
                )
        ))
                .isInstanceOf(or.hyu.ssd.global.api.handler.DomainException.class)
                .hasMessage("수정 요청에 중복된 blockId가 있습니다");
    }

    private Document document(Long id, Member member) {
        return Document.builder()
                .id(id)
                .title("문서 제목")
                .content("문서 본문")
                .bookmark(false)
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
