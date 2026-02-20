package or.hyu.ssd.domain.document.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record ExternalCheckNewTextRequest(
        @NotBlank
        @JsonProperty("block_id")
        String blockId,
        @NotBlank
        @JsonProperty("block")
        String block
) {
}
