package or.hyu.ssd.infra.document.repository.jpa;

import or.hyu.ssd.domain.document.entity.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentJpaRepository extends JpaRepository<Document, Long> {
    List<Document> findAllByMember_Id(Long memberId, Sort sort);

    List<Document> findAllByMember_IdAndFolder_Id(Long memberId, Long folderId, Sort sort);

    List<Document> findAllByMember_IdAndFolderIsNull(Long memberId, Sort sort);

    List<Document> findAllByFolder_Id(Long folderId);
}
