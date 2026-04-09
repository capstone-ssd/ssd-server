package or.hyu.ssd.domain.document.service;

import or.hyu.ssd.domain.document.entity.DocumentBlockType;
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
import or.hyu.ssd.domain.document.service.support.DocumentImageResolver;
import or.hyu.ssd.domain.document.service.support.DocumentImageUploadPart;
import or.hyu.ssd.domain.document.usecase.command.CreateDocumentCommand;
import or.hyu.ssd.domain.document.usecase.command.DocumentBlockCommand;
import or.hyu.ssd.domain.document.usecase.command.UpdateDocumentCommand;
import or.hyu.ssd.domain.document.usecase.result.CreateDocumentResult;
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
import static org.mockito.ArgumentMatchers.anyInt;
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
    @Mock
    private DocumentImageResolver documentImageResolver;

    @InjectMocks
    private DocumentService documentService;

    @Test
    @DisplayName("createDocument()는 생성 요청 블록의 pageNumber를 1로 저장하고 blockId와 첫 문단 제목을 유지한다")
    void createDocument_defaultsPageNumberAndResolvesTitleFromParagraph() {
        Member member = member(1L);
        CustomUserDetails user = new CustomUserDetails(member);
        CreateDocumentCommand command = new CreateDocumentCommand(
                null,
                "   ",
                List.of(
                        new DocumentBlockCommand(DocumentBlockType.PARAGRAPH, "첫 문단 제목", "#", 99, null, null),
                        new DocumentBlockCommand(DocumentBlockType.PARAGRAPH, "둘째 문단", "", 100, null, null)
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
        when(documentImageResolver.replaceBlobKeys(any(), any())).thenAnswer(invocation -> invocation.getArgument(0));

        CreateDocumentResult result = documentService.createDocument(user, command);

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
                .containsExactly(99, 100);
        assertThat(result.id()).isEqualTo(42L);
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
                new UpdateDocumentCommand(
                        "   ", "본문", null
                )
        ))
                .isInstanceOf(or.hyu.ssd.global.api.handler.DocumentException.class)
                .hasMessage("제목은 공백일 수 없습니다");

        assertThatThrownBy(() -> documentService.updateDocument(
                42L,
                user,
                new UpdateDocumentCommand(
                        null, "   ", null
                )
        ))
                .isInstanceOf(or.hyu.ssd.global.api.handler.DocumentException.class)
                .hasMessage("내용은 공백일 수 없습니다");
    }

    @Test
    @DisplayName("updateDocument()는 요청 blockId를 유지하고 삭제된 block 주석을 정리하며 변경 개수를 기록한다")
    void updateDocument_preservesBlockIdsAndDeletesRemovedComments() {
        Member member = member(1L);
        CustomUserDetails user = new CustomUserDetails(member);
        Document document = document(42L, member);
        when(documentRepository.findById(42L)).thenReturn(Optional.of(document));
        when(documentParagraphRepository.findBlocks(document))
                .thenReturn(List.of(
                        DocumentParagraph.of(DocumentBlockType.PARAGRAPH, "기존 문단 1", "#", 1, 1, document),
                        DocumentParagraph.of(DocumentBlockType.PARAGRAPH, "기존 문단 2", "##", 1, 2, document)
                ));
        when(documentParagraphRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(documentImageResolver.replaceBlobKeys(any(), any())).thenAnswer(invocation -> invocation.getArgument(0));

        documentService.updateDocument(
                42L,
                user,
                new UpdateDocumentCommand(
                        null,
                        "새 본문",
                        List.of(
                                new DocumentBlockCommand(DocumentBlockType.PARAGRAPH, "기존 문단 1 수정", "#", 1, null, null),
                                new DocumentBlockCommand(DocumentBlockType.PARAGRAPH, "새 문단", "", 3, null, null)
                        )
                )
        );

        assertThat(document.getTitle()).isEqualTo("문서 제목");
        assertThat(document.getContent()).isEqualTo("새 본문");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Iterable<DocumentParagraph>> paragraphCaptor =
                (ArgumentCaptor<Iterable<DocumentParagraph>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(Iterable.class);
        verify(documentParagraphRepository).deleteAllByDocument(document);
        verify(documentParagraphRepository).flush();
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
        when(documentParagraphRepository.findBlocks(document)).thenReturn(List.of());
        when(documentImageResolver.replaceBlobKeys(any(), any())).thenAnswer(invocation -> invocation.getArgument(0));

        assertThatThrownBy(() -> documentService.updateDocument(
                42L,
                user,
                new UpdateDocumentCommand(
                        null,
                        "본문",
                        List.of(new DocumentBlockCommand(DocumentBlockType.PARAGRAPH, "문단", "#", null, null, null))
                )
        ))
                .isInstanceOf(or.hyu.ssd.global.api.handler.DocumentException.class)
                .hasMessage("수정 요청의 모든 블록에는 blockId가 필요합니다");

        assertThatThrownBy(() -> documentService.updateDocument(
                42L,
                user,
                new UpdateDocumentCommand(
                        null,
                        "본문",
                        List.of(
                                new DocumentBlockCommand(DocumentBlockType.PARAGRAPH, "문단 1", "#", 1, null, null),
                                new DocumentBlockCommand(DocumentBlockType.PARAGRAPH, "문단 2", "##", 1, null, null)
                        )
                )
        ))
                .isInstanceOf(or.hyu.ssd.global.api.handler.DocumentException.class)
                .hasMessage("수정 요청에 중복된 blockId가 있습니다");
    }

    @Test
    @DisplayName("createDocument()는 이미지 블록의 blobKey를 S3 URL로 치환해 저장한다")
    void createDocument_uploadsImageBlocksAndPersistsImageType() {
        Member member = member(1L);
        CustomUserDetails user = new CustomUserDetails(member);
        CreateDocumentCommand command = new CreateDocumentCommand(
                "이미지 문서",
                "본문",
                List.of(
                        new DocumentBlockCommand(DocumentBlockType.PARAGRAPH, "문단1", "#", 1, null, null),
                        new DocumentBlockCommand(DocumentBlockType.IMAGE, null, null, 2, "img-1", null)
                ),
                0L
        );

        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> {
            Document doc = invocation.getArgument(0);
            return Document.builder()
                    .id(50L)
                    .title(doc.getTitle())
                    .content(doc.getContent())
                    .folder(doc.getFolder())
                    .bookmark(doc.isBookmark())
                    .member(doc.getMember())
                    .build();
        });
        when(documentParagraphRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(documentImageResolver.resolveImageUrl(any(), anyInt(), any(), any())).thenReturn("https://s3.example.com/documents/image.png");
        when(documentImageResolver.replaceBlobKeys(any(), any())).thenAnswer(invocation -> invocation.getArgument(0));

        documentService.createDocument(
                user,
                command,
                List.of(new DocumentImageUploadPart("img-1", 2, "image.png", "image/png", new byte[]{1, 2, 3}))
        );

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Iterable<DocumentParagraph>> paragraphCaptor =
                (ArgumentCaptor<Iterable<DocumentParagraph>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(Iterable.class);
        verify(documentParagraphRepository).saveAll(paragraphCaptor.capture());

        List<DocumentParagraph> savedParagraphs = new ArrayList<>();
        paragraphCaptor.getValue().forEach(savedParagraphs::add);

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
        Member member = member(1L);
        CustomUserDetails user = new CustomUserDetails(member);
        CreateDocumentCommand command = new CreateDocumentCommand(
                "이미지 본문",
                "<img src=\"img-1\" />",
                List.of(new DocumentBlockCommand(DocumentBlockType.IMAGE, null, null, 2, "img-1", null)),
                0L
        );

        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(documentImageResolver.resolveImageUrl(any(), anyInt(), any(), any())).thenReturn("https://s3.example.com/documents/image.png");
        when(documentImageResolver.replaceBlobKeys(any(), any())).thenReturn("<img src=\"https://s3.example.com/documents/image.png\" />");

        documentService.createDocument(
                user,
                command,
                List.of(new DocumentImageUploadPart("img-1", 2, "image.png", "image/png", new byte[]{1, 2, 3}))
        );

        ArgumentCaptor<Document> documentCaptor = ArgumentCaptor.forClass(Document.class);
        verify(documentRepository).save(documentCaptor.capture());
        assertThat(documentCaptor.getValue().getContent()).isEqualTo("<img src=\"https://s3.example.com/documents/image.png\" />");
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
