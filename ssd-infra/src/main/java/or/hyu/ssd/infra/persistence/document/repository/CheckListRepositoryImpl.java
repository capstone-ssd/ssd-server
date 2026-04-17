package or.hyu.ssd.infra.persistence.document.repository;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.document.domain.entity.CheckList;
import or.hyu.ssd.document.domain.entity.Document;
import or.hyu.ssd.document.repository.CheckListRepository;
import or.hyu.ssd.infra.persistence.document.mapper.DocumentPersistenceMapper;
import or.hyu.ssd.infra.persistence.document.repository.jpa.CheckListJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Repository
@RequiredArgsConstructor
public class CheckListRepositoryImpl implements CheckListRepository {
    private final CheckListJpaRepository checkListJpaRepository;

    @Override
    public Optional<CheckList> findById(Long id) {
        return checkListJpaRepository.findById(id).map(DocumentPersistenceMapper::toDomain);
    }

    @Override
    public List<CheckList> findAllByDocument(Document document) {
        return checkListJpaRepository.findAllByDocument(DocumentPersistenceMapper.toDocumentRef(document)).stream()
                .map(DocumentPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<CheckList> saveAll(Iterable<CheckList> entities) {
        return checkListJpaRepository.saveAll(StreamSupport.stream(entities.spliterator(), false)
                        .map(DocumentPersistenceMapper::toJpa)
                        .toList()).stream()
                .map(DocumentPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteAllByDocument(Document document) {
        checkListJpaRepository.deleteAllByDocument(DocumentPersistenceMapper.toDocumentRef(document));
    }

    @Override
    public void delete(CheckList entity) {
        if (entity.getId() != null) {
            checkListJpaRepository.deleteById(entity.getId());
            return;
        }
        checkListJpaRepository.delete(DocumentPersistenceMapper.toJpa(entity));
    }

    @Override
    public void flush() {
        checkListJpaRepository.flush();
    }
}
