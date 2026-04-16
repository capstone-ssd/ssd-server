package or.hyu.ssd.infra.document.repository.jpa;

import or.hyu.ssd.document.domain.entity.CheckList;
import or.hyu.ssd.document.domain.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CheckListJpaRepository extends JpaRepository<CheckList, Long> {
    void deleteAllByDocument(Document document);

    List<CheckList> findAllByDocument(Document document);
}
