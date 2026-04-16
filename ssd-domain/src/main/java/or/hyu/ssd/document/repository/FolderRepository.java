package or.hyu.ssd.document.repository;

import or.hyu.ssd.document.domain.entity.Folder;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

public interface FolderRepository {

    Folder save(Folder folder);

    Optional<Folder> findById(Long id);

    Optional<Folder> findByIdAndMember_Id(Long id, Long memberId);

    List<Folder> findAllByMember_IdAndParent_Id(Long memberId, Long parentId);

    List<Folder> findAllByMember_IdAndParentIsNull(Long memberId);

    List<Folder> findAllByMember_Id(Long memberId, Sort sort);

    boolean existsByMember_IdAndParent_Id(Long memberId, Long parentId);

    void delete(Folder folder);
}
