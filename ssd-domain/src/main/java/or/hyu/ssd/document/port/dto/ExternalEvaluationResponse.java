package or.hyu.ssd.document.port.dto;

import java.util.Map;

public record ExternalEvaluationResponse(
        String docId,
        ExternalEvaluationReportResponse evaluationReport,
        Map<String, Boolean> checkList
) {
}
