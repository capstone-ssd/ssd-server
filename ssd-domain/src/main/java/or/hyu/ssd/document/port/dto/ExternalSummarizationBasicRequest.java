package or.hyu.ssd.document.port.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record ExternalSummarizationBasicRequest(
        @NotBlank
        @JsonProperty("doc_id")
        @JsonAlias("docId")
        String docId,
        @NotBlank
        String doc
) {
}
