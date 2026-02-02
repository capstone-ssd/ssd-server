package or.hyu.ssd.domain.document.controller.dto;

import or.hyu.ssd.domain.document.entity.DocumentLog;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record DocumentLogItemResponse(
        String editorName,
        String time
) {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MM월 dd일 HH시 mm분");

    public static DocumentLogItemResponse of(DocumentLog log) {
        return new DocumentLogItemResponse(
                log.getEditorName(),
                formatTime(log.getCreatedAt())
        );
    }

    private static String formatTime(LocalDateTime time) {
        if (time == null) {
            return "";
        }
        return time.format(FORMATTER);
    }
}
