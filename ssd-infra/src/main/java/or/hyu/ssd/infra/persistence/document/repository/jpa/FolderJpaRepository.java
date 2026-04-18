package or.hyu.ssd.infra.persistence.document.repository.jpa;

import or.hyu.ssd.infra.persistence.document.entity.FolderJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FolderJpaRepository extends JpaRepository<FolderJpaEntity, Long> {
    Optional<FolderJpaEntity> findByIdAndMember_Id(Long id, Long memberId);

    List<FolderJpaEntity> findAllByMember_IdAndParent_Id(Long memberId, Long parentId);

    List<FolderJpaEntity> findAllByMember_IdAndParentIsNull(Long memberId);

    List<FolderJpaEntity> findAllByMember_Id(Long memberId, Sort sort);

    boolean existsByMember_IdAndParent_Id(Long memberId, Long parentId);

    @Query("""
            select distinct child.parent.id
            from FolderJpaEntity child
            where child.member.id = :memberId
              and child.parent.id in :parentIds
            """)
    List<Long> findDistinctParentIdsByMember_IdAndParent_IdIn(Long memberId, List<Long> parentIds);
}
