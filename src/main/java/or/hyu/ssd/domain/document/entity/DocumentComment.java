package or.hyu.ssd.domain.document.entity;

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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import or.hyu.ssd.domain.member.entity.Member;
import or.hyu.ssd.global.entity.BaseEntity;
import org.hibernate.annotations.Comment;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Builder
@Table(
        name = "document_comments",
        indexes = {
                @Index(name = "idx_document_comment_document_id", columnList = "document_id"),
                @Index(name = "idx_document_comment_block_id", columnList = "block_id")
        }
)
@Comment("Document의 문단에 대한 주석")
public class DocumentComment extends BaseEntity {

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
    private Document document;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    public static DocumentComment of(int blockId, String comment, Document document, Member member) {
        return DocumentComment.builder()
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
