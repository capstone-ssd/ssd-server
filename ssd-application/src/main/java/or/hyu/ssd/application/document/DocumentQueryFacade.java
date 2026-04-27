package or.hyu.ssd.application.document;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.document.application.result.DocumentDetailResult;
import or.hyu.ssd.document.application.result.DocumentListItemResult;
import or.hyu.ssd.document.application.service.DocumentQueryService;
import or.hyu.ssd.document.application.support.DocumentSort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentQueryFacade {

    private final DocumentQueryService documentQueryService;

    public DocumentDetailResult getDocument(Long documentId, Long memberId) {
        return documentQueryService.getDocument(documentId, memberId);
    }

    public List<DocumentListItemResult> listDocuments(Long memberId, DocumentSort sortOption, Long folderId) {
        return documentQueryService.listDocuments(memberId, sortOption, folderId);
    }
}
