package or.hyu.ssd.domain.document.repository;

import or.hyu.ssd.domain.document.entity.Document;
import or.hyu.ssd.domain.document.entity.DocumentParagraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentParagraphRepository extends JpaRepository<DocumentParagraph, Long> {
    List<DocumentParagraph> findAllByDocumentOrderByPageNumberAscBlockIdAscIdAsc(Document document);
    void deleteAllByDocument(Document document);
}
