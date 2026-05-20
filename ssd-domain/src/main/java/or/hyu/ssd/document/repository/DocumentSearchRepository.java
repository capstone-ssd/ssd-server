package or.hyu.ssd.document.repository;

import or.hyu.ssd.document.application.result.DocumentSearchSuggestionResult;
import or.hyu.ssd.document.domain.model.Document;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface DocumentSearchRepository {

    List<Document> searchDocuments(Long memberId, String keyword, Sort sort);

    List<Document> searchDocumentsByTitlePrefix(Long memberId, String keyword, Sort sort);

    List<DocumentSearchSuggestionResult> suggestSearchKeywords(Long memberId, String keyword, int limit, double threshold);

    void index(Document document);

    void delete(Long documentId);
}
