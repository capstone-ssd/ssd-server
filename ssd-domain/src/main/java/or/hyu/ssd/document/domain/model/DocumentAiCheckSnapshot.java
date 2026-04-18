package or.hyu.ssd.document.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import or.hyu.ssd.shared.domain.AuditableDomainEntity;

@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DocumentAiCheckSnapshot extends AuditableDomainEntity {

    private Long id;
    private Document document;
    private int blockId;
    private String content;

    public static DocumentAiCheckSnapshot of(Document document, int blockId, String content) {
        return DocumentAiCheckSnapshot.builder()
                .document(document)
                .blockId(blockId)
                .content(content)
                .build();
    }
}
