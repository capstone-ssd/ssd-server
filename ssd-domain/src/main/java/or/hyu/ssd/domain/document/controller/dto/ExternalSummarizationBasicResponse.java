package or.hyu.ssd.domain.document.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ExternalSummarizationBasicResponse(
        @JsonProperty("doc_id")
        String docId,
        @JsonProperty("summary")
        String summary,
        @JsonProperty("small")
        String small
) {
}
