package or.hyu.ssd.document.repository;

import or.hyu.ssd.document.domain.model.Document;
import or.hyu.ssd.document.domain.model.DocumentAiCheckSnapshot;

import java.util.List;

public interface DocumentAiCheckSnapshotRepository {
    List<DocumentAiCheckSnapshot> findAllByDocument(Document document);

    List<DocumentAiCheckSnapshot> saveAll(Iterable<DocumentAiCheckSnapshot> snapshots);

    void deleteAllByDocument(Document document);
}
