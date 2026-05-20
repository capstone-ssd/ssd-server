package or.hyu.ssd.application.document;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.document.application.event.DocumentSearchIndexEvent;
import or.hyu.ssd.document.application.command.CreateDocumentCommand;
import or.hyu.ssd.document.application.command.MoveDocumentFolderCommand;
import or.hyu.ssd.document.application.command.UpdateDocumentCommand;
import or.hyu.ssd.document.application.result.CreateDocumentResult;
import or.hyu.ssd.document.application.result.DocumentBookmarkResult;
import or.hyu.ssd.document.application.result.MoveDocumentFolderResult;
import or.hyu.ssd.document.application.result.UpdateDocumentResult;
import or.hyu.ssd.document.application.service.DocumentCommandService;
import or.hyu.ssd.document.application.support.DocumentImageUploadPart;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DocumentCommandFacade {

    private final DocumentCommandService documentCommandService;
    private final ApplicationEventPublisher eventPublisher;

    public CreateDocumentResult createDocument(Long memberId, CreateDocumentCommand command) {
        CreateDocumentResult result = documentCommandService.createDocument(memberId, command);
        publishAfterCommit(DocumentSearchIndexEvent.index(result.id()));
        return result;
    }

    public CreateDocumentResult createDocument(
            Long memberId,
            CreateDocumentCommand command,
            List<DocumentImageUploadPart> imageUploadParts
    ) {
        CreateDocumentResult result = documentCommandService.createDocument(memberId, command, imageUploadParts);
        publishAfterCommit(DocumentSearchIndexEvent.index(result.id()));
        return result;
    }

    public UpdateDocumentResult updateDocument(Long documentId, Long memberId, UpdateDocumentCommand command) {
        UpdateDocumentResult result = documentCommandService.updateDocument(documentId, memberId, command);
        publishAfterCommit(DocumentSearchIndexEvent.index(result.id()));
        return result;
    }

    public UpdateDocumentResult updateDocument(
            Long documentId,
            Long memberId,
            UpdateDocumentCommand command,
            List<DocumentImageUploadPart> imageUploadParts
    ) {
        UpdateDocumentResult result = documentCommandService.updateDocument(documentId, memberId, command, imageUploadParts);
        publishAfterCommit(DocumentSearchIndexEvent.index(result.id()));
        return result;
    }

    public MoveDocumentFolderResult moveDocumentFolder(Long documentId, Long memberId, MoveDocumentFolderCommand command) {
        MoveDocumentFolderResult result = documentCommandService.moveDocumentFolder(documentId, memberId, command);
        publishAfterCommit(DocumentSearchIndexEvent.index(result.documentId()));
        return result;
    }

    public void deleteDocument(Long documentId, Long memberId) {
        documentCommandService.deleteDocument(documentId, memberId);
        publishAfterCommit(DocumentSearchIndexEvent.delete(documentId));
    }

    public DocumentBookmarkResult toggleBookmark(Long documentId, Long memberId) {
        DocumentBookmarkResult result = documentCommandService.toggleBookmark(documentId, memberId);
        publishAfterCommit(DocumentSearchIndexEvent.index(result.id()));
        return result;
    }

    private void publishAfterCommit(DocumentSearchIndexEvent event) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            eventPublisher.publishEvent(event);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                eventPublisher.publishEvent(event);
            }
        });
    }
}
