package or.hyu.ssd.infra.repository.jpa;

import or.hyu.ssd.domain.document.entity.Document;
import or.hyu.ssd.domain.document.entity.DocumentLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentLogJpaRepository extends JpaRepository<DocumentLog, Long> {
    List<DocumentLog> findAllByDocumentOrderByCreatedAtAsc(Document document);

    void deleteAllByDocument(Document document);
}
