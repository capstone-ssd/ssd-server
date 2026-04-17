package or.hyu.ssd.document.port.dto;

import java.util.Map;

public record ExternalCheckNewTextResponse(
        String blockId,
        Map<String, Boolean> checkList
) {
}
