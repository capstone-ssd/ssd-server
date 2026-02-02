package or.hyu.ssd.infra.repository.jpa;

import or.hyu.ssd.domain.document.entity.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentJpaRepository extends JpaRepository<Document, Long> {
    List<Document> findAllByMember_Id(Long memberId, Sort sort);
}
