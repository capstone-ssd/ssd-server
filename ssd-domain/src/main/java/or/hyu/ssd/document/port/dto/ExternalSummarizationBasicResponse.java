package or.hyu.ssd.document.port.dto;

public record ExternalSummarizationBasicResponse(
        String docId,
        String summary,
        String small
) {
}
