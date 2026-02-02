package or.hyu.ssd.infra.document.repository;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.domain.document.entity.Document;
import or.hyu.ssd.domain.document.entity.DocumentParagraph;
import or.hyu.ssd.domain.document.repository.DocumentParagraphRepository;
import or.hyu.ssd.infra.document.repository.jpa.DocumentParagraphJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DocumentParagraphRepositoryImpl implements DocumentParagraphRepository {
    private final DocumentParagraphJpaRepository documentParagraphJpaRepository;

    @Override
    public List<DocumentParagraph> saveAll(Iterable<DocumentParagraph> entities) {
        return documentParagraphJpaRepository.saveAll(entities);
    }

    @Override
    public List<DocumentParagraph> findAllByDocumentOrderByPageNumberAscBlockIdAscIdAsc(Document document) {
        return documentParagraphJpaRepository.findAllByDocumentOrderByPageNumberAscBlockIdAscIdAsc(document);
    }

    @Override
    public Optional<DocumentParagraph> findByDocumentAndBlockId(Document document, int blockId) {
        return documentParagraphJpaRepository.findByDocumentAndBlockId(document, blockId);
    }

    @Override
    public void deleteAllByDocument(Document document) {
        documentParagraphJpaRepository.deleteAllByDocument(document);
    }
}
