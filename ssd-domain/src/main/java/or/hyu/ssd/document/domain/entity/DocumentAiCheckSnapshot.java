package or.hyu.ssd.document.domain.entity;

import lombok.*;
import or.hyu.ssd.shared.domain.AuditableDomainEntity;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
