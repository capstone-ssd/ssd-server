package or.hyu.ssd.document.repository;

import or.hyu.ssd.document.domain.entity.Document;
import or.hyu.ssd.document.domain.entity.DocumentAiCheckSnapshot;

import java.util.List;

public interface DocumentAiCheckSnapshotRepository {
    List<DocumentAiCheckSnapshot> findAllByDocument(Document document);

    List<DocumentAiCheckSnapshot> saveAll(Iterable<DocumentAiCheckSnapshot> snapshots);

    void deleteAllByDocument(Document document);
}
