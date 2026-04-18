package or.hyu.ssd.document.application.service;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.document.domain.model.Document;
import or.hyu.ssd.document.domain.model.DocumentBlockType;
import or.hyu.ssd.document.domain.model.DocumentLog;
import or.hyu.ssd.document.domain.model.DocumentParagraph;
import or.hyu.ssd.document.domain.model.Folder;
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
import or.hyu.ssd.document.application.result.DocumentBookmarkResult;
import or.hyu.ssd.document.application.result.UpdateDocumentResult;
import or.hyu.ssd.member.domain.model.Member;
import or.hyu.ssd.member.repository.MemberRepository;
import or.hyu.ssd.common.exception.ErrorCode;
import or.hyu.ssd.common.exception.DocumentException;
import or.hyu.ssd.document.application.support.OptimisticRetryExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class DocumentCommandService {

    private final DocumentRepository documentRepository;
    private final CheckListRepository checkListRepository;
    private final EvaluatorCheckListRepository evaluatorCheckListRepository;
    private final DocumentAiCheckSnapshotRepository documentAiCheckSnapshotRepository;
    private final DocumentParagraphRepository documentParagraphRepository;
    private final DocumentCommentRepository documentCommentRepository;
    private final DocumentLogRepository documentLogRepository;
    private final EvaluatorReviewRepository evaluatorReviewRepository;
    private final FolderRepository folderRepository;
    private final MemberRepository memberRepository;
    private final OptimisticRetryExecutor optimisticRetryExecutor;
    private final DocumentImageResolver documentImageResolver;

    public CreateDocumentResult createDocument(Long memberId, CreateDocumentCommand command) {
        return createDocument(memberId, command, List.of());
    }

    public CreateDocumentResult createDocument(
            Long memberId,
            CreateDocumentCommand command,
            List<DocumentImageUploadPart> imageUploadParts
    ) {
        assertAuthenticatedMember(memberId);
        validateFolderId(command.folderId());
        Member member = getMember(memberId);

        List<ResolvedDocumentBlock> resolvedBlocks = resolveCreateBlocks(
                command.blocks(),
                imageUploadParts,
                memberId
        );
        String resolvedText = documentImageResolver.replaceBlobKeys(command.text(), collectUploadedImageUrls(resolvedBlocks));
        String title = resolveTitle(command.title(), resolvedText, resolvedBlocks);
        Folder folder = resolveFolderOrNull(memberId, command.folderId());
        Document document = Document.of(title, resolvedText, folder, false, member);

        Document saved = documentRepository.save(document);
        saveCreateParagraphsIfPresent(saved, resolvedBlocks);
        saveDocumentLog(saved, member);
        return CreateDocumentResult.of(saved.getId());
    }

    public UpdateDocumentResult updateDocument(Long documentId, Long memberId, UpdateDocumentCommand command) {
        return updateDocument(documentId, memberId, command, List.of());
    }

    public UpdateDocumentResult updateDocument(
            Long documentId,
            Long memberId,
            UpdateDocumentCommand command,
            List<DocumentImageUploadPart> imageUploadParts
    ) {
        Document document = loadDocument(documentId);
        assertDocumentOwner(document, memberId);
        validateUpdateRequest(command);
        Member member = getMember(memberId);

        List<ResolvedDocumentBlock> resolvedBlocks = resolveUpdateBlocks(
                command.blocks(),
                imageUploadParts,
                memberId
        );
        String resolvedText = documentImageResolver.replaceBlobKeys(command.text(), collectUploadedImageUrls(resolvedBlocks));
        String updatedTitle = resolveUpdatedTitle(document.getTitle(), command.title());
        document.updateIfPresent(updatedTitle, resolvedText, null, null, null);

        int deletedBlockCount = 0;
        int createdBlockCount = 0;
        if (command.blocks() != null) {
            BlockChangeSummary blockChangeSummary = replaceParagraphsAndSyncComments(document, resolvedBlocks);
            deletedBlockCount = blockChangeSummary.deletedBlockCount();
            createdBlockCount = blockChangeSummary.createdBlockCount();
        }
        saveDocumentLog(document, member, deletedBlockCount, createdBlockCount);

        return UpdateDocumentResult.of(document.getId());
    }

    public void deleteDocument(Long documentId, Long memberId) {
        Document document = loadDocument(documentId);
        assertDocumentOwner(document, memberId);

        checkListRepository.deleteAllByDocument(document);
        evaluatorCheckListRepository.deleteAllByDocument(document);
        documentAiCheckSnapshotRepository.deleteAllByDocument(document);
        documentParagraphRepository.deleteAllByDocument(document);
        documentCommentRepository.deleteAllByDocument(document);
        documentLogRepository.deleteAllByDocument(document);
        evaluatorReviewRepository.deleteAllByDocument(document);
        documentRepository.delete(document);
    }

    public DocumentBookmarkResult toggleBookmark(Long documentId, Long memberId) {
        return optimisticRetryExecutor.execute(3, () -> {
            Document document = loadDocument(documentId);
            assertDocumentOwner(document, memberId);

            boolean newValue = !document.isBookmark();
            document.updateIfPresent(null, null, null, null, newValue);
            documentRepository.flush();
            return DocumentBookmarkResult.of(document.getId(), document.isBookmark());
        });
    }

    private Document loadDocument(Long documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentException(ErrorCode.DOCUMENT_NOT_FOUND));
    }

    private void assertAuthenticatedMember(Long memberId) {
        if (memberId == null) {
            throw new DocumentException(ErrorCode.MEMBER_NOT_FOUND);
        }
    }

    private void assertDocumentOwner(Document document, Long memberId) {
        if (document.getMember() == null || memberId == null) {
            throw new DocumentException(ErrorCode.DOCUMENT_FORBIDDEN);
        }
        if (!document.getMember().getId().equals(memberId)) {
            throw new DocumentException(ErrorCode.DOCUMENT_FORBIDDEN);
        }
    }

    private Folder resolveFolderOrNull(Long memberId, Long folderId) {
        if (folderId == null || folderId == 0L) {
            return null;
        }

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new DocumentException(ErrorCode.FOLDER_NOT_FOUND));
        if (folder.getMember() == null || memberId == null) {
            throw new DocumentException(ErrorCode.FOLDER_FORBIDDEN);
        }
        if (!folder.getMember().getId().equals(memberId)) {
            throw new DocumentException(ErrorCode.FOLDER_FORBIDDEN);
        }
        return folder;
    }

    private String resolveTitle(String requestedTitle, String text, List<ResolvedDocumentBlock> blocks) {
        String title = trimOrNull(requestedTitle);
        if (title != null) {
            return title;
        }
        if (blocks != null && !blocks.isEmpty()) {
            for (ResolvedDocumentBlock block : blocks) {
                if (block.type().isParagraph()) {
                    String fromParagraph = trimOrNull(block.content());
                    if (fromParagraph != null) {
                        return fromParagraph;
                    }
                }
            }
        }
        String fromText = extractFirstLine(text);
        if (fromText != null) {
            return fromText;
        }
        return "Untitled";
    }

    private String resolveUpdatedTitle(String currentTitle, String requestedTitle) {
        String title = trimOrNull(requestedTitle);
        return title != null ? title : currentTitle;
    }

    private void validateUpdateRequest(UpdateDocumentCommand command) {
        if (command == null) {
            throw new DocumentException(ErrorCode.REQUEST_BODY_INVALID_VALUE, "수정 요청 본문이 비어 있습니다");
        }
        if (isBlankProvided(command.title())) {
            throw new DocumentException(ErrorCode.REQUEST_BODY_INVALID_VALUE, "제목은 공백일 수 없습니다");
        }
        if (command.text() == null || command.text().trim().isEmpty()) {
            throw new DocumentException(ErrorCode.REQUEST_BODY_INVALID_VALUE, "내용은 공백일 수 없습니다");
        }
    }

    private void validateFolderId(Long folderId) {
        if (folderId != null && folderId < 0L) {
            throw new DocumentException(ErrorCode.REQUEST_BODY_INVALID_VALUE, "폴더 ID는 0 이상이어야 합니다");
        }
    }

    private boolean isBlankProvided(String value) {
        return value != null && value.trim().isEmpty();
    }

    private String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String extractFirstLine(String text) {
        if (text == null) {
            return null;
        }
        for (String line : text.split("\\R")) {
            String cleaned = line.trim();
            if (cleaned.isEmpty()) {
                continue;
            }
            cleaned = cleaned.replaceFirst("^#+\\s*", "").trim();
            if (!cleaned.isEmpty()) {
                return cleaned;
            }
        }
        return null;
    }

    private void saveCreateParagraphsIfPresent(Document document, List<ResolvedDocumentBlock> blocks) {
        if (blocks == null || blocks.isEmpty()) {
            return;
        }
        List<DocumentParagraph> entities = new ArrayList<>(blocks.size());
        for (ResolvedDocumentBlock block : blocks) {
            entities.add(DocumentParagraph.of(block.type(), block.content(), block.role(), 1, block.blockId(), document));
        }
        documentParagraphRepository.saveAll(entities);
    }

    private BlockChangeSummary replaceParagraphsAndSyncComments(Document document, List<ResolvedDocumentBlock> blocks) {
        List<DocumentParagraph> existingParagraphs = documentParagraphRepository.findBlocks(document);
        Set<Integer> existingBlockIds = existingParagraphs.stream()
                .map(DocumentParagraph::getBlockId)
                .collect(Collectors.toCollection(HashSet::new));
        Set<Integer> requestedBlockIds = validateAndCollectRequestedBlockIds(blocks);

        Set<Integer> removedBlockIds = new HashSet<>(existingBlockIds);
        removedBlockIds.removeAll(requestedBlockIds);

        int createdBlockCount = 0;
        for (Integer requestedBlockId : requestedBlockIds) {
            if (!existingBlockIds.contains(requestedBlockId)) {
                createdBlockCount++;
            }
        }

        documentParagraphRepository.deleteAllByDocument(document);
        documentParagraphRepository.flush();
        saveUpdatedParagraphs(document, blocks);

        if (!removedBlockIds.isEmpty()) {
            documentCommentRepository.deleteAllByDocumentAndBlockIdIn(document, removedBlockIds);
        }

        return new BlockChangeSummary(removedBlockIds.size(), createdBlockCount);
    }

    private Set<Integer> validateAndCollectRequestedBlockIds(List<ResolvedDocumentBlock> blocks) {
        Set<Integer> requestedBlockIds = new LinkedHashSet<>();
        if (blocks == null) {
            return requestedBlockIds;
        }

        for (ResolvedDocumentBlock block : blocks) {
            Integer blockId = block.blockId();
            if (blockId == null) {
                throw new DocumentException(ErrorCode.REQUEST_BODY_INVALID_VALUE, "수정 요청의 모든 문단에는 blockId가 필요합니다");
            }
            if (blockId <= 0) {
                throw new DocumentException(ErrorCode.REQUEST_BODY_INVALID_VALUE, "blockId는 1 이상이어야 합니다");
            }
            if (!requestedBlockIds.add(blockId)) {
                throw new DocumentException(ErrorCode.REQUEST_BODY_INVALID_VALUE, "수정 요청에 중복된 blockId가 있습니다");
            }
        }
        return requestedBlockIds;
    }

    private void saveUpdatedParagraphs(Document document, List<ResolvedDocumentBlock> blocks) {
        if (blocks == null || blocks.isEmpty()) {
            return;
        }

        List<DocumentParagraph> entities = new ArrayList<>(blocks.size());
        for (ResolvedDocumentBlock block : blocks) {
            entities.add(DocumentParagraph.of(block.type(), block.content(), block.role(), 1, block.blockId(), document));
        }
        documentParagraphRepository.saveAll(entities);
    }

    private List<ResolvedDocumentBlock> resolveCreateBlocks(
            List<DocumentBlockCommand> blocks,
            List<DocumentImageUploadPart> imageUploadParts,
            Long memberId
    ) {
        if (blocks == null || blocks.isEmpty()) {
            return List.of();
        }

        Map<String, DocumentImageUploadPart> imageUploadsByBlobKey = documentImageResolver.indexUploadParts(imageUploadParts);
        List<ResolvedDocumentBlock> resolvedBlocks = new ArrayList<>(blocks.size());
        Set<Integer> usedBlockIds = new LinkedHashSet<>();
        int nextBlockId = 1;

        for (DocumentBlockCommand block : blocks) {
            int resolvedBlockId = block.blockId() != null
                    ? block.blockId()
                    : nextAvailableBlockId(usedBlockIds, nextBlockId);
            nextBlockId = Math.max(nextBlockId, resolvedBlockId + 1);

            if (resolvedBlockId <= 0 || !usedBlockIds.add(resolvedBlockId)) {
                throw new DocumentException(ErrorCode.REQUEST_BODY_INVALID_VALUE, "생성 요청에 잘못된 blockId가 있습니다");
            }

            resolvedBlocks.add(resolveBlock(block, resolvedBlockId, imageUploadsByBlobKey, memberId));
        }

        documentImageResolver.validateAllMapped(imageUploadsByBlobKey, collectResolvedBlobKeys(resolvedBlocks));
        return resolvedBlocks;
    }

    private List<ResolvedDocumentBlock> resolveUpdateBlocks(
            List<DocumentBlockCommand> blocks,
            List<DocumentImageUploadPart> imageUploadParts,
            Long memberId
    ) {
        if (blocks == null || blocks.isEmpty()) {
            return List.of();
        }

        Map<String, DocumentImageUploadPart> imageUploadsByBlobKey = documentImageResolver.indexUploadParts(imageUploadParts);
        List<ResolvedDocumentBlock> resolvedBlocks = new ArrayList<>(blocks.size());

        for (DocumentBlockCommand block : blocks) {
            if (block.blockId() == null) {
                throw new DocumentException(ErrorCode.REQUEST_BODY_INVALID_VALUE, "수정 요청의 모든 블록에는 blockId가 필요합니다");
            }
            resolvedBlocks.add(resolveBlock(block, block.blockId(), imageUploadsByBlobKey, memberId));
        }

        documentImageResolver.validateAllMapped(imageUploadsByBlobKey, collectResolvedBlobKeys(resolvedBlocks));
        return resolvedBlocks;
    }

    private ResolvedDocumentBlock resolveBlock(
            DocumentBlockCommand block,
            int blockId,
            Map<String, DocumentImageUploadPart> imageUploadsByBlobKey,
            Long memberId
    ) {
        DocumentBlockType type = block.resolvedType();
        if (type.isParagraph()) {
            return new ResolvedDocumentBlock(type, block.content(), block.role(), blockId, null);
        }

        String uploadedUrl = documentImageResolver.resolveImageUrl(block, blockId, imageUploadsByBlobKey, memberId);
        return new ResolvedDocumentBlock(type, uploadedUrl, null, blockId, trimOrNull(block.blobKey()));
    }

    private int nextAvailableBlockId(Set<Integer> usedBlockIds, int candidate) {
        int next = candidate;
        while (usedBlockIds.contains(next)) {
            next++;
        }
        return next;
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    private Set<String> collectResolvedBlobKeys(List<ResolvedDocumentBlock> resolvedBlocks) {
        return resolvedBlocks.stream()
                .map(ResolvedDocumentBlock::blobKey)
                .filter(this::isNotBlank)
                .collect(Collectors.toSet());
    }

    private Map<String, String> collectUploadedImageUrls(List<ResolvedDocumentBlock> resolvedBlocks) {
        return resolvedBlocks.stream()
                .filter(block -> block.type().isImage() && isNotBlank(block.blobKey()) && isNotBlank(block.content()))
                .collect(Collectors.toMap(
                        ResolvedDocumentBlock::blobKey,
                        ResolvedDocumentBlock::content,
                        (left, right) -> right
                ));
    }

    private void saveDocumentLog(Document document, Member member) {
        saveDocumentLog(document, member, 0, 0);
    }

    private void saveDocumentLog(Document document, Member member, int deletedBlockCount, int createdBlockCount) {
        String editorName = resolveEditorName(member);
        documentLogRepository.save(
                DocumentLog.of(editorName, resolveEditorEmail(member), deletedBlockCount, createdBlockCount, document)
        );
    }

    private String resolveEditorName(Member member) {
        if (member == null) {
            return "Unknown";
        }
        String name = member.getName();
        if (name != null && !name.isBlank()) {
            return name.trim();
        }
        String email = member.getEmail();
        if (email != null && !email.isBlank()) {
            return email.trim();
        }
        return "Unknown";
    }

    private String resolveEditorEmail(Member member) {
        if (member == null) {
            return null;
        }
        String email = member.getEmail();
        return email == null || email.isBlank() ? null : email.trim();
    }

    private Member getMember(Long memberId) {
        assertAuthenticatedMember(memberId);
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new DocumentException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private record BlockChangeSummary(int deletedBlockCount, int createdBlockCount) {
    }

    private record ResolvedDocumentBlock(
            DocumentBlockType type,
            String content,
            String role,
            Integer blockId,
            String blobKey
    ) {
    }
}
