package or.hyu.ssd.domain.document.repository;

import or.hyu.ssd.domain.document.entity.Document;
import or.hyu.ssd.domain.document.entity.DocumentParagraph;

import java.util.List;
import java.util.Optional;

public interface DocumentParagraphRepository {
    List<DocumentParagraph> saveAll(Iterable<DocumentParagraph> entities);

    List<DocumentParagraph> findAllByDocumentOrderByPageNumberAscBlockIdAscIdAsc(Document document);

    Optional<DocumentParagraph> findByDocumentAndBlockId(Document document, int blockId);

    void deleteAllByDocument(Document document);
}
