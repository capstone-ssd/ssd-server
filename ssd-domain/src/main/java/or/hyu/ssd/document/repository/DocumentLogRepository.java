package or.hyu.ssd.document.repository;

import or.hyu.ssd.document.domain.entity.Document;
import or.hyu.ssd.document.domain.entity.DocumentLog;

import java.util.List;

public interface DocumentLogRepository {
    DocumentLog save(DocumentLog log);

    List<DocumentLog> findAllByDocumentOrderByCreatedAtDesc(Document document);

    void deleteAllByDocument(Document document);
}
