package or.hyu.ssd.domain.document.repository;

import or.hyu.ssd.domain.document.entity.CheckList;
import or.hyu.ssd.domain.document.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckListRepository extends JpaRepository<CheckList, Long> {
    void deleteAllByDocument(Document document);
    java.util.List<CheckList> findAllByDocument(Document document);
}
