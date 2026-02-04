package or.hyu.ssd.infra.document.repository;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.domain.document.entity.Folder;
import or.hyu.ssd.domain.document.repository.FolderRepository;
import or.hyu.ssd.infra.document.repository.jpa.FolderJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class FolderRepositoryImpl implements FolderRepository {

    private final FolderJpaRepository folderJpaRepository;

    @Override
    public Folder save(Folder folder) {
        return folderJpaRepository.save(folder);
    }

    @Override
    public Optional<Folder> findById(Long id) {
        return folderJpaRepository.findById(id);
    }

    @Override
    public Optional<Folder> findByIdAndMember_Id(Long id, Long memberId) {
        return folderJpaRepository.findByIdAndMember_Id(id, memberId);
    }

    @Override
    public List<Folder> findAllByMember_IdAndParent_Id(Long memberId, Long parentId) {
        return folderJpaRepository.findAllByMember_IdAndParent_Id(memberId, parentId);
    }

    @Override
    public List<Folder> findAllByMember_IdAndParentIsNull(Long memberId) {
        return folderJpaRepository.findAllByMember_IdAndParentIsNull(memberId);
    }

    @Override
    public boolean existsByMember_IdAndParent_Id(Long memberId, Long parentId) {
        return folderJpaRepository.existsByMember_IdAndParent_Id(memberId, parentId);
    }

    @Override
    public void delete(Folder folder) {
        folderJpaRepository.delete(folder);
    }
}
