package or.hyu.ssd.domain.document.controller.dto;

public record UpdateDocumentRequest(
        String title,
        String content,
        String summary,
        String details,
        String path,
        Boolean bookmark
) {}
