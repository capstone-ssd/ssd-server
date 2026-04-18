package or.hyu.ssd.document.application.result;

import or.hyu.ssd.document.domain.model.DocumentLog;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record DocumentLogItemResult(
        String savedTime,
        String editorName,
        String editorEmail,
        int deletedBlockCount,
        int createdBlockCount
) {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public static DocumentLogItemResult of(DocumentLog log) {
        return new DocumentLogItemResult(
                formatTime(log.getCreatedAt()),
                log.getEditorName(),
                log.getEditorEmail(),
                log.getDeletedBlockCount(),
                log.getCreatedBlockCount()
        );
    }

    private static String formatTime(LocalDateTime createdAt) {
        if (createdAt == null) {
            return "";
        }
        return createdAt.format(TIME_FORMATTER);
    }
}
