package or.hyu.ssd.infra.persistence.document.repository.jpa;

import or.hyu.ssd.infra.persistence.document.entity.DocumentJpaEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentJpaRepository extends JpaRepository<DocumentJpaEntity, Long> {
    List<DocumentJpaEntity> findAllByMember_Id(Long memberId, Sort sort);

    @EntityGraph(attributePaths = {"folder", "member"})
    List<DocumentJpaEntity> findAllByMember_IdAndTitleContaining(Long memberId, String keyword, Sort sort);

    @EntityGraph(attributePaths = {"folder", "member"})
    List<DocumentJpaEntity> findAllByMember_IdAndTitleStartingWith(Long memberId, String keyword, Sort sort);

    List<DocumentJpaEntity> findAllByMember_IdAndFolder_Id(Long memberId, Long folderId, Sort sort);

    List<DocumentJpaEntity> findAllByMember_IdAndFolderIsNull(Long memberId, Sort sort);

    List<DocumentJpaEntity> findAllByFolder_Id(Long folderId);
}
