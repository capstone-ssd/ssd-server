package or.hyu.ssd.domain.document.service;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.domain.document.controller.dto.DocumentLogDateGroupResponse;
import or.hyu.ssd.domain.document.controller.dto.DocumentLogItemResponse;
import or.hyu.ssd.domain.document.controller.dto.DocumentLogResponse;
import or.hyu.ssd.domain.document.entity.Document;
import or.hyu.ssd.domain.document.entity.DocumentLog;
import or.hyu.ssd.domain.document.repository.DocumentLogRepository;
import or.hyu.ssd.domain.document.repository.DocumentRepository;
import or.hyu.ssd.domain.member.service.CustomUserDetails;
import or.hyu.ssd.global.api.ErrorCode;
import or.hyu.ssd.global.api.handler.DocumentException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DocumentLogService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private final DocumentRepository documentRepository;
    private final DocumentLogRepository documentLogRepository;

    public DocumentLogResponse list(Long documentId, CustomUserDetails user) {
        Document document = getOwnedDocument(documentId, user);
        List<DocumentLog> logs = documentLogRepository.findAllByDocumentOrderByCreatedAtDesc(document);

        List<DocumentLogDateGroupResponse> records = new ArrayList<>();
        LocalDate currentDate = null;
        List<DocumentLogItemResponse> currentLogs = new ArrayList<>();

        for (DocumentLog log : logs) {
            LocalDate logDate = log.getCreatedAt() == null ? null : log.getCreatedAt().toLocalDate();
            if (currentDate == null || !currentDate.equals(logDate)) {
                if (currentDate != null) {
                    records.add(DocumentLogDateGroupResponse.of(currentDate.format(DATE_FORMATTER), List.copyOf(currentLogs)));
                }
                currentDate = logDate;
                currentLogs = new ArrayList<>();
            }
            currentLogs.add(DocumentLogItemResponse.of(log));
        }

        if (currentDate != null) {
            records.add(DocumentLogDateGroupResponse.of(currentDate.format(DATE_FORMATTER), List.copyOf(currentLogs)));
        }

        return DocumentLogResponse.of(document.getId(), records);
    }

    private Document getOwnedDocument(Long documentId, CustomUserDetails user) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentException(ErrorCode.DOCUMENT_NOT_FOUND));
        if (user == null || user.getMember() == null || document.getMember() == null) {
            throw new DocumentException(ErrorCode.DOCUMENT_FORBIDDEN);
        }
        if (!document.getMember().getId().equals(user.getMember().getId())) {
            throw new DocumentException(ErrorCode.DOCUMENT_FORBIDDEN);
        }
        return document;
    }
}
