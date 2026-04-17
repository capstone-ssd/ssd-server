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
public class DocumentParagraph extends AuditableDomainEntity {

    private Long id;
    private String content;
    private DocumentBlockType type;
    private String role;
    private int pageNumber;
    private int blockId;
    private Document document;
    private Long version;

    public static DocumentParagraph of(
            DocumentBlockType type,
            String content,
            String role,
            int pageNumber,
            int blockId,
            Document document
    ) {
        return DocumentParagraph.builder()
                .content(content)
                .type(type)
                .role(role)
                .pageNumber(pageNumber)
                .blockId(blockId)
                .document(document)
                .build();
    }

    public DocumentBlockType getTypeOrDefault() {
        return type == null ? DocumentBlockType.PARAGRAPH : type;
    }

    public boolean isImageBlock() {
        return getTypeOrDefault().isImage();
    }
}
