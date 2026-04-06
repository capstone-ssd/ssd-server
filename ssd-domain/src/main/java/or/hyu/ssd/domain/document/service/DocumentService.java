package or.hyu.ssd.domain.document.service;
import lombok.RequiredArgsConstructor;
import or.hyu.ssd.domain.document.controller.dto.DocumentImageUploadPart;
import or.hyu.ssd.domain.document.controller.dto.CreateDocumentParagraphRequest;
import or.hyu.ssd.domain.document.controller.dto.CreateDocumentRequest;
import or.hyu.ssd.domain.document.controller.dto.CreateDocumentResponse;
import or.hyu.ssd.domain.document.controller.dto.DocumentListItemResponse;
import or.hyu.ssd.domain.document.controller.dto.DocumentParagraphDto;
import or.hyu.ssd.domain.document.controller.dto.GetDocumentResponse;
import or.hyu.ssd.domain.document.controller.dto.UpdateDocumentRequest;
import or.hyu.ssd.domain.document.controller.dto.UpdateDocumentResponse;
import or.hyu.ssd.domain.document.controller.dto.DocumentBookmarkResponse;
import or.hyu.ssd.domain.document.entity.DocumentBlockType;
import or.hyu.ssd.domain.document.entity.Document;
import or.hyu.ssd.domain.document.entity.DocumentLog;
import or.hyu.ssd.domain.document.entity.DocumentParagraph;
import or.hyu.ssd.domain.document.port.DocumentImageStoragePort;
import or.hyu.ssd.domain.document.entity.Folder;
import or.hyu.ssd.domain.document.repository.CheckListRepository;
import or.hyu.ssd.domain.document.repository.DocumentAiCheckSnapshotRepository;
import or.hyu.ssd.domain.document.repository.DocumentCommentRepository;
import or.hyu.ssd.domain.document.repository.DocumentLogRepository;
import or.hyu.ssd.domain.document.repository.DocumentParagraphRepository;
import or.hyu.ssd.domain.document.repository.EvaluatorCheckListRepository;
import or.hyu.ssd.domain.document.repository.EvaluatorReviewRepository;
import or.hyu.ssd.domain.document.repository.FolderRepository;
import or.hyu.ssd.domain.document.repository.DocumentRepository;
import or.hyu.ssd.domain.document.service.support.DocumentSort;
import or.hyu.ssd.domain.member.service.CustomUserDetails;
import or.hyu.ssd.global.api.ErrorCode;
import or.hyu.ssd.global.api.handler.DocumentException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import or.hyu.ssd.global.util.OptimisticRetryExecutor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class DocumentService {

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
    private final DocumentImageStoragePort documentImageStoragePort;

    public CreateDocumentResponse createDocument(CustomUserDetails user, CreateDocumentRequest req) {
        return createDocument(user, req, List.of());
    }

    public CreateDocumentResponse createDocument(CustomUserDetails user, CreateDocumentRequest req, List<DocumentImageUploadPart> imageUploadParts) {
        if (user == null || user.getMember() == null) {
            throw new DocumentException(ErrorCode.MEMBER_NOT_FOUND);
        }
        validateFolderId(req.folderId());

        List<ResolvedDocumentBlock> resolvedBlocks = resolveCreateBlocks(req.paragraphs(), imageUploadParts, user.getMember().getId());
        String resolvedText = replaceImagePlaceholders(req.text(), resolvedBlocks);
        String title = resolveTitle(req.title(), resolvedText, resolvedBlocks);
        Folder folder = resolveFolderOrNull(user, req.folderId());
        Document doc = Document.of(title, resolvedText, folder, false, user.getMember());

        Document saved = documentRepository.save(doc);
        saveCreateParagraphsIfPresent(saved, resolvedBlocks);
        saveDocumentLog(saved, user);
        return CreateDocumentResponse.of(saved.getId());
    }

    public UpdateDocumentResponse updateDocument(Long documentId, CustomUserDetails user, UpdateDocumentRequest req) {
        return updateDocument(documentId, user, req, List.of());
    }

    public UpdateDocumentResponse updateDocument(Long documentId, CustomUserDetails user, UpdateDocumentRequest req, List<DocumentImageUploadPart> imageUploadParts) {
        Document doc = getDocument(documentId);

        if (doc.getMember() == null || user == null || user.getMember() == null) {
            throw new DocumentException(ErrorCode.DOCUMENT_FORBIDDEN);
        }
        if (!doc.getMember().getId().equals(user.getMember().getId())) {
            throw new DocumentException(ErrorCode.DOCUMENT_FORBIDDEN);
        }
        validateUpdateRequest(req);

        List<ResolvedDocumentBlock> resolvedBlocks = resolveUpdateBlocks(req.paragraphs(), imageUploadParts, user.getMember().getId());
        String resolvedText = replaceImagePlaceholders(req.text(), resolvedBlocks);
        String updatedTitle = resolveUpdatedTitle(doc.getTitle(), req.title());
        doc.updateIfPresent(updatedTitle, resolvedText, null, null, null);
        int deletedBlockCount = 0;
        int createdBlockCount = 0;
        if (req.paragraphs() != null) {
            BlockChangeSummary blockChangeSummary = replaceParagraphsAndSyncComments(doc, resolvedBlocks);
            deletedBlockCount = blockChangeSummary.deletedBlockCount();
            createdBlockCount = blockChangeSummary.createdBlockCount();
        }
        saveDocumentLog(doc, user, deletedBlockCount, createdBlockCount);

        return UpdateDocumentResponse.of(doc.getId());
    }

    public void deleteDocument(Long documentId, CustomUserDetails user) {
        Document doc = getDocument(documentId);

        if (doc.getMember() == null || user == null || user.getMember() == null) {
            throw new DocumentException(ErrorCode.DOCUMENT_FORBIDDEN);
        }
        if (!doc.getMember().getId().equals(user.getMember().getId())) {
            throw new DocumentException(ErrorCode.DOCUMENT_FORBIDDEN);
        }

        checkListRepository.deleteAllByDocument(doc);
        evaluatorCheckListRepository.deleteAllByDocument(doc);
        documentAiCheckSnapshotRepository.deleteAllByDocument(doc);
        documentParagraphRepository.deleteAllByDocument(doc);
        documentCommentRepository.deleteAllByDocument(doc);
        documentLogRepository.deleteAllByDocument(doc);
        evaluatorReviewRepository.deleteAllByDocument(doc);
        documentRepository.delete(doc);
    }

    @Transactional(readOnly = true)
    public GetDocumentResponse getDocument(Long documentId, CustomUserDetails user) {
        Document doc = getDocument(documentId);

        if (doc.getMember() == null || user == null || user.getMember() == null) {
            throw new DocumentException(ErrorCode.DOCUMENT_FORBIDDEN);
        }
        if (!doc.getMember().getId().equals(user.getMember().getId())) {
            throw new DocumentException(ErrorCode.DOCUMENT_FORBIDDEN);
        }

        List<DocumentParagraphDto> paragraphs = fetchParagraphs(doc);
        return GetDocumentResponse.of(doc, paragraphs);
    }

    @Transactional(readOnly = true)
    public List<DocumentListItemResponse> listDocuments(CustomUserDetails user, DocumentSort sortOption, Long folderId) {
        if (user == null || user.getMember() == null) {
            throw new DocumentException(ErrorCode.MEMBER_NOT_FOUND);
        }

        Sort sort = switch (sortOption) {
            case LATEST -> Sort.by(Sort.Order.desc("createdAt"));
            case OLDEST -> Sort.by(Sort.Order.asc("createdAt"));
            case NAME -> Sort.by(Sort.Order.asc("title"));
            case MODIFIED -> Sort.by(Sort.Order.desc("updatedAt"));
        };
        List<Document> documents;
        Long memberId = user.getMember().getId();

        if (folderId == null) {
            documents = documentRepository.findAllByMember_Id(memberId, sort);
        } else if (folderId == 0L) {
            documents = documentRepository.findAllByMember_IdAndFolderIsNull(memberId, sort);
        } else {
            Folder folder = resolveFolderOrNull(user, folderId);
            documents = documentRepository.findAllByMember_IdAndFolder_Id(memberId, folder.getId(), sort);
        }

        return documents.stream()
                .map(DocumentListItemResponse::of)
                .collect(Collectors.toList());
    }

    public DocumentBookmarkResponse toggleBookmark(Long documentId, CustomUserDetails user) {
        DocumentBookmarkResponse result = optimisticRetryExecutor.execute(3, () -> {
            Document doc = getDocument(documentId);

            if (doc.getMember() == null || user == null || user.getMember() == null) {
                throw new DocumentException(ErrorCode.DOCUMENT_FORBIDDEN);
            }
            if (!doc.getMember().getId().equals(user.getMember().getId())) {
                throw new DocumentException(ErrorCode.DOCUMENT_FORBIDDEN);
            }

            boolean newVal = !doc.isBookmark();
            doc.updateIfPresent(null, null, null, null, newVal);
            documentRepository.flush();
            return DocumentBookmarkResponse.of(doc.getId(), doc.isBookmark());
        });
        return result;
    }


    

    private Document getDocument(Long documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentException(ErrorCode.DOCUMENT_NOT_FOUND));
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

    private void validateUpdateRequest(UpdateDocumentRequest req) {
        if (req == null) {
            throw new DocumentException(ErrorCode.REQUEST_BODY_INVALID_VALUE, "수정 요청 본문이 비어 있습니다");
        }
        if (isBlankProvided(req.title())) {
            throw new DocumentException(ErrorCode.REQUEST_BODY_INVALID_VALUE, "제목은 공백일 수 없습니다");
        }
        if (req.text() == null || req.text().trim().isEmpty()) {
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

    private void saveCreateParagraphsIfPresent(Document doc, List<ResolvedDocumentBlock> blocks) {
        if (blocks == null || blocks.isEmpty()) {
            return;
        }
        List<DocumentParagraph> entities = new ArrayList<>(blocks.size());
        for (ResolvedDocumentBlock block : blocks) {
            entities.add(DocumentParagraph.of(block.type(), block.content(), block.role(), 1, block.blockId(), doc));
        }
        documentParagraphRepository.saveAll(entities);
    }

    private BlockChangeSummary replaceParagraphsAndSyncComments(Document doc, List<ResolvedDocumentBlock> blocks) {
        List<DocumentParagraph> existingParagraphs = documentParagraphRepository.findAllByDocumentOrderByPageNumberAscBlockIdAscIdAsc(doc);
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

        documentParagraphRepository.deleteAllByDocument(doc);
        documentParagraphRepository.flush();
        saveUpdatedParagraphs(doc, blocks);

        if (!removedBlockIds.isEmpty()) {
            documentCommentRepository.deleteAllByDocumentAndBlockIdIn(doc, removedBlockIds);
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

    private void saveUpdatedParagraphs(Document doc, List<ResolvedDocumentBlock> blocks) {
        if (blocks == null || blocks.isEmpty()) {
            return;
        }

        List<DocumentParagraph> entities = new ArrayList<>(blocks.size());
        for (ResolvedDocumentBlock block : blocks) {
            entities.add(DocumentParagraph.of(block.type(), block.content(), block.role(), 1, block.blockId(), doc));
        }
        documentParagraphRepository.saveAll(entities);
    }

    private List<DocumentParagraphDto> fetchParagraphs(Document doc) {
        return documentParagraphRepository.findAllByDocumentOrderByPageNumberAscBlockIdAscIdAsc(doc).stream()
                .map(p -> p.isImageBlock()
                        ? new DocumentParagraphDto(p.getTypeOrDefault(), null, null, p.getPageNumber(), p.getBlockId(), p.getContent())
                        : new DocumentParagraphDto(p.getTypeOrDefault(), p.getContent(), p.getRole(), p.getPageNumber(), p.getBlockId(), null))
                .collect(Collectors.toList());
    }

    private List<ResolvedDocumentBlock> resolveCreateBlocks(
            List<CreateDocumentParagraphRequest> blocks,
            List<DocumentImageUploadPart> imageUploadParts,
            Long memberId
    ) {
        if (blocks == null || blocks.isEmpty()) {
            return List.of();
        }

        Map<String, DocumentImageUploadPart> imageUploadsByBlobKey = validateAndIndexImageUploads(imageUploadParts);
        List<ResolvedDocumentBlock> resolvedBlocks = new ArrayList<>(blocks.size());
        Set<Integer> usedBlockIds = new LinkedHashSet<>();
        int nextBlockId = 1;

        for (CreateDocumentParagraphRequest block : blocks) {
            int resolvedBlockId = block.blockId() != null
                    ? block.blockId()
                    : nextAvailableBlockId(usedBlockIds, nextBlockId);
            nextBlockId = Math.max(nextBlockId, resolvedBlockId + 1);

            if (resolvedBlockId <= 0 || !usedBlockIds.add(resolvedBlockId)) {
                throw new DocumentException(ErrorCode.REQUEST_BODY_INVALID_VALUE, "생성 요청에 잘못된 blockId가 있습니다");
            }

            resolvedBlocks.add(resolveBlock(block, resolvedBlockId, imageUploadsByBlobKey, memberId));
        }

        validateAllImageUploadsMapped(imageUploadsByBlobKey, resolvedBlocks);
        return resolvedBlocks;
    }

    private List<ResolvedDocumentBlock> resolveUpdateBlocks(
            List<CreateDocumentParagraphRequest> blocks,
            List<DocumentImageUploadPart> imageUploadParts,
            Long memberId
    ) {
        if (blocks == null || blocks.isEmpty()) {
            return List.of();
        }

        Map<String, DocumentImageUploadPart> imageUploadsByBlobKey = validateAndIndexImageUploads(imageUploadParts);
        List<ResolvedDocumentBlock> resolvedBlocks = new ArrayList<>(blocks.size());

        for (CreateDocumentParagraphRequest block : blocks) {
            if (block.blockId() == null) {
                throw new DocumentException(ErrorCode.REQUEST_BODY_INVALID_VALUE, "수정 요청의 모든 블록에는 blockId가 필요합니다");
            }
            resolvedBlocks.add(resolveBlock(block, block.blockId(), imageUploadsByBlobKey, memberId));
        }

        validateAllImageUploadsMapped(imageUploadsByBlobKey, resolvedBlocks);
        return resolvedBlocks;
    }

    private ResolvedDocumentBlock resolveBlock(
            CreateDocumentParagraphRequest block,
            int blockId,
            Map<String, DocumentImageUploadPart> imageUploadsByBlobKey,
            Long memberId
    ) {
        DocumentBlockType type = block.resolvedType();
        if (type.isParagraph()) {
            return new ResolvedDocumentBlock(type, block.content(), block.role(), blockId, null);
        }

        String imageUrl = trimOrNull(block.url());
        if (imageUrl != null) {
            return new ResolvedDocumentBlock(type, imageUrl, null, blockId, null);
        }

        String blobKey = trimOrNull(block.blobKey());
        if (blobKey == null) {
            throw new DocumentException(ErrorCode.REQUEST_BODY_INVALID_VALUE, "이미지 블록에는 blobKey 또는 url이 필요합니다");
        }

        DocumentImageUploadPart imageUploadPart = imageUploadsByBlobKey.get(blobKey);
        if (imageUploadPart == null) {
            throw new DocumentException(ErrorCode.REQUEST_BODY_INVALID_VALUE, "blobKey에 해당하는 이미지 파일이 없습니다: " + blobKey);
        }
        if (imageUploadPart.blockId() != null && imageUploadPart.blockId() != blockId) {
            throw new DocumentException(ErrorCode.REQUEST_BODY_INVALID_VALUE, "imageMetas의 blockId와 요청 블록의 blockId가 일치하지 않습니다: " + blobKey);
        }

        String storageKey = buildImageStorageKey(memberId, blockId, imageUploadPart.originalFilename());
        String uploadedUrl = documentImageStoragePort.upload(storageKey, imageUploadPart.bytes(), imageUploadPart.contentType());
        return new ResolvedDocumentBlock(type, uploadedUrl, null, blockId, blobKey);
    }

    private Map<String, DocumentImageUploadPart> validateAndIndexImageUploads(List<DocumentImageUploadPart> imageUploadParts) {
        if (imageUploadParts == null || imageUploadParts.isEmpty()) {
            return Map.of();
        }

        return imageUploadParts.stream()
                .peek(part -> {
                    if (trimOrNull(part.blobKey()) == null) {
                        throw new DocumentException(ErrorCode.REQUEST_BODY_INVALID_VALUE, "imageMetas의 blobKey는 필수입니다");
                    }
                    if (part.blockId() == null || part.blockId() <= 0) {
                        throw new DocumentException(ErrorCode.REQUEST_BODY_INVALID_VALUE, "imageMetas의 blockId는 1 이상이어야 합니다");
                    }
                    if (part.bytes() == null || part.bytes().length == 0) {
                        throw new DocumentException(ErrorCode.REQUEST_BODY_INVALID_VALUE, "이미지 파일이 비어 있습니다");
                    }
                    if (part.contentType() == null || !part.contentType().startsWith("image/")) {
                        throw new DocumentException(ErrorCode.REQUEST_MEDIA_TYPE_NOT_SUPPORTED, "이미지 파일만 업로드할 수 있습니다");
                    }
                })
                .collect(Collectors.toMap(
                        DocumentImageUploadPart::blobKey,
                        part -> part,
                        (left, right) -> {
                            throw new DocumentException(ErrorCode.REQUEST_BODY_INVALID_VALUE, "중복된 blobKey가 있습니다: " + left.blobKey());
                        }
                ));
    }

    private void validateAllImageUploadsMapped(
            Map<String, DocumentImageUploadPart> imageUploadsByBlobKey,
            List<ResolvedDocumentBlock> resolvedBlocks
    ) {
        if (imageUploadsByBlobKey.isEmpty()) {
            return;
        }

        Set<String> resolvedBlobKeys = resolvedBlocks.stream()
                .map(ResolvedDocumentBlock::blobKey)
                .filter(this::isNotBlank)
                .collect(Collectors.toSet());

        for (String blobKey : imageUploadsByBlobKey.keySet()) {
            if (!resolvedBlobKeys.contains(blobKey)) {
                throw new DocumentException(ErrorCode.REQUEST_BODY_INVALID_VALUE, "본문에 없는 blobKey가 imageMetas로 전달되었습니다: " + blobKey);
            }
        }
    }

    private int nextAvailableBlockId(Set<Integer> usedBlockIds, int candidate) {
        int next = candidate;
        while (usedBlockIds.contains(next)) {
            next++;
        }
        return next;
    }

    private String buildImageStorageKey(Long memberId, int blockId, String originalFilename) {
        String extension = "";
        if (isNotBlank(originalFilename) && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
        }
        return "documents/%d/%s-block-%d%s".formatted(memberId, UUID.randomUUID(), blockId, extension);
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    private String replaceImagePlaceholders(String text, List<ResolvedDocumentBlock> resolvedBlocks) {
        if (text == null || text.isBlank() || resolvedBlocks == null || resolvedBlocks.isEmpty()) {
            return text;
        }

        String resolvedText = text;
        for (ResolvedDocumentBlock block : resolvedBlocks) {
            if (!block.type().isImage() || !isNotBlank(block.blobKey()) || !isNotBlank(block.content())) {
                continue;
            }
            resolvedText = resolvedText.replace(block.blobKey(), block.content());
        }
        return resolvedText;
    }

    private void saveDocumentLog(Document doc, CustomUserDetails user) {
        saveDocumentLog(doc, user, 0, 0);
    }

    private void saveDocumentLog(Document doc, CustomUserDetails user, int deletedBlockCount, int createdBlockCount) {
        String editorName = resolveEditorName(user);
        documentLogRepository.save(DocumentLog.of(editorName, resolveEditorEmail(user), deletedBlockCount, createdBlockCount, doc));
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
