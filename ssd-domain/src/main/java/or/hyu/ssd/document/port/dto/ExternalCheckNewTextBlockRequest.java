package or.hyu.ssd.document.port.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ExternalCheckNewTextBlockRequest(
        @JsonProperty("block_id")
        String blockId,
        String block
) {
}
