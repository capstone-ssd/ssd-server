package or.hyu.ssd.document.application.command;

import or.hyu.ssd.document.domain.entity.DocumentBlockType;

public record DocumentBlockCommand(
        DocumentBlockType type,
        String content,
        String role,
        Integer blockId,
        String blobKey,
        String url
) {
    public DocumentBlockType resolvedType() {
        return type == null ? DocumentBlockType.PARAGRAPH : type;
    }
}
