package or.hyu.ssd.infra.repository;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.domain.document.entity.Document;
import or.hyu.ssd.domain.document.entity.DocumentLog;
import or.hyu.ssd.domain.document.repository.DocumentLogRepository;
import or.hyu.ssd.infra.repository.jpa.DocumentLogJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class DocumentLogRepositoryImpl implements DocumentLogRepository {
    private final DocumentLogJpaRepository documentLogJpaRepository;

    @Override
    public DocumentLog save(DocumentLog log) {
        return documentLogJpaRepository.save(log);
    }

    @Override
    public List<DocumentLog> findAllByDocumentOrderByCreatedAtAsc(Document document) {
        return documentLogJpaRepository.findAllByDocumentOrderByCreatedAtAsc(document);
    }

    @Override
    public void deleteAllByDocument(Document document) {
        documentLogJpaRepository.deleteAllByDocument(document);
    }
}
