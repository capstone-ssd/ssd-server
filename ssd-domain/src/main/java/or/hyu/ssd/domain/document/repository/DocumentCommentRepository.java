package or.hyu.ssd.domain.document.repository;

import or.hyu.ssd.domain.document.entity.Document;
import or.hyu.ssd.domain.document.entity.DocumentComment;

import java.util.List;
import java.util.Optional;

public interface DocumentCommentRepository {
    Optional<DocumentComment> findById(Long id);

    DocumentComment save(DocumentComment comment);

    List<DocumentComment> findAllByDocumentOrderByCreatedAtAsc(Document document);

    void deleteAllByDocument(Document document);

    void delete(DocumentComment comment);
}
