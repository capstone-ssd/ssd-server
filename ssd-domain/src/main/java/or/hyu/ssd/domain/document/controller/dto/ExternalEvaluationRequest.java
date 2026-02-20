package or.hyu.ssd.domain.document.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record ExternalEvaluationRequest(
        @NotBlank
        @JsonProperty("doc_id")
        String docId,
        @NotBlank
        @JsonProperty("doc")
        String doc
) {
}
