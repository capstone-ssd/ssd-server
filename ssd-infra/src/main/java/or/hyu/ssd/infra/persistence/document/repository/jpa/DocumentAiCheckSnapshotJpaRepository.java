package or.hyu.ssd.infra.persistence.document.repository.jpa;

import or.hyu.ssd.infra.persistence.document.entity.DocumentJpaEntity;
import or.hyu.ssd.infra.persistence.document.entity.DocumentAiCheckSnapshotJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentAiCheckSnapshotJpaRepository extends JpaRepository<DocumentAiCheckSnapshotJpaEntity, Long> {
    List<DocumentAiCheckSnapshotJpaEntity> findAllByDocument(DocumentJpaEntity document);

    void deleteAllByDocument(DocumentJpaEntity document);
}
