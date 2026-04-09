package or.hyu.ssd.domain.document.controller.dto;

import or.hyu.ssd.domain.document.usecase.result.ExternalAiKeywordResult;

public record ExternalAiKeywordResponse(
        Long documentId,
        String keyword
) {
    public static ExternalAiKeywordResponse from(ExternalAiKeywordResult result) {
        return new ExternalAiKeywordResponse(result.documentId(), result.keyword());
    }
}
