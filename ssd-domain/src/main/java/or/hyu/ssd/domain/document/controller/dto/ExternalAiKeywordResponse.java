package or.hyu.ssd.domain.document.controller.dto;

public record ExternalAiKeywordResponse(
        Long documentId,
        String keyword
) {
    public static ExternalAiKeywordResponse of(Long documentId, String keyword) {
        return new ExternalAiKeywordResponse(documentId, keyword == null ? "" : keyword.trim());
    }
}
