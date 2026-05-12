package or.hyu.ssd.api.document.response;

import io.swagger.v3.oas.annotations.media.Schema;
import or.hyu.ssd.document.application.result.DocumentDetailResult;
import or.hyu.ssd.document.domain.model.DocumentPurpose;

import java.util.List;

public record GetDocumentResponse(
        Long id,
        String title,
        String text,
        List<DocumentBlockResponseItem> paragraphs,
        String summary,
        String details,
        DocumentPurpose purpose,
        Long folderId,
        boolean bookmark,
        @Schema(description = "외부 AI 결과가 한 번이라도 정상 반영되었는지 여부")
        boolean hasExternalAiResult,
        Long authorId,
        String authorName
) {
    public static GetDocumentResponse from(DocumentDetailResult result) {
        return new GetDocumentResponse(
                result.id(),
                result.title(),
                result.text(),
                result.blocks().stream().map(DocumentBlockResponseItem::from).toList(),
                result.summary(),
                result.details(),
                result.purpose(),
                result.folderId(),
                result.bookmark(),
                result.hasExternalAiResult(),
                result.authorId(),
                result.authorName()
        );
    }
}
