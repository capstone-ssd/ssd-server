package or.hyu.ssd.domain.document.controller.dto;

import or.hyu.ssd.domain.document.entity.DocumentLog;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record DocumentLogItemResponse(
        String savedTime,
        String editorName,
        String editorEmail
) {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public static DocumentLogItemResponse of(DocumentLog log) {
        return new DocumentLogItemResponse(
                formatTime(log.getCreatedAt()),
                log.getEditorName(),
                log.getEditorEmail()
        );
    }

    private static String formatTime(LocalDateTime createdAt) {
        if (createdAt == null) {
            return "";
        }
        return createdAt.format(TIME_FORMATTER);
    }
}
