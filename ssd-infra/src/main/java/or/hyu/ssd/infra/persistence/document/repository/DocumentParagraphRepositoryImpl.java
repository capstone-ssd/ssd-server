package or.hyu.ssd.infra.persistence.document.repository;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.document.domain.model.Document;
import or.hyu.ssd.document.domain.model.DocumentBlockType;
import or.hyu.ssd.document.domain.model.DocumentParagraph;
import or.hyu.ssd.document.repository.DocumentParagraphRepository;
import or.hyu.ssd.infra.persistence.document.mapper.DocumentPersistenceMapper;
import or.hyu.ssd.infra.persistence.document.repository.jpa.DocumentParagraphJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Repository
@RequiredArgsConstructor
public class DocumentParagraphRepositoryImpl implements DocumentParagraphRepository {
    private final DocumentParagraphJpaRepository documentParagraphJpaRepository;

    @Override
    public List<DocumentParagraph> saveAll(Iterable<DocumentParagraph> entities) {
        return documentParagraphJpaRepository.saveAll(StreamSupport.stream(entities.spliterator(), false)
                        .map(DocumentPersistenceMapper::toJpa)
                        .toList()).stream()
                .map(DocumentPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public void flush() {
        documentParagraphJpaRepository.flush();
    }

    @Override
    public List<DocumentParagraph> findBlocks(Document document) {
        return documentParagraphJpaRepository.findAllByDocumentOrderByPageNumberAscBlockIdAscIdAsc(
                        DocumentPersistenceMapper.toDocumentRef(document)
                ).stream()
                .map(DocumentPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<DocumentParagraph> findParagraphBlocks(Document document) {
        List<DocumentParagraph> paragraphs = new ArrayList<>();
        paragraphs.addAll(documentParagraphJpaRepository.findAllByDocumentAndTypeOrderByPageNumberAscBlockIdAscIdAsc(
                DocumentPersistenceMapper.toDocumentRef(document),
                DocumentBlockType.PARAGRAPH
        ).stream().map(DocumentPersistenceMapper::toDomain).toList());
        paragraphs.addAll(documentParagraphJpaRepository.findAllByDocumentAndTypeIsNullOrderByPageNumberAscBlockIdAscIdAsc(
                DocumentPersistenceMapper.toDocumentRef(document)
        ).stream().map(DocumentPersistenceMapper::toDomain).toList());
        paragraphs.sort((left, right) -> {
            int pageOrder = Integer.compare(left.getPageNumber(), right.getPageNumber());
            if (pageOrder != 0) {
                return pageOrder;
            }
            int blockOrder = Integer.compare(left.getBlockId(), right.getBlockId());
            if (blockOrder != 0) {
                return blockOrder;
            }
            return Long.compare(left.getId(), right.getId());
        });
        return paragraphs;
    }

    @Override
    public Optional<DocumentParagraph> findByDocumentAndBlockId(Document document, int blockId) {
        return documentParagraphJpaRepository.findByDocumentAndBlockId(DocumentPersistenceMapper.toDocumentRef(document), blockId)
                .map(DocumentPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByDocumentMemberIdAndBlockId(Long memberId, int blockId) {
        return documentParagraphJpaRepository.existsByDocument_Member_IdAndBlockId(memberId, blockId);
    }

    @Override
    public void deleteAllByDocument(Document document) {
        documentParagraphJpaRepository.deleteAllByDocument(DocumentPersistenceMapper.toDocumentRef(document));
    }
}
