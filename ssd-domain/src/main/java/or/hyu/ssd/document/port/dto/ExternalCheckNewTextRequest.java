package or.hyu.ssd.document.port.dto;

import java.util.List;

public record ExternalCheckNewTextRequest(
        String docId,
        List<ExternalCheckNewTextBlockRequest> blocks
) {
}
