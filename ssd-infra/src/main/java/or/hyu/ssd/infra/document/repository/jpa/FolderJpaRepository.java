package or.hyu.ssd.infra.document.repository.jpa;

import or.hyu.ssd.domain.document.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FolderJpaRepository extends JpaRepository<Folder, Long> {
    Optional<Folder> findByIdAndMember_Id(Long id, Long memberId);

    List<Folder> findAllByMember_IdAndParent_Id(Long memberId, Long parentId);

    List<Folder> findAllByMember_IdAndParentIsNull(Long memberId);

    boolean existsByMember_IdAndParent_Id(Long memberId, Long parentId);
}
