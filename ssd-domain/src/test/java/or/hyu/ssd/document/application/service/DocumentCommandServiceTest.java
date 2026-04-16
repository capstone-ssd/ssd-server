package or.hyu.ssd.document.application.service;

import or.hyu.ssd.document.domain.entity.Document;
import or.hyu.ssd.document.domain.entity.DocumentBlockType;
import or.hyu.ssd.document.domain.entity.DocumentLog;
import or.hyu.ssd.document.domain.entity.DocumentParagraph;
import or.hyu.ssd.document.repository.CheckListRepository;
import or.hyu.ssd.document.repository.DocumentAiCheckSnapshotRepository;
import or.hyu.ssd.document.repository.DocumentCommentRepository;
import or.hyu.ssd.document.repository.DocumentLogRepository;
import or.hyu.ssd.document.repository.DocumentParagraphRepository;
import or.hyu.ssd.document.repository.DocumentRepository;
import or.hyu.ssd.document.repository.EvaluatorCheckListRepository;
import or.hyu.ssd.document.repository.EvaluatorReviewRepository;
import or.hyu.ssd.document.repository.FolderRepository;
import or.hyu.ssd.document.application.support.DocumentImageResolver;
import or.hyu.ssd.document.application.support.DocumentImageUploadPart;
import or.hyu.ssd.document.application.command.CreateDocumentCommand;
import or.hyu.ssd.document.application.command.DocumentBlockCommand;
import or.hyu.ssd.document.application.command.UpdateDocumentCommand;
import or.hyu.ssd.document.application.result.CreateDocumentResult;
import or.hyu.ssd.member.domain.entity.Member;
import or.hyu.ssd.member.domain.entity.Role;
import or.hyu.ssd.member.application.service.CustomUserDetails;
import or.hyu.ssd.common.util.OptimisticRetryExecutor;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentCommandServiceTest {

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
    @Mock
    private DocumentImageResolver documentImageResolver;

    @InjectMocks
    private DocumentCommandService documentCommandService;

    @Test
    @DisplayName("createDocument()는 생성 요청 블록의 pageNumber를 1로 저장하고 blockId와 첫 문단 제목을 유지한다")
    void createDocument_defaultsPageNumberAndResolvesTitleFromParagraph() {
        // given
        CustomUserDetails user = user(1L);
        CreateDocumentCommand command = new CreateDocumentCommand(
                null,
                "   ",
                List.of(
                        paragraphBlock("첫 문단 제목", "#", 99),
                        paragraphBlock("둘째 문단", "", 100)
                ),
                0L
        );
        givenDocumentSaveReturns(42L);
        givenParagraphSaveReturnsInput();
        givenPassThroughBlobKeyReplacement();

        // when
        CreateDocumentResult result = documentCommandService.createDocument(user, command);

        // then
        ArgumentCaptor<Document> documentCaptor = ArgumentCaptor.forClass(Document.class);
        verify(documentRepository).save(documentCaptor.capture());
        assertThat(documentCaptor.getValue().getTitle()).isEqualTo("첫 문단 제목");

        assertThat(captureSavedParagraphs())
                .extracting(DocumentParagraph::getPageNumber, DocumentParagraph::getBlockId)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(1, 99),
                        org.assertj.core.groups.Tuple.tuple(1, 100)
                );
        assertThat(result.id()).isEqualTo(42L);
    }

    @Test
    @DisplayName("updateDocument()는 공백 제목을 거부한다")
    void updateDocument_rejectsBlankTitle() {
        // given
        Member member = member(1L);
        CustomUserDetails user = new CustomUserDetails(member);
        when(documentRepository.findById(42L)).thenReturn(Optional.of(document(42L, member)));

        // when
        ThrowingCallable action = () -> documentCommandService.updateDocument(
                42L,
                user,
                new UpdateDocumentCommand("   ", "본문", null)
        );

        // then
        assertThatThrownBy(action)
                .isInstanceOf(or.hyu.ssd.common.exception.DocumentException.class)
                .hasMessage("제목은 공백일 수 없습니다");
    }

    @Test
    @DisplayName("updateDocument()는 공백 본문을 거부한다")
    void updateDocument_rejectsBlankContent() {
        // given
        Member member = member(1L);
        CustomUserDetails user = new CustomUserDetails(member);
        when(documentRepository.findById(42L)).thenReturn(Optional.of(document(42L, member)));

        // when
        ThrowingCallable action = () -> documentCommandService.updateDocument(
                42L,
                user,
                new UpdateDocumentCommand(null, "   ", null)
        );

        // then
        assertThatThrownBy(action)
                .isInstanceOf(or.hyu.ssd.common.exception.DocumentException.class)
                .hasMessage("내용은 공백일 수 없습니다");
    }

    @Test
    @DisplayName("updateDocument()는 요청 blockId를 유지하고 삭제된 block 주석을 정리하며 변경 개수를 기록한다")
    void updateDocument_preservesBlockIdsAndDeletesRemovedComments() {
        // given
        Member member = member(1L);
        CustomUserDetails user = new CustomUserDetails(member);
        Document document = document(42L, member);
        when(documentRepository.findById(42L)).thenReturn(Optional.of(document));
        when(documentParagraphRepository.findBlocks(document)).thenReturn(List.of(
                paragraph(document, "기존 문단 1", "#", 1, 1),
                paragraph(document, "기존 문단 2", "##", 1, 2)
        ));
        givenParagraphSaveReturnsInput();
        givenPassThroughBlobKeyReplacement();

        // when
        documentCommandService.updateDocument(
                42L,
                user,
                new UpdateDocumentCommand(
                        null,
                        "새 본문",
                        List.of(
                                paragraphBlock("기존 문단 1 수정", "#", 1),
                                paragraphBlock("새 문단", "", 3)
                        )
                )
        );

        // then
        assertThat(document.getTitle()).isEqualTo("문서 제목");
        assertThat(document.getContent()).isEqualTo("새 본문");

        verify(documentParagraphRepository).deleteAllByDocument(document);
        verify(documentParagraphRepository).flush();
        assertThat(captureSavedParagraphs())
                .extracting(DocumentParagraph::getPageNumber, DocumentParagraph::getBlockId)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(1, 1),
                        org.assertj.core.groups.Tuple.tuple(1, 3)
                );

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<Integer>> removedBlockIdsCaptor =
                (ArgumentCaptor<Collection<Integer>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(Collection.class);
        verify(documentCommentRepository).deleteAllByDocumentAndBlockIdIn(eq(document), removedBlockIdsCaptor.capture());
        assertThat(removedBlockIdsCaptor.getValue()).containsExactly(2);

        ArgumentCaptor<DocumentLog> documentLogCaptor = ArgumentCaptor.forClass(DocumentLog.class);
        verify(documentLogRepository).save(documentLogCaptor.capture());
        assertThat(documentLogCaptor.getValue().getDeletedBlockCount()).isEqualTo(1);
        assertThat(documentLogCaptor.getValue().getCreatedBlockCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("updateDocument()는 blockId가 없는 수정 블록을 거부한다")
    void updateDocument_rejectsMissingBlockId() {
        // given
        Member member = member(1L);
        CustomUserDetails user = new CustomUserDetails(member);
        Document document = document(42L, member);
        when(documentRepository.findById(42L)).thenReturn(Optional.of(document));

        // when
        ThrowingCallable action = () -> documentCommandService.updateDocument(
                42L,
                user,
                new UpdateDocumentCommand(
                        null,
                        "본문",
                        List.of(paragraphWithoutBlockId("문단", "#"))
                )
        );

        // then
        assertThatThrownBy(action)
                .isInstanceOf(or.hyu.ssd.common.exception.DocumentException.class)
                .hasMessage("수정 요청의 모든 블록에는 blockId가 필요합니다");
    }

    @Test
    @DisplayName("updateDocument()는 중복된 수정 blockId를 거부한다")
    void updateDocument_rejectsDuplicateBlockIds() {
        // given
        Member member = member(1L);
        CustomUserDetails user = new CustomUserDetails(member);
        Document document = document(42L, member);
        when(documentRepository.findById(42L)).thenReturn(Optional.of(document));

        // when
        ThrowingCallable action = () -> documentCommandService.updateDocument(
                42L,
                user,
                new UpdateDocumentCommand(
                        null,
                        "본문",
                        List.of(
                                paragraphBlock("문단 1", "#", 1),
                                paragraphBlock("문단 2", "##", 1)
                        )
                )
        );

        // then
        assertThatThrownBy(action)
                .isInstanceOf(or.hyu.ssd.common.exception.DocumentException.class)
                .hasMessage("수정 요청에 중복된 blockId가 있습니다");
    }

    @Test
    @DisplayName("createDocument()는 이미지 블록의 blobKey를 S3 URL로 치환해 저장한다")
    void createDocument_uploadsImageBlocksAndPersistsImageType() {
        // given
        CustomUserDetails user = user(1L);
        CreateDocumentCommand command = new CreateDocumentCommand(
                "이미지 문서",
                "본문",
                List.of(
                        paragraphBlock("문단1", "#", 1),
                        imageBlock(2, "img-1")
                ),
                0L
        );
        givenDocumentSaveReturns(50L);
        givenParagraphSaveReturnsInput();
        when(documentImageResolver.resolveImageUrl(any(), anyInt(), any(), any()))
                .thenReturn("https://s3.example.com/documents/image.png");
        givenPassThroughBlobKeyReplacement();

        // when
        documentCommandService.createDocument(user, command, List.of(uploadPart("img-1", 2)));

        // then
        List<DocumentParagraph> savedParagraphs = captureSavedParagraphs();
        assertThat(savedParagraphs).hasSize(2);
        assertThat(savedParagraphs.get(1).getTypeOrDefault()).isEqualTo(DocumentBlockType.IMAGE);
        assertThat(savedParagraphs.get(1).getContent()).isEqualTo("https://s3.example.com/documents/image.png");
        assertThat(savedParagraphs.get(1).getBlockId()).isEqualTo(2);
        verify(documentRepository).save(any(Document.class));
        assertThat(command.text()).isEqualTo("본문");
    }

    @Test
    @DisplayName("createDocument()는 본문 text 안의 blobKey를 업로드된 S3 URL로 치환한다")
    void createDocument_replacesBlobKeyInsideText() {
        // given
        CustomUserDetails user = user(1L);
        CreateDocumentCommand command = new CreateDocumentCommand(
                "이미지 본문",
                "<img src=\"img-1\" />",
                List.of(imageBlock(2, "img-1")),
                0L
        );
        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(documentImageResolver.resolveImageUrl(any(), anyInt(), any(), any()))
                .thenReturn("https://s3.example.com/documents/image.png");
        when(documentImageResolver.replaceBlobKeys(any(), any()))
                .thenReturn("<img src=\"https://s3.example.com/documents/image.png\" />");

        // when
        documentCommandService.createDocument(user, command, List.of(uploadPart("img-1", 2)));

        // then
        ArgumentCaptor<Document> documentCaptor = ArgumentCaptor.forClass(Document.class);
        verify(documentRepository).save(documentCaptor.capture());
        assertThat(documentCaptor.getValue().getContent())
                .isEqualTo("<img src=\"https://s3.example.com/documents/image.png\" />");
    }

    private void givenDocumentSaveReturns(Long id) {
        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> {
            Document document = invocation.getArgument(0);
            return Document.builder()
                    .id(id)
                    .title(document.getTitle())
                    .content(document.getContent())
                    .folder(document.getFolder())
                    .bookmark(document.isBookmark())
                    .member(document.getMember())
                    .build();
        });
    }

    private void givenParagraphSaveReturnsInput() {
        when(documentParagraphRepository.saveAll(any())).thenAnswer(invocation -> {
            Iterable<DocumentParagraph> paragraphs = invocation.getArgument(0);
            List<DocumentParagraph> saved = new ArrayList<>();
            paragraphs.forEach(saved::add);
            return saved;
        });
    }

    private void givenPassThroughBlobKeyReplacement() {
        when(documentImageResolver.replaceBlobKeys(any(), any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private List<DocumentParagraph> captureSavedParagraphs() {
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Iterable<DocumentParagraph>> paragraphCaptor =
                (ArgumentCaptor<Iterable<DocumentParagraph>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(Iterable.class);
        verify(documentParagraphRepository).saveAll(paragraphCaptor.capture());

        List<DocumentParagraph> savedParagraphs = new ArrayList<>();
        paragraphCaptor.getValue().forEach(savedParagraphs::add);
        return savedParagraphs;
    }

    private CustomUserDetails user(Long memberId) {
        return new CustomUserDetails(member(memberId));
    }

    private DocumentParagraph paragraph(Document document, String content, String role, int pageNumber, int blockId) {
        return DocumentParagraph.of(DocumentBlockType.PARAGRAPH, content, role, pageNumber, blockId, document);
    }

    private DocumentBlockCommand paragraphBlock(String content, String role, Integer blockId) {
        return new DocumentBlockCommand(DocumentBlockType.PARAGRAPH, content, role, blockId, null, null);
    }

    private DocumentBlockCommand paragraphWithoutBlockId(String content, String role) {
        return new DocumentBlockCommand(DocumentBlockType.PARAGRAPH, content, role, null, null, null);
    }

    private DocumentBlockCommand imageBlock(Integer blockId, String blobKey) {
        return new DocumentBlockCommand(DocumentBlockType.IMAGE, null, null, blockId, blobKey, null);
    }

    private DocumentImageUploadPart uploadPart(String blobKey, int blockId) {
        return new DocumentImageUploadPart(blobKey, blockId, "image.png", "image/png", new byte[]{1, 2, 3});
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
