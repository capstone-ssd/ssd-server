package or.hyu.ssd.domain.document.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ExternalCheckNewTextBlockRequest(
        @JsonProperty("block_id")
        String blockId,
        String block
) {
}
