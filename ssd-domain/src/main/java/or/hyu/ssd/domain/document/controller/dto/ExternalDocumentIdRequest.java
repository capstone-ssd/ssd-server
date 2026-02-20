package or.hyu.ssd.domain.document.controller.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;

public record ExternalDocumentIdRequest(
        @NotBlank
        @JsonAlias("doc_id")
        String docId
) {
}
