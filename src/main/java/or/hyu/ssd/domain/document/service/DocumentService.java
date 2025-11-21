package or.hyu.ssd.domain.document.service;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.domain.document.repository.DocumentRepository;
import or.hyu.ssd.domain.document.repository.CheckListRepository;
import or.hyu.ssd.domain.document.entity.Document;
import or.hyu.ssd.domain.member.service.CustomUserDetails;
import or.hyu.ssd.domain.document.controller.dto.CreateDocumentRequest;
import or.hyu.ssd.domain.document.controller.dto.UpdateDocumentRequest;
import or.hyu.ssd.global.api.ErrorCode;
import or.hyu.ssd.global.api.handler.UserExceptionHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final CheckListRepository checkListRepository;


    public Long createDocument(CustomUserDetails user, CreateDocumentRequest req) {
        boolean bookmark = req.bookmark() != null ? req.bookmark() : false;

        Document doc = Document.of(req.content(), req.summary(), req.details(), bookmark,user.getMember());

        Document saved = documentRepository.save(doc);
        return saved.getId();
    }


    public Long updateDocument(Long documentId, CustomUserDetails user, UpdateDocumentRequest req) {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new UserExceptionHandler(ErrorCode.DOCUMENT_NOT_FOUND));

        if (doc.getMember() == null || user == null || user.getMember() == null) {
            throw new UserExceptionHandler(ErrorCode.DOCUMENT_FORBIDDEN);
        }
        if (!doc.getMember().getId().equals(user.getMember().getId())) {
            throw new UserExceptionHandler(ErrorCode.DOCUMENT_FORBIDDEN);
        }

        doc.updateIfPresent(req.content(), req.summary(), req.details(), req.bookmark());

        return doc.getId();
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



    // 문서 조회 API
    // 문서 전체 조회 API
}
