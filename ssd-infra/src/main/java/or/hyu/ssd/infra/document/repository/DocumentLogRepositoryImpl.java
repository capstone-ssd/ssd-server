package or.hyu.ssd.infra.document.repository;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.document.domain.entity.Document;
import or.hyu.ssd.document.domain.entity.DocumentLog;
import or.hyu.ssd.document.repository.DocumentLogRepository;
import or.hyu.ssd.infra.document.repository.jpa.DocumentLogJpaRepository;
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
    public List<DocumentLog> findAllByDocumentOrderByCreatedAtDesc(Document document) {
        return documentLogJpaRepository.findAllByDocumentOrderByCreatedAtDesc(document);
    }

    @Override
    public void deleteAllByDocument(Document document) {
        documentLogJpaRepository.deleteAllByDocument(document);
    }
}
