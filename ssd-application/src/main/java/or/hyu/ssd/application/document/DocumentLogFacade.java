package or.hyu.ssd.application.document;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.document.application.result.DocumentLogResult;
import or.hyu.ssd.document.application.service.DocumentLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentLogFacade {

    private final DocumentLogService documentLogService;

    public DocumentLogResult list(Long documentId, Long memberId) {
        return documentLogService.list(documentId, memberId);
    }
}
