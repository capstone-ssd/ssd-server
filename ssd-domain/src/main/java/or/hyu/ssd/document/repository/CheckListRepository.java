package or.hyu.ssd.document.repository;

import or.hyu.ssd.document.domain.model.CheckList;
import or.hyu.ssd.document.domain.model.Document;

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
