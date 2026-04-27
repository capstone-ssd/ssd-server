package or.hyu.ssd.document.application.service;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.document.domain.model.Document;
import or.hyu.ssd.document.domain.model.DocumentLog;
import or.hyu.ssd.document.repository.DocumentLogRepository;
import or.hyu.ssd.document.repository.DocumentRepository;
import or.hyu.ssd.document.application.result.DocumentLogDateGroupResult;
import or.hyu.ssd.document.application.result.DocumentLogItemResult;
import or.hyu.ssd.document.application.result.DocumentLogResult;
import or.hyu.ssd.common.exception.ErrorCode;
import or.hyu.ssd.common.exception.DocumentException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentLogService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private final DocumentRepository documentRepository;
    private final DocumentLogRepository documentLogRepository;

    public DocumentLogResult list(Long documentId, Long memberId) {
        Document document = getOwnedDocument(documentId, memberId);
        List<DocumentLog> logs = documentLogRepository.findAllByDocumentOrderByCreatedAtDesc(document);

        List<DocumentLogDateGroupResult> records = new ArrayList<>();
        LocalDate currentDate = null;
        List<DocumentLogItemResult> currentLogs = new ArrayList<>();

        for (DocumentLog log : logs) {
            LocalDate logDate = log.getCreatedAt() == null ? null : log.getCreatedAt().toLocalDate();
            if (currentDate == null || !currentDate.equals(logDate)) {
                if (currentDate != null) {
                    records.add(DocumentLogDateGroupResult.of(currentDate.format(DATE_FORMATTER), List.copyOf(currentLogs)));
                }
                currentDate = logDate;
                currentLogs = new ArrayList<>();
            }
            currentLogs.add(DocumentLogItemResult.of(log));
        }

        if (currentDate != null) {
            records.add(DocumentLogDateGroupResult.of(currentDate.format(DATE_FORMATTER), List.copyOf(currentLogs)));
        }

        return DocumentLogResult.of(document.getId(), records);
    }

    private Document getOwnedDocument(Long documentId, Long memberId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentException(ErrorCode.DOCUMENT_NOT_FOUND));
        if (memberId == null || document.getMember() == null) {
            throw new DocumentException(ErrorCode.DOCUMENT_FORBIDDEN);
        }
        if (!document.getMember().getId().equals(memberId)) {
            throw new DocumentException(ErrorCode.DOCUMENT_FORBIDDEN);
        }
        return document;
    }
}
