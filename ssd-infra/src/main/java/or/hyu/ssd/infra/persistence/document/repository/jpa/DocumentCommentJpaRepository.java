package or.hyu.ssd.infra.persistence.document.repository.jpa;

import or.hyu.ssd.infra.persistence.document.entity.DocumentJpaEntity;
import or.hyu.ssd.infra.persistence.document.entity.DocumentCommentJpaEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface DocumentCommentJpaRepository extends JpaRepository<DocumentCommentJpaEntity, Long> {
    @EntityGraph(attributePaths = "member")
    List<DocumentCommentJpaEntity> findAllByDocumentOrderByCreatedAtAsc(DocumentJpaEntity document);

    void deleteAllByDocument(DocumentJpaEntity document);

    void deleteAllByDocumentAndBlockIdIn(DocumentJpaEntity document, Collection<Integer> blockIds);
}
