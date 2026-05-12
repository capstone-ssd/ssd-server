package or.hyu.ssd.document.application.result;

import or.hyu.ssd.document.domain.model.Document;
import or.hyu.ssd.document.domain.model.DocumentPurpose;

import java.util.List;

public record DocumentDetailResult(
        Long id,
        String title,
        String text,
        List<DocumentBlockResult> blocks,
        String summary,
        String details,
        DocumentPurpose purpose,
        Long folderId,
        boolean bookmark,
        boolean hasExternalAiResult,
        Long authorId,
        String authorName
) {
    public static DocumentDetailResult of(Document document, List<DocumentBlockResult> blocks) {
        Long authorId = document.getMember() != null ? document.getMember().getId() : null;
        String authorName = document.getMember() != null ? document.getMember().getName() : null;
        return new DocumentDetailResult(
                document.getId(),
                document.getTitle(),
                document.getContent(),
                blocks == null ? List.of() : List.copyOf(blocks),
                document.getSummary(),
                document.getDetails(),
                document.getPurposeOrDefault(),
                document.getFolder() != null ? document.getFolder().getId() : null,
                document.isBookmark(),
                document.isExternalAiProcessed(),
                authorId,
                authorName
        );
    }
}
