package or.hyu.ssd.document.port.dto;

public record ExternalCheckNewTextBlockRequest(
        String blockId,
        String block
) {
}
