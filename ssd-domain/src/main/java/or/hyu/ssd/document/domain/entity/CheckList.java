package or.hyu.ssd.document.domain.entity;

import lombok.*;
import or.hyu.ssd.shared.domain.AuditableDomainEntity;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class CheckList extends AuditableDomainEntity {

    private Long id;

    private String content;

    private boolean checked;

    private Document document;

    private Long version;

    public static CheckList of(String content, Document document) {
        return CheckList.builder()
                .content(content)
                .checked(false)
                .document(document)
                .build();
    }

    public void updateChecked(boolean checked) {
        this.checked = checked;
    }
}
