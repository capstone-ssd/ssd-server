package or.hyu.ssd.document.repository;

import or.hyu.ssd.document.domain.model.Document;
import or.hyu.ssd.document.domain.model.EvaluatorCheckList;

import java.util.List;

public interface EvaluatorCheckListRepository {
    List<EvaluatorCheckList> findAllByDocumentOrderByIdAsc(Document document);

    List<EvaluatorCheckList> saveAll(Iterable<EvaluatorCheckList> entities);

    void deleteAllByDocument(Document document);
}
