package or.hyu.ssd.domain.document.repository;

import or.hyu.ssd.domain.document.entity.Document;
import or.hyu.ssd.domain.document.entity.DocumentLog;

import java.util.List;

public interface DocumentLogRepository {
    DocumentLog save(DocumentLog log);

    List<DocumentLog> findAllByDocumentOrderByCreatedAtAsc(Document document);

    void deleteAllByDocument(Document document);
}
