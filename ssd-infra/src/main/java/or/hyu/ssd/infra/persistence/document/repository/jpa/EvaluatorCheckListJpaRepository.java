package or.hyu.ssd.infra.persistence.document.repository.jpa;

import or.hyu.ssd.infra.persistence.document.entity.DocumentJpaEntity;
import or.hyu.ssd.infra.persistence.document.entity.EvaluatorCheckListJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EvaluatorCheckListJpaRepository extends JpaRepository<EvaluatorCheckListJpaEntity, Long> {
    List<EvaluatorCheckListJpaEntity> findAllByDocumentOrderByIdAsc(DocumentJpaEntity document);

    void deleteAllByDocument(DocumentJpaEntity document);
}
