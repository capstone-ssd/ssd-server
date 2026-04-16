package or.hyu.ssd.document.port.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ExternalSummarizationKeywordResponse(
        @JsonProperty("doc_id")
        String docId,
        String keyword
) {
}
