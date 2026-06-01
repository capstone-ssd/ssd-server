package or.hyu.ssd.api.document.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import or.hyu.ssd.document.application.command.CreateDocumentCommand;
import or.hyu.ssd.document.domain.model.DocumentPurpose;

import java.util.List;

public record CreateDocumentRequest(
        String title,
        @NotBlank(message = "내용은 필수입니다")
        String text,
        List<@NotNull(message = "문단 블록 항목은 null일 수 없습니다") @Valid CreateDocumentBlockRequest> paragraphs,
        @PositiveOrZero(message = "폴더 ID는 0 이상이어야 합니다")
        Long folderId,
        @NotNull(message = "문서 목적은 필수입니다")
        DocumentPurpose purpose
) {
    public CreateDocumentCommand toCommand() {
        return new CreateDocumentCommand(
                RequestStringSanitizer.stripNullChar(title),
                RequestStringSanitizer.stripNullChar(text),
                paragraphs == null ? null : paragraphs.stream().map(CreateDocumentBlockRequest::toCommand).toList(),
                folderId,
                purpose
        );
    }
}
