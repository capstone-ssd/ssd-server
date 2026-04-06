package or.hyu.ssd.infra.document.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import or.hyu.ssd.domain.document.entity.DocumentBlockType;
import or.hyu.ssd.domain.document.entity.Document;
import or.hyu.ssd.domain.document.entity.DocumentParagraph;
import or.hyu.ssd.domain.document.repository.DocumentParagraphRepository;
import or.hyu.ssd.infra.document.repository.jpa.DocumentParagraphJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static or.hyu.ssd.domain.document.entity.QDocumentParagraph.documentParagraph;

@Repository
@RequiredArgsConstructor
public class DocumentParagraphRepositoryImpl implements DocumentParagraphRepository {
    private final DocumentParagraphJpaRepository documentParagraphJpaRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    public List<DocumentParagraph> saveAll(Iterable<DocumentParagraph> entities) {
        return documentParagraphJpaRepository.saveAll(entities);
    }

    @Override
    public void flush() {
        documentParagraphJpaRepository.flush();
    }

    @Override
    public List<DocumentParagraph> findBlocks(Document document) {
        return queryFactory.selectFrom(documentParagraph)
                .where(documentParagraph.document.eq(document))
                .orderBy(documentParagraph.pageNumber.asc(), documentParagraph.blockId.asc(), documentParagraph.id.asc())
                .fetch();
    }

    @Override
    public List<DocumentParagraph> findParagraphBlocks(Document document) {
        return queryFactory.selectFrom(documentParagraph)
                .where(
                        documentParagraph.document.eq(document),
                        documentParagraph.type.eq(DocumentBlockType.PARAGRAPH)
                                .or(documentParagraph.type.isNull())
                )
                .orderBy(documentParagraph.pageNumber.asc(), documentParagraph.blockId.asc(), documentParagraph.id.asc())
                .fetch();
    }

    @Override
    public Optional<DocumentParagraph> findByDocumentAndBlockId(Document document, int blockId) {
        return documentParagraphJpaRepository.findByDocumentAndBlockId(document, blockId);
    }

    @Override
    public boolean existsByDocumentMemberIdAndBlockId(Long memberId, int blockId) {
        return documentParagraphJpaRepository.existsByDocument_Member_IdAndBlockId(memberId, blockId);
    }

    @Override
    public void deleteAllByDocument(Document document) {
        documentParagraphJpaRepository.deleteAllByDocument(document);
    }
}
