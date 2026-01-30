package or.hyu.ssd.domain.document.service;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.domain.document.controller.dto.DocumentCommentItemResponse;
import or.hyu.ssd.domain.document.controller.dto.DocumentCommentRequest;
import or.hyu.ssd.domain.document.controller.dto.DocumentCommentResponse;
import or.hyu.ssd.domain.document.controller.dto.DocumentCommentUpdateRequest;
import or.hyu.ssd.domain.document.entity.Document;
import or.hyu.ssd.domain.document.entity.DocumentComment;
import or.hyu.ssd.domain.document.entity.DocumentParagraph;
import or.hyu.ssd.domain.document.repository.DocumentCommentRepository;
import or.hyu.ssd.domain.document.repository.DocumentParagraphRepository;
import or.hyu.ssd.domain.document.repository.DocumentRepository;
import or.hyu.ssd.domain.member.service.CustomUserDetails;
import or.hyu.ssd.global.api.ErrorCode;
import or.hyu.ssd.global.api.handler.UserExceptionHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class DocumentCommentService {

    private final DocumentRepository documentRepository;
    private final DocumentParagraphRepository documentParagraphRepository;
    private final DocumentCommentRepository documentCommentRepository;

    public DocumentCommentResponse create(Long documentId, CustomUserDetails user, DocumentCommentRequest request) {
        Document doc = getDocument(documentId);
        ensureAuthenticated(user);
        int blockId = request.blockId();

        // 문서와 blockId를 통해서 지정된 blockId를 조회합니다
        ensureBlockExists(doc, blockId);

        // 그걸 저장합니다
        DocumentComment saved = documentCommentRepository.save(
                DocumentComment.of(blockId, request.comment(), doc, user.getMember())
        );
        return DocumentCommentResponse.of(saved.getId());
    }

    public DocumentCommentResponse update(Long commentId, CustomUserDetails user, DocumentCommentUpdateRequest request) {
        ensureAuthenticated(user);
        DocumentComment comment = getComment(commentId);
        ensureCommentOwner(comment, user);
        comment.updateComment(request.comment());
        return DocumentCommentResponse.of(comment.getId());
    }

    @Transactional(readOnly = true)
    public List<DocumentCommentItemResponse> list(Long documentId, CustomUserDetails user) {

        // 문서 ID로 문서를 가져옵니다
        Document doc = getDocument(documentId);
        ensureAuthenticated(user);

        // 문서에 매핑된 DocumentParagraph를 blockId순으로 정렬하여 가져옵니다 -> 주석의 본문 내용을 가져오기 위함
        Map<Integer, String> blockContentMap = documentParagraphRepository
                .findAllByDocumentOrderByPageNumberAscBlockIdAscIdAsc(doc).stream()
                .collect(Collectors.toMap(
                        DocumentParagraph::getBlockId,
                        DocumentParagraph::getContent,
                        (a, b) -> a
                ));

        // 문서가 매핑된 주석을 가져옥 DTO에 매핑합니다
        return documentCommentRepository.findAllByDocumentOrderByCreatedAtAsc(doc).stream()
                .map(comment -> DocumentCommentItemResponse.of(
                        comment.getMember() != null ? comment.getMember().getName() : null,
                        comment.getMember() != null ? comment.getMember().getEmail() : null,
                        comment.getCreatedAt(),
                        blockContentMap.get(comment.getBlockId()),
                        comment.getComment()
                ))
                .collect(Collectors.toList());
    }

    private Document getDocument(Long documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new UserExceptionHandler(ErrorCode.DOCUMENT_NOT_FOUND));
    }

    private DocumentComment getComment(Long commentId) {
        return documentCommentRepository.findById(commentId)
                .orElseThrow(() -> new UserExceptionHandler(ErrorCode.COMMENT_NOT_FOUND));
    }

    private void ensureAuthenticated(CustomUserDetails user) {
        if (user == null || user.getMember() == null) {
            throw new UserExceptionHandler(ErrorCode.MEMBER_NOT_FOUND);
        }
    }

    private void ensureCommentOwner(DocumentComment comment, CustomUserDetails user) {
        if (comment.getMember() == null) {
            throw new UserExceptionHandler(ErrorCode.COMMENT_FORBIDDEN);
        }
        if (!comment.getMember().getId().equals(user.getMember().getId())) {
            throw new UserExceptionHandler(ErrorCode.COMMENT_FORBIDDEN);
        }
    }

    private void ensureBlockExists(Document doc, int blockId) {
        if (documentParagraphRepository.findByDocumentAndBlockId(doc, blockId).isEmpty()) {
            throw new UserExceptionHandler(ErrorCode.DOCUMENT_PARAGRAPH_NOT_FOUND);
        }
    }
}
