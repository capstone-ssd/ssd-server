package or.hyu.ssd.domain.document.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ExternalSummarizationKeywordResponse(
        @JsonProperty("doc_id")
        String docId,
        String keyword
) {
}
