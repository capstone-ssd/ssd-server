package or.hyu.ssd.domain.document.repository;

import or.hyu.ssd.domain.document.entity.Folder;

import java.util.List;
import java.util.Optional;

public interface FolderRepository {

    Folder save(Folder folder);

    Optional<Folder> findById(Long id);

    Optional<Folder> findByIdAndMember_Id(Long id, Long memberId);

    List<Folder> findAllByMember_IdAndParent_Id(Long memberId, Long parentId);

    List<Folder> findAllByMember_IdAndParentIsNull(Long memberId);

    boolean existsByMember_IdAndParent_Id(Long memberId, Long parentId);

    void delete(Folder folder);
}
