package or.hyu.ssd.document.repository;

import or.hyu.ssd.document.domain.model.Document;
import or.hyu.ssd.document.domain.model.DocumentParagraph;

import java.util.List;
import java.util.Optional;

public interface DocumentParagraphRepository {
    List<DocumentParagraph> saveAll(Iterable<DocumentParagraph> entities);

    void flush();

    List<DocumentParagraph> findBlocks(Document document);

    List<DocumentParagraph> findParagraphBlocks(Document document);

    Optional<DocumentParagraph> findByDocumentAndBlockId(Document document, int blockId);

    boolean existsByDocumentMemberIdAndBlockId(Long memberId, int blockId);

    void deleteAllByDocument(Document document);
}
