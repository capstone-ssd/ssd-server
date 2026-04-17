package or.hyu.ssd.infra.persistence.document.repository;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.document.domain.entity.Document;
import or.hyu.ssd.document.domain.entity.DocumentLog;
import or.hyu.ssd.document.repository.DocumentLogRepository;
import or.hyu.ssd.infra.persistence.document.mapper.DocumentPersistenceMapper;
import or.hyu.ssd.infra.persistence.document.repository.jpa.DocumentLogJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class DocumentLogRepositoryImpl implements DocumentLogRepository {
    private final DocumentLogJpaRepository documentLogJpaRepository;

    @Override
    public DocumentLog save(DocumentLog log) {
        return DocumentPersistenceMapper.toDomain(documentLogJpaRepository.save(DocumentPersistenceMapper.toJpa(log)));
    }

    @Override
    public List<DocumentLog> findAllByDocumentOrderByCreatedAtDesc(Document document) {
        return documentLogJpaRepository.findAllByDocumentOrderByCreatedAtDesc(DocumentPersistenceMapper.toDocumentRef(document)).stream()
                .map(DocumentPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteAllByDocument(Document document) {
        documentLogJpaRepository.deleteAllByDocument(DocumentPersistenceMapper.toDocumentRef(document));
    }
}
