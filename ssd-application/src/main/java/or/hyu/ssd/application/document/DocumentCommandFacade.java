package or.hyu.ssd.application.document;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.document.application.command.CreateDocumentCommand;
import or.hyu.ssd.document.application.command.UpdateDocumentCommand;
import or.hyu.ssd.document.application.result.CreateDocumentResult;
import or.hyu.ssd.document.application.result.DocumentBookmarkResult;
import or.hyu.ssd.document.application.result.UpdateDocumentResult;
import or.hyu.ssd.document.application.service.DocumentCommandService;
import or.hyu.ssd.document.application.support.DocumentImageUploadPart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DocumentCommandFacade {

    private final DocumentCommandService documentCommandService;

    public CreateDocumentResult createDocument(Long memberId, CreateDocumentCommand command) {
        return documentCommandService.createDocument(memberId, command);
    }

    public CreateDocumentResult createDocument(
            Long memberId,
            CreateDocumentCommand command,
            List<DocumentImageUploadPart> imageUploadParts
    ) {
        return documentCommandService.createDocument(memberId, command, imageUploadParts);
    }

    public UpdateDocumentResult updateDocument(Long documentId, Long memberId, UpdateDocumentCommand command) {
        return documentCommandService.updateDocument(documentId, memberId, command);
    }

    public UpdateDocumentResult updateDocument(
            Long documentId,
            Long memberId,
            UpdateDocumentCommand command,
            List<DocumentImageUploadPart> imageUploadParts
    ) {
        return documentCommandService.updateDocument(documentId, memberId, command, imageUploadParts);
    }

    public void deleteDocument(Long documentId, Long memberId) {
        documentCommandService.deleteDocument(documentId, memberId);
    }

    public DocumentBookmarkResult toggleBookmark(Long documentId, Long memberId) {
        return documentCommandService.toggleBookmark(documentId, memberId);
    }
}
