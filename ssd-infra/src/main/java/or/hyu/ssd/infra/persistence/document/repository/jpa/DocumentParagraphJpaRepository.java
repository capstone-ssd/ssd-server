package or.hyu.ssd.infra.persistence.document.repository.jpa;

import or.hyu.ssd.document.domain.entity.Document;
import or.hyu.ssd.document.domain.entity.DocumentParagraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DocumentParagraphJpaRepository extends JpaRepository<DocumentParagraph, Long> {
    Optional<DocumentParagraph> findByDocumentAndBlockId(Document document, int blockId);

    boolean existsByDocument_Member_IdAndBlockId(Long memberId, int blockId);

    void deleteAllByDocument(Document document);
}
