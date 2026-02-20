package or.hyu.ssd.domain.document.controller.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record ExternalSummarizationKeywordRequest(
        @NotBlank
        @JsonProperty("doc_id")
        @JsonAlias("docId")
        String docId,
        @NotBlank
        String doc
) {
}
