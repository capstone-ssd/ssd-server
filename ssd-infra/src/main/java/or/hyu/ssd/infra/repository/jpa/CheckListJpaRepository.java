package or.hyu.ssd.infra.repository.jpa;

import or.hyu.ssd.domain.document.entity.CheckList;
import or.hyu.ssd.domain.document.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CheckListJpaRepository extends JpaRepository<CheckList, Long> {
    void deleteAllByDocument(Document document);

    List<CheckList> findAllByDocument(Document document);
}
