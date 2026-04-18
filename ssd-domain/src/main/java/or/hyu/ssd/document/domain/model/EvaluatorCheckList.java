package or.hyu.ssd.document.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import or.hyu.ssd.shared.domain.AuditableDomainEntity;

@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EvaluatorCheckList extends AuditableDomainEntity {

    private Long id;
    private String content;
    private boolean checked;
    private Document document;
    private Long version;

    public static EvaluatorCheckList of(String content, boolean checked, Document document) {
        return EvaluatorCheckList.builder()
                .content(content)
                .checked(checked)
                .document(document)
                .build();
    }

    public void updateChecked(boolean checked) {
        this.checked = checked;
    }
}
