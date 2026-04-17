package or.hyu.ssd.infra.persistence.document.repository;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.document.domain.entity.Document;
import or.hyu.ssd.document.domain.entity.EvaluatorCheckList;
import or.hyu.ssd.document.repository.EvaluatorCheckListRepository;
import or.hyu.ssd.infra.persistence.document.mapper.DocumentPersistenceMapper;
import or.hyu.ssd.infra.persistence.document.repository.jpa.EvaluatorCheckListJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.StreamSupport;

@Repository
@RequiredArgsConstructor
public class EvaluatorCheckListRepositoryImpl implements EvaluatorCheckListRepository {
    private final EvaluatorCheckListJpaRepository evaluatorCheckListJpaRepository;

    @Override
    public List<EvaluatorCheckList> findAllByDocumentOrderByIdAsc(Document document) {
        return evaluatorCheckListJpaRepository.findAllByDocumentOrderByIdAsc(DocumentPersistenceMapper.toDocumentRef(document)).stream()
                .map(DocumentPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<EvaluatorCheckList> saveAll(Iterable<EvaluatorCheckList> entities) {
        return evaluatorCheckListJpaRepository.saveAll(StreamSupport.stream(entities.spliterator(), false)
                        .map(DocumentPersistenceMapper::toJpa)
                        .toList()).stream()
                .map(DocumentPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteAllByDocument(Document document) {
        evaluatorCheckListJpaRepository.deleteAllByDocument(DocumentPersistenceMapper.toDocumentRef(document));
    }
}
