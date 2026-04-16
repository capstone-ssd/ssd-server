package or.hyu.ssd.infra.document.repository;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.document.domain.entity.Document;
import or.hyu.ssd.document.domain.entity.EvaluatorCheckList;
import or.hyu.ssd.document.repository.EvaluatorCheckListRepository;
import or.hyu.ssd.infra.document.repository.jpa.EvaluatorCheckListJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class EvaluatorCheckListRepositoryImpl implements EvaluatorCheckListRepository {
    private final EvaluatorCheckListJpaRepository evaluatorCheckListJpaRepository;

    @Override
    public List<EvaluatorCheckList> findAllByDocumentOrderByIdAsc(Document document) {
        return evaluatorCheckListJpaRepository.findAllByDocumentOrderByIdAsc(document);
    }

    @Override
    public List<EvaluatorCheckList> saveAll(Iterable<EvaluatorCheckList> entities) {
        return evaluatorCheckListJpaRepository.saveAll(entities);
    }

    @Override
    public void deleteAllByDocument(Document document) {
        evaluatorCheckListJpaRepository.deleteAllByDocument(document);
    }
}
