package or.hyu.ssd.api.document.response;

import or.hyu.ssd.document.application.result.DocumentSearchSuggestionResult;

public record DocumentSearchSuggestionResponse(
        String keyword,
        double score
) {
    public static DocumentSearchSuggestionResponse from(DocumentSearchSuggestionResult result) {
        return new DocumentSearchSuggestionResponse(result.keyword(), result.score());
    }
}
