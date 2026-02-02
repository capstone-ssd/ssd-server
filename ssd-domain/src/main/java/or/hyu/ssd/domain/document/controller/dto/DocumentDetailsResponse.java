package or.hyu.ssd.domain.document.controller.dto;

public record DocumentDetailsResponse(
        Long documentId,
        String details
) {
    public static DocumentDetailsResponse of(Long documentId, String details) {
        String body = details == null ? "" : details.trim();
        return new DocumentDetailsResponse(documentId, body);
    }
}
