package or.hyu.ssd.api.document.response;

import or.hyu.ssd.domain.document.entity.DocumentBlockType;
import or.hyu.ssd.domain.document.usecase.result.DocumentBlockResult;

public record DocumentBlockResponseItem(
        DocumentBlockType type,
        String content,
        String role,
        int pageNumber,
        Integer blockId,
        String url
) {
    public static DocumentBlockResponseItem from(DocumentBlockResult result) {
        return new DocumentBlockResponseItem(
                result.type(),
                result.content(),
                result.role(),
                result.pageNumber(),
                result.blockId(),
                result.url()
        );
    }
}
