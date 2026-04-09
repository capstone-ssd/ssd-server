package or.hyu.ssd.domain.document.usecase.command;

import or.hyu.ssd.domain.document.entity.DocumentBlockType;

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
