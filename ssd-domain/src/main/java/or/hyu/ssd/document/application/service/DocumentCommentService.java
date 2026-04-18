package or.hyu.ssd.document.application.service;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.document.domain.model.Document;
import or.hyu.ssd.document.domain.model.DocumentComment;
import or.hyu.ssd.document.domain.model.DocumentParagraph;
import or.hyu.ssd.document.repository.DocumentCommentRepository;
import or.hyu.ssd.document.repository.DocumentParagraphRepository;
import or.hyu.ssd.document.repository.DocumentRepository;
import or.hyu.ssd.document.application.command.CreateDocumentCommentCommand;
import or.hyu.ssd.document.application.command.UpdateDocumentCommentCommand;
import or.hyu.ssd.document.application.result.DocumentCommentItemResult;
import or.hyu.ssd.document.application.result.DocumentCommentResult;
import or.hyu.ssd.member.domain.model.Member;
import or.hyu.ssd.member.repository.MemberRepository;
import or.hyu.ssd.common.exception.ErrorCode;
import or.hyu.ssd.common.exception.DocumentException;
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
    private final MemberRepository memberRepository;

    public DocumentCommentResult create(Long documentId, Long memberId, CreateDocumentCommentCommand command) {
        Document doc = getDocument(documentId);
        Member member = getMember(memberId);
        ensureDocumentOwner(doc, memberId);
        int blockId = command.blockId();

        // 문서와 blockId를 통해서 지정된 blockId를 조회합니다
        ensureBlockExists(doc, blockId);

        // 그걸 저장합니다
        DocumentComment saved = documentCommentRepository.save(
                DocumentComment.of(blockId, command.comment(), doc, member)
        );
        return DocumentCommentResult.of(saved.getId());
    }

    public DocumentCommentResult update(Long commentId, Long memberId, UpdateDocumentCommentCommand command) {
        ensureAuthenticated(memberId);
        DocumentComment comment = getComment(commentId);
        ensureCommentOwner(comment, memberId);
        comment.updateComment(command.comment());
        return DocumentCommentResult.of(comment.getId());
    }

    public void delete(Long commentId, Long memberId) {
        ensureAuthenticated(memberId);
        DocumentComment comment = getComment(commentId);
        ensureCommentOwner(comment, memberId);
        documentCommentRepository.delete(comment);
    }

    @Transactional(readOnly = true)
    public List<DocumentCommentItemResult> list(Long documentId, Long memberId) {

        // 문서 ID로 문서를 가져옵니다
        Document doc = getDocument(documentId);
        ensureAuthenticated(memberId);
        ensureDocumentOwner(doc, memberId);

        // 문서에 매핑된 DocumentParagraph를 blockId순으로 정렬하여 가져옵니다 -> 주석의 본문 내용을 가져오기 위함
        Map<Integer, String> blockContentMap = documentParagraphRepository
                .findBlocks(doc).stream()
                .collect(Collectors.toMap(
                        DocumentParagraph::getBlockId,
                        paragraph -> paragraph.isImageBlock() ? "[이미지]" : paragraph.getContent(),
                        (a, b) -> a
                ));

        // 문서가 매핑된 주석을 가져옥 DTO에 매핑합니다
        return documentCommentRepository.findAllByDocumentOrderByCreatedAtAsc(doc).stream()
                .map(comment -> DocumentCommentItemResult.of(
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
                .orElseThrow(() -> new DocumentException(ErrorCode.DOCUMENT_NOT_FOUND));
    }

    private DocumentComment getComment(Long commentId) {
        return documentCommentRepository.findById(commentId)
                .orElseThrow(() -> new DocumentException(ErrorCode.COMMENT_NOT_FOUND));
    }

    private void ensureAuthenticated(Long memberId) {
        if (memberId == null) {
            throw new DocumentException(ErrorCode.MEMBER_NOT_FOUND);
        }
    }

    private void ensureCommentOwner(DocumentComment comment, Long memberId) {
        if (comment.getMember() == null) {
            throw new DocumentException(ErrorCode.COMMENT_FORBIDDEN);
        }
        if (!comment.getMember().getId().equals(memberId)) {
            throw new DocumentException(ErrorCode.COMMENT_FORBIDDEN);
        }
    }

    private void ensureDocumentOwner(Document document, Long memberId) {
        if (document.getMember() == null || memberId == null) {
            throw new DocumentException(ErrorCode.DOCUMENT_FORBIDDEN);
        }
        if (!document.getMember().getId().equals(memberId)) {
            throw new DocumentException(ErrorCode.DOCUMENT_FORBIDDEN);
        }
    }

    private Member getMember(Long memberId) {
        ensureAuthenticated(memberId);
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new DocumentException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private void ensureBlockExists(Document doc, int blockId) {
        if (documentParagraphRepository.findByDocumentAndBlockId(doc, blockId).isEmpty()) {
            throw new DocumentException(ErrorCode.DOCUMENT_PARAGRAPH_NOT_FOUND);
        }
    }
}
