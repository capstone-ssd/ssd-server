package or.hyu.ssd.domain.document.service;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.domain.document.controller.dto.DocumentCommentRequest;
import or.hyu.ssd.domain.document.controller.dto.DocumentCommentResponse;
import or.hyu.ssd.domain.document.entity.Document;
import or.hyu.ssd.domain.document.entity.DocumentComment;
import or.hyu.ssd.domain.document.repository.DocumentCommentRepository;
import or.hyu.ssd.domain.document.repository.DocumentParagraphRepository;
import or.hyu.ssd.domain.document.repository.DocumentRepository;
import or.hyu.ssd.domain.member.service.CustomUserDetails;
import or.hyu.ssd.global.api.ErrorCode;
import or.hyu.ssd.global.api.handler.UserExceptionHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class DocumentCommentService {

    private final DocumentRepository documentRepository;
    private final DocumentParagraphRepository documentParagraphRepository;
    private final DocumentCommentRepository documentCommentRepository;

    public DocumentCommentResponse create(Long documentId, CustomUserDetails user, DocumentCommentRequest request) {

        // 식별자를 통해서 문서를 조회합니다
        Document doc = getOwnedDocument(documentId, user);
        int blockId = request.blockId();

        // 문서와 blockId를 통해서 지정된 blockId를 조회합니다
        ensureBlockExists(doc, blockId);

        // 그걸 저장합니다
        DocumentComment saved = documentCommentRepository.save(
                DocumentComment.of(blockId, request.comment(), doc, user.getMember())
        );
        return DocumentCommentResponse.of(saved.getId());
    }

    private Document getOwnedDocument(Long documentId, CustomUserDetails user) {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new UserExceptionHandler(ErrorCode.DOCUMENT_NOT_FOUND));
        if (doc.getMember() == null || user == null || user.getMember() == null) {
            throw new UserExceptionHandler(ErrorCode.DOCUMENT_FORBIDDEN);
        }
        if (!doc.getMember().getId().equals(user.getMember().getId())) {
            throw new UserExceptionHandler(ErrorCode.DOCUMENT_FORBIDDEN);
        }
        return doc;
    }

    private void ensureBlockExists(Document doc, int blockId) {
        if (documentParagraphRepository.findByDocumentAndBlockId(doc, blockId).isEmpty()) {
            throw new UserExceptionHandler(ErrorCode.DOCUMENT_PARAGRAPH_NOT_FOUND);
        }
    }
}
