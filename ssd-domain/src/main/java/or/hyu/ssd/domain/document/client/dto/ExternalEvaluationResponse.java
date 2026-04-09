package or.hyu.ssd.domain.document.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public record ExternalEvaluationResponse(
        @JsonProperty("doc_id")
        String docId,
        @JsonProperty("evaluation_report")
        ExternalEvaluationReportResponse evaluationReport,
        @JsonProperty("check_list")
        Map<String, Boolean> checkList
) {
}
