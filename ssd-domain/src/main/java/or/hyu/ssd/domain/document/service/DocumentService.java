package or.hyu.ssd.domain.document.service;
import lombok.RequiredArgsConstructor;
import or.hyu.ssd.domain.document.controller.dto.CreateDocumentParagraphRequest;
import or.hyu.ssd.domain.document.controller.dto.CreateDocumentRequest;
import or.hyu.ssd.domain.document.controller.dto.CreateDocumentResponse;
import or.hyu.ssd.domain.document.controller.dto.DocumentListItemResponse;
import or.hyu.ssd.domain.document.controller.dto.DocumentParagraphDto;
import or.hyu.ssd.domain.document.controller.dto.GetDocumentResponse;
import or.hyu.ssd.domain.document.controller.dto.UpdateDocumentResponse;
import or.hyu.ssd.domain.document.controller.dto.DocumentBookmarkResponse;
import or.hyu.ssd.domain.document.entity.Document;
import or.hyu.ssd.domain.document.entity.DocumentLog;
import or.hyu.ssd.domain.document.entity.DocumentParagraph;
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
import or.hyu.ssd.global.api.handler.UserExceptionHandler;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import or.hyu.ssd.global.util.OptimisticRetryExecutor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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

    public CreateDocumentResponse createDocument(CustomUserDetails user, CreateDocumentRequest req) {
        if (user == null || user.getMember() == null) {
            throw new UserExceptionHandler(ErrorCode.MEMBER_NOT_FOUND);
        }
        validateFolderId(req.folderId());

        String title = resolveTitle(req.title(), req.text(), req.paragraphs());
        Folder folder = resolveFolderOrNull(user, req.folderId());
        Document doc = Document.of(title, req.text(), folder, false, user.getMember());

        Document saved = documentRepository.save(doc);
        saveCreateParagraphsIfPresent(saved, req.paragraphs());
        saveDocumentLog(saved, user);
        return CreateDocumentResponse.of(saved.getId());
    }

    public UpdateDocumentResponse updateDocument(Long documentId, CustomUserDetails user, CreateDocumentRequest req) {
        Document doc = getDocument(documentId);

        if (doc.getMember() == null || user == null || user.getMember() == null) {
            throw new UserExceptionHandler(ErrorCode.DOCUMENT_FORBIDDEN);
        }
        if (!doc.getMember().getId().equals(user.getMember().getId())) {
            throw new UserExceptionHandler(ErrorCode.DOCUMENT_FORBIDDEN);
        }
        validateUpdateRequest(req);

        String updatedTitle = resolveUpdatedTitle(doc.getTitle(), req.title());
        doc.updateIfPresent(updatedTitle, req.text(), null, null, null);
        if (req.folderId() != null) {
            Folder folder = resolveFolderOrNull(user, req.folderId());
            doc.updateFolder(folder);
        }
        int deletedBlockCount = 0;
        int createdBlockCount = 0;
        if (req.paragraphs() != null) {
            BlockChangeSummary blockChangeSummary = replaceParagraphsAndSyncComments(doc, req.paragraphs());
            deletedBlockCount = blockChangeSummary.deletedBlockCount();
            createdBlockCount = blockChangeSummary.createdBlockCount();
        }
        saveDocumentLog(doc, user, deletedBlockCount, createdBlockCount);

        return UpdateDocumentResponse.of(doc.getId());
    }

    public void deleteDocument(Long documentId, CustomUserDetails user) {
        Document doc = getDocument(documentId);

        if (doc.getMember() == null || user == null || user.getMember() == null) {
            throw new UserExceptionHandler(ErrorCode.DOCUMENT_FORBIDDEN);
        }
        if (!doc.getMember().getId().equals(user.getMember().getId())) {
            throw new UserExceptionHandler(ErrorCode.DOCUMENT_FORBIDDEN);
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
            throw new UserExceptionHandler(ErrorCode.DOCUMENT_FORBIDDEN);
        }
        if (!doc.getMember().getId().equals(user.getMember().getId())) {
            throw new UserExceptionHandler(ErrorCode.DOCUMENT_FORBIDDEN);
        }

        List<DocumentParagraphDto> paragraphs = fetchParagraphs(doc);
        return GetDocumentResponse.of(doc, paragraphs);
    }

    @Transactional(readOnly = true)
    public List<DocumentListItemResponse> listDocuments(CustomUserDetails user, DocumentSort sortOption, Long folderId) {
        if (user == null || user.getMember() == null) {
            throw new UserExceptionHandler(ErrorCode.MEMBER_NOT_FOUND);
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
                throw new UserExceptionHandler(ErrorCode.DOCUMENT_FORBIDDEN);
            }
            if (!doc.getMember().getId().equals(user.getMember().getId())) {
                throw new UserExceptionHandler(ErrorCode.DOCUMENT_FORBIDDEN);
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
                .orElseThrow(() -> new UserExceptionHandler(ErrorCode.DOCUMENT_NOT_FOUND));
    }

    private Folder resolveFolderOrNull(CustomUserDetails user, Long folderId) {
        if (folderId == null || folderId == 0L) {
            return null;
        }
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new UserExceptionHandler(ErrorCode.FOLDER_NOT_FOUND));
        if (folder.getMember() == null || user == null || user.getMember() == null) {
            throw new UserExceptionHandler(ErrorCode.FOLDER_FORBIDDEN);
        }
        if (!folder.getMember().getId().equals(user.getMember().getId())) {
            throw new UserExceptionHandler(ErrorCode.FOLDER_FORBIDDEN);
        }
        return folder;
    }

    private String resolveTitle(String requestedTitle, String text, List<CreateDocumentParagraphRequest> paragraphs) {
        String title = trimOrNull(requestedTitle);
        if (title != null) {
            return title;
        }
        if (paragraphs != null && !paragraphs.isEmpty()) {
            String fromParagraph = trimOrNull(paragraphs.get(0).content());
            if (fromParagraph != null) {
                return fromParagraph;
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

    private void validateUpdateRequest(CreateDocumentRequest req) {
        if (req == null) {
            throw new UserExceptionHandler(ErrorCode.REQUEST_BODY_INVALID_VALUE, "수정 요청 본문이 비어 있습니다");
        }
        if (isBlankProvided(req.title())) {
            throw new UserExceptionHandler(ErrorCode.REQUEST_BODY_INVALID_VALUE, "제목은 공백일 수 없습니다");
        }
        if (req.text() == null || req.text().trim().isEmpty()) {
            throw new UserExceptionHandler(ErrorCode.REQUEST_BODY_INVALID_VALUE, "내용은 공백일 수 없습니다");
        }
        validateFolderId(req.folderId());
    }

    private void validateFolderId(Long folderId) {
        if (folderId != null && folderId < 0L) {
            throw new UserExceptionHandler(ErrorCode.REQUEST_BODY_INVALID_VALUE, "폴더 ID는 0 이상이어야 합니다");
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

    private void saveCreateParagraphsIfPresent(Document doc, List<CreateDocumentParagraphRequest> paragraphs) {
        if (paragraphs == null || paragraphs.isEmpty()) {
            return;
        }
        List<DocumentParagraph> entities = new ArrayList<>(paragraphs.size());

        // 생성 API는 pageNumber를 받지 않으므로 모든 문단을 1페이지로 저장합니다.
        int blockId = 1;
        for (CreateDocumentParagraphRequest p : paragraphs) {
            entities.add(DocumentParagraph.of(p.content(), p.role(), 1, blockId++, doc));
        }
        documentParagraphRepository.saveAll(entities);
    }

    private BlockChangeSummary replaceParagraphsAndSyncComments(Document doc, List<CreateDocumentParagraphRequest> paragraphs) {
        List<DocumentParagraph> existingParagraphs = documentParagraphRepository.findAllByDocumentOrderByPageNumberAscBlockIdAscIdAsc(doc);
        Set<Integer> existingBlockIds = existingParagraphs.stream()
                .map(DocumentParagraph::getBlockId)
                .collect(Collectors.toCollection(HashSet::new));
        Set<Integer> requestedBlockIds = validateAndCollectRequestedBlockIds(paragraphs);

        Set<Integer> removedBlockIds = new HashSet<>(existingBlockIds);
        removedBlockIds.removeAll(requestedBlockIds);

        int createdBlockCount = 0;
        for (Integer requestedBlockId : requestedBlockIds) {
            if (!existingBlockIds.contains(requestedBlockId)) {
                createdBlockCount++;
            }
        }

        documentParagraphRepository.deleteAllByDocument(doc);
        saveUpdatedParagraphs(doc, paragraphs);

        if (!removedBlockIds.isEmpty()) {
            documentCommentRepository.deleteAllByDocumentAndBlockIdIn(doc, removedBlockIds);
        }

        return new BlockChangeSummary(removedBlockIds.size(), createdBlockCount);
    }

    private Set<Integer> validateAndCollectRequestedBlockIds(List<CreateDocumentParagraphRequest> paragraphs) {
        Set<Integer> requestedBlockIds = new LinkedHashSet<>();
        if (paragraphs == null) {
            return requestedBlockIds;
        }

        for (CreateDocumentParagraphRequest paragraph : paragraphs) {
            Integer blockId = paragraph.blockId();
            if (blockId == null) {
                throw new UserExceptionHandler(ErrorCode.REQUEST_BODY_INVALID_VALUE, "수정 요청의 모든 문단에는 blockId가 필요합니다");
            }
            if (blockId <= 0) {
                throw new UserExceptionHandler(ErrorCode.REQUEST_BODY_INVALID_VALUE, "blockId는 1 이상이어야 합니다");
            }
            if (!requestedBlockIds.add(blockId)) {
                throw new UserExceptionHandler(ErrorCode.REQUEST_BODY_INVALID_VALUE, "수정 요청에 중복된 blockId가 있습니다");
            }
        }
        return requestedBlockIds;
    }

    private void saveUpdatedParagraphs(Document doc, List<CreateDocumentParagraphRequest> paragraphs) {
        if (paragraphs == null || paragraphs.isEmpty()) {
            return;
        }

        List<DocumentParagraph> entities = new ArrayList<>(paragraphs.size());
        for (CreateDocumentParagraphRequest paragraph : paragraphs) {
            entities.add(DocumentParagraph.of(paragraph.content(), paragraph.role(), 1, paragraph.blockId(), doc));
        }
        documentParagraphRepository.saveAll(entities);
    }

    private List<DocumentParagraphDto> fetchParagraphs(Document doc) {
        return documentParagraphRepository.findAllByDocumentOrderByPageNumberAscBlockIdAscIdAsc(doc).stream()
                .map(p -> new DocumentParagraphDto(p.getContent(), p.getRole(), p.getPageNumber(), p.getBlockId()))
                .collect(Collectors.toList());
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
}
