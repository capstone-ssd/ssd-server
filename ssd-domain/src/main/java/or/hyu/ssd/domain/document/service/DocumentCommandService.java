package or.hyu.ssd.domain.document.service;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.domain.document.entity.Document;
import or.hyu.ssd.domain.document.entity.DocumentBlockType;
import or.hyu.ssd.domain.document.entity.DocumentLog;
import or.hyu.ssd.domain.document.entity.DocumentParagraph;
import or.hyu.ssd.domain.document.entity.Folder;
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
import or.hyu.ssd.domain.document.usecase.result.DocumentBookmarkResult;
import or.hyu.ssd.domain.document.usecase.result.UpdateDocumentResult;
import or.hyu.ssd.domain.member.service.CustomUserDetails;
import or.hyu.ssd.common.exception.ErrorCode;
import or.hyu.ssd.common.exception.DocumentException;
import or.hyu.ssd.common.util.OptimisticRetryExecutor;
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
    private final OptimisticRetryExecutor optimisticRetryExecutor;
    private final DocumentImageResolver documentImageResolver;

    public CreateDocumentResult createDocument(CustomUserDetails user, CreateDocumentCommand command) {
        return createDocument(user, command, List.of());
    }

    public CreateDocumentResult createDocument(
            CustomUserDetails user,
            CreateDocumentCommand command,
            List<DocumentImageUploadPart> imageUploadParts
    ) {
        assertAuthenticatedMember(user);
        validateFolderId(command.folderId());

        List<ResolvedDocumentBlock> resolvedBlocks = resolveCreateBlocks(
                command.blocks(),
                imageUploadParts,
                user.getMember().getId()
        );
        String resolvedText = documentImageResolver.replaceBlobKeys(command.text(), collectUploadedImageUrls(resolvedBlocks));
        String title = resolveTitle(command.title(), resolvedText, resolvedBlocks);
        Folder folder = resolveFolderOrNull(user, command.folderId());
        Document document = Document.of(title, resolvedText, folder, false, user.getMember());

        Document saved = documentRepository.save(document);
        saveCreateParagraphsIfPresent(saved, resolvedBlocks);
        saveDocumentLog(saved, user);
        return CreateDocumentResult.of(saved.getId());
    }

    public UpdateDocumentResult updateDocument(Long documentId, CustomUserDetails user, UpdateDocumentCommand command) {
        return updateDocument(documentId, user, command, List.of());
    }

    public UpdateDocumentResult updateDocument(
            Long documentId,
            CustomUserDetails user,
            UpdateDocumentCommand command,
            List<DocumentImageUploadPart> imageUploadParts
    ) {
        Document document = loadDocument(documentId);
        assertDocumentOwner(document, user);
        validateUpdateRequest(command);

        List<ResolvedDocumentBlock> resolvedBlocks = resolveUpdateBlocks(
                command.blocks(),
                imageUploadParts,
                user.getMember().getId()
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
        saveDocumentLog(document, user, deletedBlockCount, createdBlockCount);

        return UpdateDocumentResult.of(document.getId());
    }

    public void deleteDocument(Long documentId, CustomUserDetails user) {
        Document document = loadDocument(documentId);
        assertDocumentOwner(document, user);

        checkListRepository.deleteAllByDocument(document);
        evaluatorCheckListRepository.deleteAllByDocument(document);
        documentAiCheckSnapshotRepository.deleteAllByDocument(document);
        documentParagraphRepository.deleteAllByDocument(document);
        documentCommentRepository.deleteAllByDocument(document);
        documentLogRepository.deleteAllByDocument(document);
        evaluatorReviewRepository.deleteAllByDocument(document);
        documentRepository.delete(document);
    }

    public DocumentBookmarkResult toggleBookmark(Long documentId, CustomUserDetails user) {
        return optimisticRetryExecutor.execute(3, () -> {
            Document document = loadDocument(documentId);
            assertDocumentOwner(document, user);

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

    private void assertAuthenticatedMember(CustomUserDetails user) {
        if (user == null || user.getMember() == null) {
            throw new DocumentException(ErrorCode.MEMBER_NOT_FOUND);
        }
    }

    private void assertDocumentOwner(Document document, CustomUserDetails user) {
        if (document.getMember() == null || user == null || user.getMember() == null) {
            throw new DocumentException(ErrorCode.DOCUMENT_FORBIDDEN);
        }
        if (!document.getMember().getId().equals(user.getMember().getId())) {
            throw new DocumentException(ErrorCode.DOCUMENT_FORBIDDEN);
        }
    }

    private Folder resolveFolderOrNull(CustomUserDetails user, Long folderId) {
        if (folderId == null || folderId == 0L) {
            return null;
        }

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new DocumentException(ErrorCode.FOLDER_NOT_FOUND));
        if (folder.getMember() == null || user == null || user.getMember() == null) {
            throw new DocumentException(ErrorCode.FOLDER_FORBIDDEN);
        }
        if (!folder.getMember().getId().equals(user.getMember().getId())) {
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

    private void saveDocumentLog(Document document, CustomUserDetails user) {
        saveDocumentLog(document, user, 0, 0);
    }

    private void saveDocumentLog(Document document, CustomUserDetails user, int deletedBlockCount, int createdBlockCount) {
        String editorName = resolveEditorName(user);
        documentLogRepository.save(
                DocumentLog.of(editorName, resolveEditorEmail(user), deletedBlockCount, createdBlockCount, document)
        );
    }

    private String resolveEditorName(CustomUserDetails user) {
        if (user == null || user.getMember() == null) {
            return "Unknown";
        }
        String name = user.getMember().getName();
        if (name != null && !name.isBlank()) {
            return name.trim();
        }
        String email = user.getMember().getEmail();
        if (email != null && !email.isBlank()) {
            return email.trim();
        }
        return "Unknown";
    }

    private String resolveEditorEmail(CustomUserDetails user) {
        if (user == null || user.getMember() == null) {
            return null;
        }
        String email = user.getMember().getEmail();
        return email == null || email.isBlank() ? null : email.trim();
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
