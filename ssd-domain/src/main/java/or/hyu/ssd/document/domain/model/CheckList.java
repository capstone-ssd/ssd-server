package or.hyu.ssd.document.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import or.hyu.ssd.shared.domain.AuditableDomainEntity;

@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
