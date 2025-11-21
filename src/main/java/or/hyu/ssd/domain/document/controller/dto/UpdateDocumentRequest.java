package or.hyu.ssd.domain.document.controller.dto;

public record UpdateDocumentRequest(
        String content,
        String summary,
        String details,
        Boolean bookmark
) {}

