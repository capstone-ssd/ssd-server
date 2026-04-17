package or.hyu.ssd.infra.persistence.document.repository.jpa;

import or.hyu.ssd.infra.persistence.document.entity.DocumentJpaEntity;
import or.hyu.ssd.infra.persistence.document.entity.DocumentLogJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentLogJpaRepository extends JpaRepository<DocumentLogJpaEntity, Long> {
    List<DocumentLogJpaEntity> findAllByDocumentOrderByCreatedAtDesc(DocumentJpaEntity document);

    void deleteAllByDocument(DocumentJpaEntity document);
}
