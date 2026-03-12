package or.hyu.ssd.domain.document.repository;

import or.hyu.ssd.domain.document.entity.Document;
import or.hyu.ssd.domain.document.entity.DocumentAiCheckSnapshot;

import java.util.List;

public interface DocumentAiCheckSnapshotRepository {
    List<DocumentAiCheckSnapshot> findAllByDocument(Document document);

    List<DocumentAiCheckSnapshot> saveAll(Iterable<DocumentAiCheckSnapshot> snapshots);

    void deleteAllByDocument(Document document);
}
