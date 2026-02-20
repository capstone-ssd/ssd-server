package or.hyu.ssd.domain.document.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ExternalSummarizationKeywordResponse(
        @JsonProperty("doc_id")
        String docId,
        @JsonProperty("keyword")
        String keyword
) {
}
