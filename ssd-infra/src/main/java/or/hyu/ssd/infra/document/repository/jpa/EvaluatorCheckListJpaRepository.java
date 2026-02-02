package or.hyu.ssd.infra.document.repository.jpa;

import or.hyu.ssd.domain.document.entity.Document;
import or.hyu.ssd.domain.document.entity.EvaluatorCheckList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EvaluatorCheckListJpaRepository extends JpaRepository<EvaluatorCheckList, Long> {
    List<EvaluatorCheckList> findAllByDocumentOrderByIdAsc(Document document);

    void deleteAllByDocument(Document document);
}
