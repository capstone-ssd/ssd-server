package or.hyu.ssd.domain.document.controller.dto;

import java.util.List;

public record ThreeLineSummaryResponse(
        Long documentId,
        List<String> lines
) {
    public static ThreeLineSummaryResponse of(Long id, List<String> lines) {
        return new ThreeLineSummaryResponse(id, lines);
    }
}

