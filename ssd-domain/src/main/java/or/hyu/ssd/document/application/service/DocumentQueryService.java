package or.hyu.ssd.document.application.service;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.document.domain.entity.Document;
import or.hyu.ssd.document.domain.entity.DocumentParagraph;
import or.hyu.ssd.document.domain.entity.Folder;
import or.hyu.ssd.document.repository.DocumentParagraphRepository;
import or.hyu.ssd.document.repository.DocumentRepository;
import or.hyu.ssd.document.repository.FolderRepository;
import or.hyu.ssd.document.application.support.DocumentSort;
import or.hyu.ssd.document.application.result.DocumentBlockResult;
import or.hyu.ssd.document.application.result.DocumentDetailResult;
import or.hyu.ssd.document.application.result.DocumentListItemResult;
import or.hyu.ssd.common.exception.ErrorCode;
import or.hyu.ssd.common.exception.DocumentException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DocumentQueryService {

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

        Sort sort = switch (sortOption) {
            case LATEST -> Sort.by(Sort.Order.desc("createdAt"));
            case OLDEST -> Sort.by(Sort.Order.asc("createdAt"));
            case NAME -> Sort.by(Sort.Order.asc("title"));
            case MODIFIED -> Sort.by(Sort.Order.desc("updatedAt"));
        };

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
