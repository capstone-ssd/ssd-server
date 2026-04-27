package or.hyu.ssd.application.document;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.document.application.command.CreateDocumentCommentCommand;
import or.hyu.ssd.document.application.command.UpdateDocumentCommentCommand;
import or.hyu.ssd.document.application.result.DocumentCommentItemResult;
import or.hyu.ssd.document.application.result.DocumentCommentResult;
import or.hyu.ssd.document.application.service.DocumentCommentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentCommentFacade {

    private final DocumentCommentService documentCommentService;

    @Transactional
    public DocumentCommentResult create(Long documentId, Long memberId, CreateDocumentCommentCommand command) {
        return documentCommentService.create(documentId, memberId, command);
    }

    @Transactional
    public DocumentCommentResult update(Long commentId, Long memberId, UpdateDocumentCommentCommand command) {
        return documentCommentService.update(commentId, memberId, command);
    }

    @Transactional
    public void delete(Long commentId, Long memberId) {
        documentCommentService.delete(commentId, memberId);
    }

    @Transactional(readOnly = true)
    public List<DocumentCommentItemResult> list(Long documentId, Long memberId) {
        return documentCommentService.list(documentId, memberId);
    }
}
