package or.hyu.ssd.infra.persistence.document.repository.jpa;

import or.hyu.ssd.document.domain.entity.Document;
import or.hyu.ssd.document.domain.entity.DocumentLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentLogJpaRepository extends JpaRepository<DocumentLog, Long> {
    List<DocumentLog> findAllByDocumentOrderByCreatedAtDesc(Document document);

    void deleteAllByDocument(Document document);
}
