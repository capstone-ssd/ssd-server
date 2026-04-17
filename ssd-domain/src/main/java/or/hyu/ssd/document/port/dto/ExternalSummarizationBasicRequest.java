package or.hyu.ssd.document.port.dto;

import jakarta.validation.constraints.NotBlank;

public record ExternalSummarizationBasicRequest(
        @NotBlank
        String docId,
        @NotBlank
        String doc
) {
}
