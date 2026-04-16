package or.hyu.ssd.infra.persistence.document.repository;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.document.domain.entity.Document;
import or.hyu.ssd.document.domain.entity.DocumentAiCheckSnapshot;
import or.hyu.ssd.document.repository.DocumentAiCheckSnapshotRepository;
import or.hyu.ssd.infra.persistence.document.repository.jpa.DocumentAiCheckSnapshotJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class DocumentAiCheckSnapshotRepositoryImpl implements DocumentAiCheckSnapshotRepository {

    private final DocumentAiCheckSnapshotJpaRepository documentAiCheckSnapshotJpaRepository;

    @Override
    public List<DocumentAiCheckSnapshot> findAllByDocument(Document document) {
        return documentAiCheckSnapshotJpaRepository.findAllByDocument(document);
    }

    @Override
    public List<DocumentAiCheckSnapshot> saveAll(Iterable<DocumentAiCheckSnapshot> snapshots) {
        return documentAiCheckSnapshotJpaRepository.saveAll(snapshots);
    }

    @Override
    public void deleteAllByDocument(Document document) {
        documentAiCheckSnapshotJpaRepository.deleteAllByDocument(document);
    }
}
