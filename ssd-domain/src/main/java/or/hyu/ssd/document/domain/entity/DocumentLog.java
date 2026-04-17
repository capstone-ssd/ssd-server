package or.hyu.ssd.document.domain.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import or.hyu.ssd.shared.domain.AuditableDomainEntity;

@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class DocumentLog extends AuditableDomainEntity {

    private Long id;
    private String editorName;
    private String editorEmail;
    private int deletedBlockCount;
    private int createdBlockCount;
    private Document document;
    private Long version;

    public static DocumentLog of(String editorName, String editorEmail, Document document) {
        return of(editorName, editorEmail, 0, 0, document);
    }

    public static DocumentLog of(
            String editorName,
            String editorEmail,
            int deletedBlockCount,
            int createdBlockCount,
            Document document
    ) {
        return DocumentLog.builder()
                .editorName(editorName)
                .editorEmail(editorEmail)
                .deletedBlockCount(deletedBlockCount)
                .createdBlockCount(createdBlockCount)
                .document(document)
                .build();
    }
}
