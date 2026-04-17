package or.hyu.ssd.document.domain.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import or.hyu.ssd.member.domain.entity.Member;
import or.hyu.ssd.shared.domain.AuditableDomainEntity;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class DocumentComment extends AuditableDomainEntity {

    private Long id;

    private int blockId;

    private String comment;

    private Document document;

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
