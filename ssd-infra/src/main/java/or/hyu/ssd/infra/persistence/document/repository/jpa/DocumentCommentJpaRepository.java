package or.hyu.ssd.infra.persistence.document.repository.jpa;

import or.hyu.ssd.document.domain.entity.Document;
import or.hyu.ssd.document.domain.entity.DocumentComment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface DocumentCommentJpaRepository extends JpaRepository<DocumentComment, Long> {
    @EntityGraph(attributePaths = "member")
    List<DocumentComment> findAllByDocumentOrderByCreatedAtAsc(Document document);

    void deleteAllByDocument(Document document);

    void deleteAllByDocumentAndBlockIdIn(Document document, Collection<Integer> blockIds);
}
