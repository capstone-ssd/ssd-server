package or.hyu.ssd.external.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ExternalSummarizationBasicResponse(
        @JsonProperty("doc_id")
        String docId,
        String summary,
        String small
) {
}
