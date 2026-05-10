package or.hyu.ssd.external.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ExternalCheckNewTextRequest(
        @JsonProperty("doc_id")
        String docId,
        @JsonProperty("blocks")
        List<ExternalCheckNewTextBlockRequest> blocks
) {
}
