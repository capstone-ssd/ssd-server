package or.hyu.ssd.domain.document.controller.dto;

public record DocumentParagraphDto(
        String content,
        String role,
        int pageNumber,
        int blockId
) {}
