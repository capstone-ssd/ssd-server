package or.hyu.ssd.infra.persistence.document.repository.jpa;

import or.hyu.ssd.infra.persistence.document.entity.DocumentParagraphJpaEntity;
import or.hyu.ssd.document.domain.entity.DocumentBlockType;
import or.hyu.ssd.infra.persistence.document.entity.DocumentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentParagraphJpaRepository extends JpaRepository<DocumentParagraphJpaEntity, Long> {
    Optional<DocumentParagraphJpaEntity> findByDocumentAndBlockId(DocumentJpaEntity document, int blockId);

    List<DocumentParagraphJpaEntity> findAllByDocumentOrderByPageNumberAscBlockIdAscIdAsc(DocumentJpaEntity document);

    List<DocumentParagraphJpaEntity> findAllByDocumentAndTypeOrderByPageNumberAscBlockIdAscIdAsc(DocumentJpaEntity document, DocumentBlockType type);

    List<DocumentParagraphJpaEntity> findAllByDocumentAndTypeIsNullOrderByPageNumberAscBlockIdAscIdAsc(DocumentJpaEntity document);

    boolean existsByDocument_Member_IdAndBlockId(Long memberId, int blockId);

    void deleteAllByDocument(DocumentJpaEntity document);
}
