package or.hyu.ssd.domain.document.usecase.result;

import or.hyu.ssd.domain.document.entity.DocumentBlockType;

public record DocumentBlockResult(
        DocumentBlockType type,
        String content,
        String role,
        int pageNumber,
        Integer blockId,
        String url
) {
}
