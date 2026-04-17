package or.hyu.ssd.infra.persistence.document.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import or.hyu.ssd.infra.persistence.base.BaseJpaEntity;
import or.hyu.ssd.infra.persistence.member.entity.MemberJpaEntity;
import org.hibernate.annotations.Comment;

@Entity
@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "document_comments",
        indexes = {
                @Index(name = "idx_document_comment_document_id", columnList = "document_id"),
                @Index(name = "idx_document_comment_block_id", columnList = "block_id")
        }
)
@Comment("DocumentJpaEntity의 문단에 대한 주석")
public class DocumentCommentJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("주석 대상 블록 ID")
    @Column(name = "block_id", nullable = false)
    private int blockId;

    @Comment("주석 코멘트 본문")
    @Column(name = "comment", nullable = false, columnDefinition = "TEXT")
    private String comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private DocumentJpaEntity document;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberJpaEntity member;

    public static DocumentCommentJpaEntity of(int blockId, String comment, DocumentJpaEntity document, MemberJpaEntity member) {
        return DocumentCommentJpaEntity.builder()
                .blockId(blockId)
                .comment(comment)
                .document(document)
                .member(member)
                .build();
    }

    public void updateComment(String comment) {
        this.comment = comment;
    }
}
