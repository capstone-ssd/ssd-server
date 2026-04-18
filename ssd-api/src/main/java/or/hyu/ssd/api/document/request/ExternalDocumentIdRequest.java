package or.hyu.ssd.api.document.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import or.hyu.ssd.document.application.command.ExternalDocumentIdCommand;

public record ExternalDocumentIdRequest(
        @NotBlank(message = "docId는 필수입니다")
        @Pattern(regexp = "^[1-9]\\d*$", message = "docId는 1 이상의 숫자여야 합니다")
        @JsonAlias("doc_id")
        String docId
) {
    public ExternalDocumentIdCommand toCommand() {
        return new ExternalDocumentIdCommand(docId);
    }
}
