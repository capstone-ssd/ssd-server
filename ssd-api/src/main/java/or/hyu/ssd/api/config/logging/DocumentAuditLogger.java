package or.hyu.ssd.api.config.logging;

import lombok.extern.slf4j.Slf4j;
import or.hyu.ssd.common.logging.LoggingMdcKey;
import org.springframework.stereotype.Component;

import or.hyu.ssd.common.logging.MdcScope;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
@Slf4j
public class DocumentAuditLogger {

    public void logDocument(String action, Long memberId, Long documentId, Long folderId) {
        log(action, memberId, documentId, folderId, "성공");
    }

    public void logDocument(String action, Long memberId, Long documentId) {
        log(action, memberId, documentId, null, "성공");
    }

    public void logFolder(String action, Long memberId, Long folderId) {
        log(action, memberId, null, folderId, "성공");
    }

    private void log(String action, Long memberId, Long documentId, Long folderId, String result) {
        Map<String, String> values = new LinkedHashMap<>();
        values.put(LoggingMdcKey.LOG_TYPE, LoggingMdcKey.LOG_TYPE_DOCUMENT_CRUD);
        values.put(LoggingMdcKey.ACTION, action);
        values.put(LoggingMdcKey.RESULT, result);
        values.put(LoggingMdcKey.MEMBER_ID, memberId == null ? null : String.valueOf(memberId));
        values.put(LoggingMdcKey.DOCUMENT_ID, documentId == null ? null : String.valueOf(documentId));
        values.put(LoggingMdcKey.FOLDER_ID, folderId == null ? null : String.valueOf(folderId));

        try (MdcScope ignored = MdcScope.with(values)) {
            log.info("문서/폴더 작업이 완료되었습니다. action={}", action);
        }
    }
}
