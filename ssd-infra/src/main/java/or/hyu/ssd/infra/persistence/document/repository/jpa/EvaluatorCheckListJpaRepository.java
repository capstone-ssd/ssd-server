package or.hyu.ssd.infra.persistence.document.repository.jpa;

import or.hyu.ssd.document.domain.entity.Document;
import or.hyu.ssd.document.domain.entity.EvaluatorCheckList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EvaluatorCheckListJpaRepository extends JpaRepository<EvaluatorCheckList, Long> {
    List<EvaluatorCheckList> findAllByDocumentOrderByIdAsc(Document document);

    void deleteAllByDocument(Document document);
}
