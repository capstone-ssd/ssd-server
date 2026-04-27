package or.hyu.ssd.application.document;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.document.application.command.ExternalDocumentIdCommand;
import or.hyu.ssd.document.application.result.ExternalAiBatchResult;
import or.hyu.ssd.document.application.service.ExternalAiBatchService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExternalAiBatchFacade {

    private final ExternalAiBatchService externalAiBatchService;

    @Transactional
    public ExternalAiBatchResult generateAll(ExternalDocumentIdCommand command, Long memberId) {
        return externalAiBatchService.generateAll(command, memberId);
    }
}
