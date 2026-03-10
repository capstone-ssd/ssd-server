package or.hyu.ssd.domain.document.controller.dto;

public record CreateDocumentParagraphRequest(
        String content,
        String role,
        Integer blockId
) {}
