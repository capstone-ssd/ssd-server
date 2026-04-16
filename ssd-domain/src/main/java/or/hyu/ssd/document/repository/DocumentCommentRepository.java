package or.hyu.ssd.document.repository;

import or.hyu.ssd.document.domain.entity.Document;
import or.hyu.ssd.document.domain.entity.DocumentComment;

import java.util.List;
import java.util.Optional;
import java.util.Collection;

public interface DocumentCommentRepository {
    Optional<DocumentComment> findById(Long id);

    DocumentComment save(DocumentComment comment);

    List<DocumentComment> findAllByDocumentOrderByCreatedAtAsc(Document document);

    void deleteAllByDocument(Document document);

    void deleteAllByDocumentAndBlockIdIn(Document document, Collection<Integer> blockIds);

    void delete(DocumentComment comment);
}
