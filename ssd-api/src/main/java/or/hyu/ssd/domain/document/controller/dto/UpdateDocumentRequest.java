package or.hyu.ssd.domain.document.controller.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import or.hyu.ssd.domain.document.usecase.command.UpdateDocumentCommand;

import java.util.List;

public record UpdateDocumentRequest(
        String title,
        @NotBlank(message = "내용은 필수입니다")
        String text,
        List<@Valid CreateDocumentBlockRequest> paragraphs
) {
    public UpdateDocumentCommand toCommand() {
        return new UpdateDocumentCommand(
                title,
                text,
                paragraphs == null ? null : paragraphs.stream().map(CreateDocumentBlockRequest::toCommand).toList()
        );
    }
}
