package or.hyu.ssd.infra.document.repository;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.domain.document.entity.CheckList;
import or.hyu.ssd.domain.document.entity.Document;
import or.hyu.ssd.domain.document.repository.CheckListRepository;
import or.hyu.ssd.infra.document.repository.jpa.CheckListJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CheckListRepositoryImpl implements CheckListRepository {
    private final CheckListJpaRepository checkListJpaRepository;

    @Override
    public Optional<CheckList> findById(Long id) {
        return checkListJpaRepository.findById(id);
    }

    @Override
    public List<CheckList> findAllByDocument(Document document) {
        return checkListJpaRepository.findAllByDocument(document);
    }

    @Override
    public List<CheckList> saveAll(Iterable<CheckList> entities) {
        return checkListJpaRepository.saveAll(entities);
    }

    @Override
    public void deleteAllByDocument(Document document) {
        checkListJpaRepository.deleteAllByDocument(document);
    }

    @Override
    public void delete(CheckList entity) {
        checkListJpaRepository.delete(entity);
    }

    @Override
    public void flush() {
        checkListJpaRepository.flush();
    }
}
