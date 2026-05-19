package or.hyu.ssd.document.application.service;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.document.domain.model.Document;
import or.hyu.ssd.document.domain.model.DocumentParagraph;
import or.hyu.ssd.document.domain.model.Folder;
import or.hyu.ssd.document.repository.DocumentParagraphRepository;
import or.hyu.ssd.document.repository.DocumentRepository;
import or.hyu.ssd.document.repository.FolderRepository;
import or.hyu.ssd.document.application.support.DocumentSort;
import or.hyu.ssd.document.application.result.DocumentBlockResult;
import or.hyu.ssd.document.application.result.DocumentDetailResult;
import or.hyu.ssd.document.application.result.DocumentListItemResult;
import or.hyu.ssd.document.application.result.DocumentSearchSuggestionResult;
import or.hyu.ssd.common.exception.ErrorCode;
import or.hyu.ssd.common.exception.DocumentException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentQueryService {

    private static final int DEFAULT_SUGGESTION_LIMIT = 5;
    private static final int MAX_SUGGESTION_LIMIT = 10;
    private static final int MIN_SUGGESTION_KEYWORD_LENGTH = 2;
    private static final double DEFAULT_SUGGESTION_THRESHOLD = 0.2;

    private final DocumentRepository documentRepository;
    private final DocumentParagraphRepository documentParagraphRepository;
    private final FolderRepository folderRepository;

    public DocumentDetailResult getDocument(Long documentId, Long memberId) {
        Document document = loadDocument(documentId);
        assertDocumentOwner(document, memberId);
        return DocumentDetailResult.of(document, fetchBlocks(document));
    }

    public List<DocumentListItemResult> listDocuments(Long memberId, DocumentSort sortOption, Long folderId) {
        assertAuthenticatedMember(memberId);

        Sort sort = toSort(sortOption);

        List<Document> documents;
        if (folderId == null) {
            documents = documentRepository.findAllByMember_Id(memberId, sort);
        } else if (folderId == 0L) {
            documents = documentRepository.findAllByMember_IdAndFolderIsNull(memberId, sort);
        } else {
            Folder folder = resolveFolderOrNull(memberId, folderId);
            documents = documentRepository.findAllByMember_IdAndFolder_Id(memberId, folder.getId(), sort);
        }

        return documents.stream()
                .map(DocumentListItemResult::of)
                .collect(Collectors.toList());
    }

    public List<DocumentListItemResult> searchDocuments(Long memberId, String keyword, DocumentSort sortOption) {
        assertAuthenticatedMember(memberId);
        String normalizedKeyword = normalizeKeyword(keyword);

        return documentRepository.findAllByMember_IdAndTitleContaining(memberId, normalizedKeyword, toSort(sortOption)).stream()
                .map(DocumentListItemResult::of)
                .collect(Collectors.toList());
    }

    public List<DocumentListItemResult> searchDocumentsByTitlePrefix(Long memberId, String keyword, DocumentSort sortOption) {
        assertAuthenticatedMember(memberId);
        String normalizedKeyword = normalizeKeyword(keyword);

        return documentRepository.findAllByMember_IdAndTitleStartingWith(memberId, normalizedKeyword, toSort(sortOption)).stream()
                .map(DocumentListItemResult::of)
                .collect(Collectors.toList());
    }

    public List<DocumentSearchSuggestionResult> suggestSearchKeywords(Long memberId, String keyword, Integer limit) {
        assertAuthenticatedMember(memberId);
        String normalizedKeyword = normalizeKeyword(keyword);
        if (normalizedKeyword.length() < MIN_SUGGESTION_KEYWORD_LENGTH) {
            return List.of();
        }
        return documentRepository.findSearchSuggestions(
                memberId,
                normalizedKeyword,
                normalizeSuggestionLimit(limit),
                DEFAULT_SUGGESTION_THRESHOLD
        );
    }

    private int normalizeSuggestionLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_SUGGESTION_LIMIT;
        }
        return Math.min(Math.max(limit, 1), MAX_SUGGESTION_LIMIT);
    }

    private Sort toSort(DocumentSort sortOption) {
        return switch (sortOption) {
            case LATEST -> Sort.by(Sort.Order.desc("createdAt"));
            case OLDEST -> Sort.by(Sort.Order.asc("createdAt"));
            case NAME -> Sort.by(Sort.Order.asc("title"));
            case MODIFIED -> Sort.by(Sort.Order.desc("updatedAt"));
        };
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new DocumentException(ErrorCode.REQUEST_BODY_INVALID_VALUE, "검색어는 공백일 수 없습니다");
        }
        return keyword.trim();
    }

    private Document loadDocument(Long documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentException(ErrorCode.DOCUMENT_NOT_FOUND));
    }

    private void assertAuthenticatedMember(Long memberId) {
        if (memberId == null) {
            throw new DocumentException(ErrorCode.MEMBER_NOT_FOUND);
        }
    }

    private void assertDocumentOwner(Document document, Long memberId) {
        if (document.getMember() == null || memberId == null) {
            throw new DocumentException(ErrorCode.DOCUMENT_FORBIDDEN);
        }
        if (!document.getMember().getId().equals(memberId)) {
            throw new DocumentException(ErrorCode.DOCUMENT_FORBIDDEN);
        }
    }

    private Folder resolveFolderOrNull(Long memberId, Long folderId) {
        if (folderId == null || folderId == 0L) {
            return null;
        }

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new DocumentException(ErrorCode.FOLDER_NOT_FOUND));
        if (folder.getMember() == null || memberId == null) {
            throw new DocumentException(ErrorCode.FOLDER_FORBIDDEN);
        }
        if (!folder.getMember().getId().equals(memberId)) {
            throw new DocumentException(ErrorCode.FOLDER_FORBIDDEN);
        }
        return folder;
    }

    private List<DocumentBlockResult> fetchBlocks(Document document) {
        return documentParagraphRepository.findBlocks(document).stream()
                .map(paragraph -> paragraph.isImageBlock()
                        ? new DocumentBlockResult(
                                paragraph.getTypeOrDefault(),
                                null,
                                null,
                                paragraph.getPageNumber(),
                                paragraph.getBlockId(),
                                paragraph.getContent()
                        )
                        : new DocumentBlockResult(
                                paragraph.getTypeOrDefault(),
                                paragraph.getContent(),
                                paragraph.getRole(),
                                paragraph.getPageNumber(),
                                paragraph.getBlockId(),
                                null
                        ))
                .toList();
    }
}
