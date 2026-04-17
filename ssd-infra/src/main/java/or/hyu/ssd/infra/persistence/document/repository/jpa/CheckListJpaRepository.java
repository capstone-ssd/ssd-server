package or.hyu.ssd.infra.persistence.document.repository.jpa;

import or.hyu.ssd.infra.persistence.document.entity.CheckListJpaEntity;
import or.hyu.ssd.infra.persistence.document.entity.DocumentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CheckListJpaRepository extends JpaRepository<CheckListJpaEntity, Long> {
    void deleteAllByDocument(DocumentJpaEntity document);

    List<CheckListJpaEntity> findAllByDocument(DocumentJpaEntity document);
}
