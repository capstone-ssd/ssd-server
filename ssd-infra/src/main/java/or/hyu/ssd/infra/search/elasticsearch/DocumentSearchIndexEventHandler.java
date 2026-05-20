package or.hyu.ssd.infra.search.elasticsearch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.hyu.ssd.document.application.event.DocumentSearchIndexEvent;
import or.hyu.ssd.document.repository.DocumentRepository;
import or.hyu.ssd.document.repository.DocumentSearchRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentSearchIndexEventHandler {

    private final DocumentRepository documentRepository;
    private final DocumentSearchRepository documentSearchRepository;

    @EventListener
    public void handle(DocumentSearchIndexEvent event) {
        if (event == null || event.documentId() == null) {
            return;
        }
        if (event.action() == DocumentSearchIndexEvent.Action.DELETE) {
            documentSearchRepository.delete(event.documentId());
            return;
        }
        documentRepository.findById(event.documentId())
                .ifPresentOrElse(
                        documentSearchRepository::index,
                        () -> log.warn("색인 대상 문서를 찾지 못했습니다. documentId={}", event.documentId())
                );
    }
}
