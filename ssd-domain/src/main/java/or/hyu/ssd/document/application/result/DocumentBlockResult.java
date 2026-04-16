package or.hyu.ssd.document.application.result;

import or.hyu.ssd.document.domain.entity.DocumentBlockType;

public record DocumentBlockResult(
        DocumentBlockType type,
        String content,
        String role,
        int pageNumber,
        Integer blockId,
        String url
) {
}
