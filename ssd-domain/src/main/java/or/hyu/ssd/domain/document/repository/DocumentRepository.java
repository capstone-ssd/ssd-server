package or.hyu.ssd.domain.document.repository;

import or.hyu.ssd.domain.document.entity.Document;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository {
    Document save(Document document);

    Optional<Document> findById(Long id);

    List<Document> findAllByMember_Id(Long memberId, Sort sort);

    void delete(Document document);

    void flush();
}
