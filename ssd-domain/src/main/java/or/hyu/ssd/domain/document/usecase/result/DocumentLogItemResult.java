package or.hyu.ssd.domain.document.usecase.result;

import or.hyu.ssd.domain.document.entity.DocumentLog;

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
