package or.hyu.ssd.document.port.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ExternalCheckNewTextRequest(
        @JsonProperty("doc_id")
        String docId,
        @JsonProperty("block_id")
        List<ExternalCheckNewTextBlockRequest> blocks
) {
}
