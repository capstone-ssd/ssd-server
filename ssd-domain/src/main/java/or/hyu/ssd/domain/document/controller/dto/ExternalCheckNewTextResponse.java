package or.hyu.ssd.domain.document.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public record ExternalCheckNewTextResponse(
        @JsonProperty("block_id")
        String blockId,
        @JsonProperty("check_list")
        Map<String, Boolean> checkList
) {
}
