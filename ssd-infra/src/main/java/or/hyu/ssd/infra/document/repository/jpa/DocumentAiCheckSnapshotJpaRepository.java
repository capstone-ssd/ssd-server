package or.hyu.ssd.infra.document.repository.jpa;

import or.hyu.ssd.domain.document.entity.Document;
import or.hyu.ssd.domain.document.entity.DocumentAiCheckSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentAiCheckSnapshotJpaRepository extends JpaRepository<DocumentAiCheckSnapshot, Long> {
    List<DocumentAiCheckSnapshot> findAllByDocument(Document document);

    void deleteAllByDocument(Document document);
}
