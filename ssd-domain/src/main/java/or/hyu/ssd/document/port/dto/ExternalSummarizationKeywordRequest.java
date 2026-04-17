package or.hyu.ssd.document.port.dto;

import jakarta.validation.constraints.NotBlank;

public record ExternalSummarizationKeywordRequest(
        @NotBlank
        String docId,
        @NotBlank
        String doc
) {
}
