package or.hyu.ssd.document.application.result;

public record ExternalAiKeywordResult(
        Long documentId,
        String keyword
) {
    public static ExternalAiKeywordResult of(Long documentId, String keyword) {
        return new ExternalAiKeywordResult(documentId, keyword == null ? "" : keyword.trim());
    }
}
