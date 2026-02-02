package or.hyu.ssd.domain.document.repository;

import or.hyu.ssd.domain.document.entity.Document;
import or.hyu.ssd.domain.document.entity.EvaluatorCheckList;

import java.util.List;

public interface EvaluatorCheckListRepository {
    List<EvaluatorCheckList> findAllByDocumentOrderByIdAsc(Document document);

    List<EvaluatorCheckList> saveAll(Iterable<EvaluatorCheckList> entities);

    void deleteAllByDocument(Document document);
}
