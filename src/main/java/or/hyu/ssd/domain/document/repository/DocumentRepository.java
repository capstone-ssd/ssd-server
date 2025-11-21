package or.hyu.ssd.domain.document.repository;

import or.hyu.ssd.domain.document.entity.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findAllByMember_Id(Long memberId, Sort sort);
}
