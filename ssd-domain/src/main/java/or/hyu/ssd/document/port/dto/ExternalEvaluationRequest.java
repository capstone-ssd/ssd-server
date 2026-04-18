package or.hyu.ssd.document.port.dto;

import jakarta.validation.constraints.NotBlank;

public record ExternalEvaluationRequest(
        @NotBlank
        String docId,
        @NotBlank
        String doc
) {
}
