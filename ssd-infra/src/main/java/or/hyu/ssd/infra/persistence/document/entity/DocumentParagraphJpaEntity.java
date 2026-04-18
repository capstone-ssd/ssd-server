package or.hyu.ssd.infra.persistence.document.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import or.hyu.ssd.document.domain.model.DocumentBlockType;
import or.hyu.ssd.infra.persistence.base.BaseJpaEntity;
import org.hibernate.annotations.Comment;

@Entity
@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "document_paragraphs",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_document_paragraph_doc_block", columnNames = {"document_id", "block_id"})
        },
        indexes = {
                @Index(name = "idx_document_paragraph_document_id", columnList = "document_id")
        }
)
@Comment("DocumentJpaEntity의 block을 저장하는 엔티티")
public class DocumentParagraphJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("문단 내용 (입력: paragraphs[].content)")
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Comment("블록 타입 (문단/이미지)")
    @Enumerated(EnumType.STRING)
    @Column(name = "block_type", length = 20)
    private DocumentBlockType type;

    @Comment("문단 역할(입력: paragraphs[].role)")
    @Column(name = "role", length = 16)
    private String role;

    @Comment("문단 페이지 번호 (입력: paragraphs[].pageNumber)")
    @Column(name = "page_number", nullable = false)
    private int pageNumber;

    @Comment("문단 블록 ID (입력: paragraphs[].blockId)")
    @Column(name = "block_id", nullable = false)
    private int blockId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private DocumentJpaEntity document;

    @Version
    private Long version;

    public static DocumentParagraphJpaEntity of(
            DocumentBlockType type,
            String content,
            String role,
            int pageNumber,
            int blockId,
            DocumentJpaEntity document
    ) {
        return DocumentParagraphJpaEntity.builder()
                .content(content)
                .type(type)
                .role(role)
                .pageNumber(pageNumber)
                .blockId(blockId)
                .document(document)
                .build();
    }

    public DocumentBlockType getTypeOrDefault() {
        return type == null ? DocumentBlockType.PARAGRAPH : type;
    }

    public boolean isImageBlock() {
        return getTypeOrDefault().isImage();
    }
}
