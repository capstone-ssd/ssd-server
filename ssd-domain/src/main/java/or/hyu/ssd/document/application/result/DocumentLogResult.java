package or.hyu.ssd.document.application.result;

import java.util.List;

public record DocumentLogResult(
        Long documentId,
        List<DocumentLogDateGroupResult> records
) {
    public static DocumentLogResult of(Long documentId, List<DocumentLogDateGroupResult> records) {
        return new DocumentLogResult(documentId, records == null ? List.of() : records);
    }
}
