package or.hyu.ssd.domain.document.usecase.result;

import or.hyu.ssd.domain.document.entity.Document;

import java.util.List;

public record DocumentDetailResult(
        Long id,
        String title,
        String text,
        List<DocumentBlockResult> blocks,
        String summary,
        String details,
        Long folderId,
        boolean bookmark,
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
                blocks,
                document.getSummary(),
                document.getDetails(),
                document.getFolder() != null ? document.getFolder().getId() : null,
                document.isBookmark(),
                authorId,
                authorName
        );
    }
}
