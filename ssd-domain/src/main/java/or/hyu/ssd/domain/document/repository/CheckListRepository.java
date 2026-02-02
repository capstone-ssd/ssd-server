package or.hyu.ssd.domain.document.repository;

import or.hyu.ssd.domain.document.entity.CheckList;
import or.hyu.ssd.domain.document.entity.Document;

import java.util.List;
import java.util.Optional;

public interface CheckListRepository {
    Optional<CheckList> findById(Long id);

    List<CheckList> findAllByDocument(Document document);

    List<CheckList> saveAll(Iterable<CheckList> entities);

    void deleteAllByDocument(Document document);

    void delete(CheckList entity);

    void flush();
}
