package or.hyu.ssd.domain.document.service;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.domain.document.repository.DocumentRepository;
import or.hyu.ssd.domain.document.repository.CheckListRepository;
import or.hyu.ssd.domain.document.entity.Document;
import or.hyu.ssd.domain.member.service.CustomUserDetails;
import or.hyu.ssd.domain.document.controller.dto.CreateDocumentRequest;
import or.hyu.ssd.domain.document.controller.dto.UpdateDocumentRequest;
import or.hyu.ssd.domain.document.controller.dto.GetDocumentResponse;
import or.hyu.ssd.domain.document.controller.dto.CreateDocumentResponse;
import or.hyu.ssd.domain.document.controller.dto.UpdateDocumentResponse;
import or.hyu.ssd.domain.document.controller.dto.DocumentListItemResponse;
import or.hyu.ssd.domain.document.service.support.DocumentSort;
import or.hyu.ssd.global.api.ErrorCode;
import or.hyu.ssd.global.api.handler.UserExceptionHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final CheckListRepository checkListRepository;


    public CreateDocumentResponse createDocument(CustomUserDetails user, CreateDocumentRequest req) {
        boolean bookmark = req.bookmark() != null ? req.bookmark() : false;

        Document doc = Document.of(req.content(), req.summary(), req.details(), bookmark,user.getMember());

        Document saved = documentRepository.save(doc);
        return CreateDocumentResponse.of(saved.getId());
    }


    public UpdateDocumentResponse updateDocument(Long documentId, CustomUserDetails user, UpdateDocumentRequest req) {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new UserExceptionHandler(ErrorCode.DOCUMENT_NOT_FOUND));

        if (doc.getMember() == null || user == null || user.getMember() == null) {
            throw new UserExceptionHandler(ErrorCode.DOCUMENT_FORBIDDEN);
        }
        if (!doc.getMember().getId().equals(user.getMember().getId())) {
            throw new UserExceptionHandler(ErrorCode.DOCUMENT_FORBIDDEN);
        }

        doc.updateIfPresent(req.content(), req.summary(), req.details(), req.bookmark());

        return UpdateDocumentResponse.of(doc.getId());
    }


    public void deleteDocument(Long documentId, CustomUserDetails user) {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new UserExceptionHandler(ErrorCode.DOCUMENT_NOT_FOUND));

        if (doc.getMember() == null || user == null || user.getMember() == null) {
            throw new UserExceptionHandler(ErrorCode.DOCUMENT_FORBIDDEN);
        }
        if (!doc.getMember().getId().equals(user.getMember().getId())) {
            throw new UserExceptionHandler(ErrorCode.DOCUMENT_FORBIDDEN);
        }

        // 하위 체크리스트 먼저 제거(FK 제약 보호)
        checkListRepository.deleteAllByDocument(doc);

        // 문서 삭제
        documentRepository.delete(doc);
    }


    @Transactional(readOnly = true)
    public GetDocumentResponse getDocument(Long documentId, CustomUserDetails user) {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new UserExceptionHandler(ErrorCode.DOCUMENT_NOT_FOUND));

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
            case NAME -> Sort.by(Sort.Order.asc("summary")); // 이름이 없으므로 summary 기준으로 정렬
            case MODIFIED -> Sort.by(Sort.Order.desc("updatedAt"));
        };

        return documentRepository.findAllByMember_Id(user.getMember().getId(), sort)
                .stream()
                .map(DocumentListItemResponse::of)
                .collect(Collectors.toList());
    }



    // 문서 조회 API
    // 문서 전체 조회 API
}
