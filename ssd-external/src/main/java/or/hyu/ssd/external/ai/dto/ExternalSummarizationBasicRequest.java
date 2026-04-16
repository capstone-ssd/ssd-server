package or.hyu.ssd.external.ai.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ExternalSummarizationBasicRequest(
        @JsonProperty("doc_id")
        @JsonAlias("docId")
        String docId,
        String doc
) {
}
