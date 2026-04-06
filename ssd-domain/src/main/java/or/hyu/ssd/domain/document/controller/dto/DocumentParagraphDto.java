package or.hyu.ssd.domain.document.controller.dto;

import or.hyu.ssd.domain.document.entity.DocumentBlockType;

public record DocumentParagraphDto(
        DocumentBlockType type,
        String content,
        String role,
        int pageNumber,
        Integer blockId,
        String url
) {
}
