package or.hyu.ssd.infra.repository.jpa;

import or.hyu.ssd.domain.document.entity.Document;
import or.hyu.ssd.domain.document.entity.DocumentComment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentCommentJpaRepository extends JpaRepository<DocumentComment, Long> {
    @EntityGraph(attributePaths = "member")
    List<DocumentComment> findAllByDocumentOrderByCreatedAtAsc(Document document);

    void deleteAllByDocument(Document document);
}
