package or.hyu.ssd.domain.document.entity;

import jakarta.persistence.*;
import lombok.*;
import or.hyu.ssd.global.entity.BaseEntity;
import org.hibernate.annotations.Comment;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Builder
@Table(
        name = "document_paragraphs",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_document_paragraph_doc_block", columnNames = {"document_id", "block_id"})
        },
        indexes = {
                @Index(name = "idx_document_paragraph_document_id", columnList = "document_id")
        }
)
@Comment("Document의 block을 저장하는 엔티티")
public class DocumentParagraph extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("문단 내용 (입력: paragraphs[].content)")
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

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
    private Document document;

    @Version
    private Long version;

    public static DocumentParagraph of(String content, String role, int pageNumber, int blockId, Document document) {
        return DocumentParagraph.builder()
                .content(content)
                .role(role)
                .pageNumber(pageNumber)
                .blockId(blockId)
                .document(document)
                .build();
    }
}
