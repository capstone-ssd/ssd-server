package or.hyu.ssd.infra.persistence.document.repository;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.document.domain.model.Document;
import or.hyu.ssd.document.domain.model.DocumentAiCheckSnapshot;
import or.hyu.ssd.document.repository.DocumentAiCheckSnapshotRepository;
import or.hyu.ssd.infra.persistence.document.mapper.DocumentPersistenceMapper;
import or.hyu.ssd.infra.persistence.document.repository.jpa.DocumentAiCheckSnapshotJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.StreamSupport;

@Repository
@RequiredArgsConstructor
public class DocumentAiCheckSnapshotRepositoryImpl implements DocumentAiCheckSnapshotRepository {

    private final DocumentAiCheckSnapshotJpaRepository documentAiCheckSnapshotJpaRepository;

    @Override
    public List<DocumentAiCheckSnapshot> findAllByDocument(Document document) {
        return documentAiCheckSnapshotJpaRepository.findAllByDocument(DocumentPersistenceMapper.toDocumentRef(document)).stream()
                .map(DocumentPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<DocumentAiCheckSnapshot> saveAll(Iterable<DocumentAiCheckSnapshot> snapshots) {
        return documentAiCheckSnapshotJpaRepository.saveAll(StreamSupport.stream(snapshots.spliterator(), false)
                        .map(DocumentPersistenceMapper::toJpa)
                        .toList()).stream()
                .map(DocumentPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteAllByDocument(Document document) {
        documentAiCheckSnapshotJpaRepository.deleteAllByDocument(DocumentPersistenceMapper.toDocumentRef(document));
    }
}
