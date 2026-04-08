package or.hyu.ssd.domain.document.usecase.result;

import java.util.List;

public record DocumentLogDateGroupResult(
        String savedDate,
        List<DocumentLogItemResult> logs
) {
    public static DocumentLogDateGroupResult of(String savedDate, List<DocumentLogItemResult> logs) {
        return new DocumentLogDateGroupResult(savedDate, logs == null ? List.of() : logs);
    }
}
