package or.hyu.ssd.document.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import or.hyu.ssd.member.domain.model.Member;
import or.hyu.ssd.shared.domain.AuditableDomainEntity;

@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
