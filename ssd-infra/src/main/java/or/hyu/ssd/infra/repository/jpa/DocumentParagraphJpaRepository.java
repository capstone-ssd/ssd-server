package or.hyu.ssd.infra.repository.jpa;

import or.hyu.ssd.domain.document.entity.Document;
import or.hyu.ssd.domain.document.entity.DocumentParagraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentParagraphJpaRepository extends JpaRepository<DocumentParagraph, Long> {
    List<DocumentParagraph> findAllByDocumentOrderByPageNumberAscBlockIdAscIdAsc(Document document);

    Optional<DocumentParagraph> findByDocumentAndBlockId(Document document, int blockId);

    void deleteAllByDocument(Document document);
}
