package or.hyu.ssd.domain.document.service;
import lombok.RequiredArgsConstructor;
import or.hyu.ssd.domain.document.controller.dto.CreateDocumentRequest;
import or.hyu.ssd.domain.document.controller.dto.CreateDocumentResponse;
import or.hyu.ssd.domain.document.controller.dto.DocumentListItemResponse;
import or.hyu.ssd.domain.document.controller.dto.DocumentLogResponse;
import or.hyu.ssd.domain.document.controller.dto.DocumentParagraphDto;
import or.hyu.ssd.domain.document.controller.dto.GetDocumentResponse;
import or.hyu.ssd.domain.document.controller.dto.UpdateDocumentRequest;
import or.hyu.ssd.domain.document.controller.dto.UpdateDocumentResponse;
import or.hyu.ssd.domain.document.controller.dto.DocumentBookmarkResponse;
import or.hyu.ssd.domain.document.entity.Document;
import or.hyu.ssd.domain.document.entity.DocumentLog;
import or.hyu.ssd.domain.document.entity.DocumentParagraph;
import or.hyu.ssd.domain.document.repository.CheckListRepository;
import or.hyu.ssd.domain.document.repository.DocumentLogRepository;
import or.hyu.ssd.domain.document.repository.DocumentParagraphRepository;
import or.hyu.ssd.domain.document.repository.EvaluatorCheckListRepository;
import or.hyu.ssd.domain.document.repository.DocumentRepository;
import or.hyu.ssd.domain.document.service.support.DocumentSort;
import or.hyu.ssd.domain.member.service.CustomUserDetails;
import or.hyu.ssd.global.api.ErrorCode;
import or.hyu.ssd.global.api.handler.UserExceptionHandler;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import or.hyu.ssd.global.util.OptimisticRetryExecutor;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final CheckListRepository checkListRepository;
    private final EvaluatorCheckListRepository evaluatorCheckListRepository;
    private final DocumentParagraphRepository documentParagraphRepository;
    private final DocumentLogRepository documentLogRepository;
    private final OptimisticRetryExecutor optimisticRetryExecutor;

    public CreateDocumentResponse createDocument(CustomUserDetails user, CreateDocumentRequest req) {

        String normalizedPath = normalizePath(req.path());
        String title = resolveTitle(req.title(), req.text(), req.paragraphs());
        Document doc = Document.of(title, req.text(), normalizedPath, false, user.getMember());

        Document saved = documentRepository.save(doc);
        saveParagraphsIfPresent(saved, req.paragraphs());
        saveDocumentLog(saved, user);
        return CreateDocumentResponse.of(saved.getId());
    }

    public UpdateDocumentResponse updateDocument(Long documentId, CustomUserDetails user, UpdateDocumentRequest req) {
        Document doc = getDocument(documentId);

        if (doc.getMember() == null || user == null || user.getMember() == null) {
            throw new UserExceptionHandler(ErrorCode.DOCUMENT_FORBIDDEN);
        }
        if (!doc.getMember().getId().equals(user.getMember().getId())) {
            throw new UserExceptionHandler(ErrorCode.DOCUMENT_FORBIDDEN);
        }

        String normalizedPath = normalizePathOrNull(req.path());
        doc.updateIfPresent(req.title(), req.text(), req.summary(), req.details(), normalizedPath, req.bookmark());
        if (req.paragraphs() != null) {
            documentParagraphRepository.deleteAllByDocument(doc);
            saveParagraphsIfPresent(doc, req.paragraphs());
        }
        saveDocumentLog(doc, user);

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
        documentParagraphRepository.deleteAllByDocument(doc);
        documentLogRepository.deleteAllByDocument(doc);
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
    public DocumentLogResponse listDocumentLogs(Long documentId, CustomUserDetails user) {
        Document doc = getDocument(documentId);

        if (doc.getMember() == null || user == null || user.getMember() == null) {
            throw new UserExceptionHandler(ErrorCode.DOCUMENT_FORBIDDEN);
        }
        if (!doc.getMember().getId().equals(user.getMember().getId())) {
            throw new UserExceptionHandler(ErrorCode.DOCUMENT_FORBIDDEN);
        }

        List<DocumentLog> logs = documentLogRepository.findAllByDocumentOrderByCreatedAtAsc(doc);
        return DocumentLogResponse.of(doc.getId(), logs);
    }

    @Transactional(readOnly = true)
    public List<DocumentListItemResponse> listDocuments(CustomUserDetails user, DocumentSort sortOption) {
        if (user == null || user.getMember() == null) {
            throw new UserExceptionHandler(ErrorCode.MEMBER_NOT_FOUND);
        }

        Sort sort = switch (sortOption) {
            case LATEST -> Sort.by(Sort.Order.desc("createdAt"));
            case OLDEST -> Sort.by(Sort.Order.asc("createdAt"));
            case NAME -> Sort.by(Sort.Order.asc("title"));
            case MODIFIED -> Sort.by(Sort.Order.desc("updatedAt"));
        };

        return documentRepository.findAllByMember_Id(user.getMember().getId(), sort)
                .stream()
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
            doc.updateIfPresent(null, null, null, null, null, newVal);
            documentRepository.flush();
            return DocumentBookmarkResponse.of(doc.getId(), doc.isBookmark());
        });
        return result;
    }


    

    private Document getDocument(Long documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new UserExceptionHandler(ErrorCode.DOCUMENT_NOT_FOUND));
    }

    private String normalizePathOrNull(String rawPath) {
        if (rawPath == null) {
            return null;
        }
        return normalizePath(rawPath);
    }

    private String normalizePath(String rawPath) {

        // null이면 루트 경로로 반환
        if (rawPath == null) {
            return "/";
        }

        // null이 아니라면 앞뒤 공백 제거 후 비어있다면 루트 경로 반환
        String path = rawPath.trim();
        if (path.isEmpty()) {
            return "/";
        }

        // 백슬래시와 슬래시 동기화 후 //가 있다면 /으로 축약
        path = path.replace('\\', '/');
        while (path.contains("//")) {
            path = path.replace("//", "/");
        }

        if ("/".equals(path)) {
            return "/";
        }

        // 앞뒤 슬래시 제거 후 항상 선행 슬래시를 붙인다
        while (path.startsWith("/")) {
            path = path.substring(1);
        }
        while (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        if (path.isEmpty()) {
            return "/";
        }
        return "/" + path;
    }

    private String resolveTitle(String requestedTitle, String text, List<DocumentParagraphDto> paragraphs) {
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

    private void saveParagraphsIfPresent(Document doc, List<DocumentParagraphDto> paragraphs) {
        if (paragraphs == null || paragraphs.isEmpty()) {
            return;
        }
        List<DocumentParagraph> entities = paragraphs.stream()
                .map(p -> DocumentParagraph.of(p.content(), p.role(), p.pageNumber(), p.blockId(), doc))
                .collect(Collectors.toList());
        documentParagraphRepository.saveAll(entities);
    }

    private List<DocumentParagraphDto> fetchParagraphs(Document doc) {
        return documentParagraphRepository.findAllByDocumentOrderByPageNumberAscBlockIdAscIdAsc(doc).stream()
                .map(p -> new DocumentParagraphDto(p.getContent(), p.getRole(), p.getPageNumber(), p.getBlockId()))
                .collect(Collectors.toList());
    }

    private void saveDocumentLog(Document doc, CustomUserDetails user) {
        String editorName = resolveEditorName(user);
        documentLogRepository.save(DocumentLog.of(editorName, doc));
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
}
