package or.hyu.ssd.domain.document.service;
import lombok.RequiredArgsConstructor;
import or.hyu.ssd.domain.document.controller.dto.CreateDocumentRequest;
import or.hyu.ssd.domain.document.controller.dto.CreateDocumentResponse;
import or.hyu.ssd.domain.document.controller.dto.DocumentListItemResponse;
import or.hyu.ssd.domain.document.controller.dto.GetDocumentResponse;
import or.hyu.ssd.domain.document.controller.dto.UpdateDocumentRequest;
import or.hyu.ssd.domain.document.controller.dto.UpdateDocumentResponse;
import or.hyu.ssd.domain.document.controller.dto.DocumentBookmarkResponse;
import or.hyu.ssd.domain.document.entity.Document;
import or.hyu.ssd.domain.document.repository.CheckListRepository;
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
    private final OptimisticRetryExecutor optimisticRetryExecutor;

    public CreateDocumentResponse createDocument(CustomUserDetails user, CreateDocumentRequest req) {

        String normalizedPath = normalizePath(req.path());
        Document doc = Document.of(req.title(), req.content(), normalizedPath, false, user.getMember());

        Document saved = documentRepository.save(doc);
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
        doc.updateIfPresent(req.title(), req.content(), req.summary(), req.details(), normalizedPath, req.bookmark());

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

        return GetDocumentResponse.of(doc);
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
}
