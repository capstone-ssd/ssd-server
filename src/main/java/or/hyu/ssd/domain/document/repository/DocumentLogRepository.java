package or.hyu.ssd.domain.document.repository;

import or.hyu.ssd.domain.document.entity.Document;
import or.hyu.ssd.domain.document.entity.DocumentLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentLogRepository extends JpaRepository<DocumentLog, Long> {
    List<DocumentLog> findAllByDocumentOrderByCreatedAtAsc(Document document);
    void deleteAllByDocument(Document document);
}
