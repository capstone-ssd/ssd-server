package or.hyu.ssd.infra.persistence.document.repository;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.document.domain.model.Folder;
import or.hyu.ssd.document.repository.FolderRepository;
import or.hyu.ssd.infra.persistence.document.mapper.DocumentPersistenceMapper;
import or.hyu.ssd.infra.persistence.document.repository.jpa.FolderJpaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class FolderRepositoryImpl implements FolderRepository {

    private final FolderJpaRepository folderJpaRepository;

    @Override
    public Folder save(Folder folder) {
        return DocumentPersistenceMapper.toDomain(folderJpaRepository.save(DocumentPersistenceMapper.toJpa(folder)));
    }

    @Override
    public Optional<Folder> findById(Long id) {
        return folderJpaRepository.findById(id).map(DocumentPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Folder> findByIdAndMember_Id(Long id, Long memberId) {
        return folderJpaRepository.findByIdAndMember_Id(id, memberId).map(DocumentPersistenceMapper::toDomain);
    }

    @Override
    public List<Folder> findAllByMember_IdAndParent_Id(Long memberId, Long parentId) {
        return folderJpaRepository.findAllByMember_IdAndParent_Id(memberId, parentId).stream()
                .map(DocumentPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Folder> findAllByMember_IdAndParentIsNull(Long memberId) {
        return folderJpaRepository.findAllByMember_IdAndParentIsNull(memberId).stream()
                .map(DocumentPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Folder> findAllByMember_Id(Long memberId, Sort sort) {
        return folderJpaRepository.findAllByMember_Id(memberId, sort).stream()
                .map(DocumentPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByMember_IdAndParent_Id(Long memberId, Long parentId) {
        return folderJpaRepository.existsByMember_IdAndParent_Id(memberId, parentId);
    }

    @Override
    public void delete(Folder folder) {
        if (folder.getId() != null) {
            folderJpaRepository.deleteById(folder.getId());
            return;
        }
        folderJpaRepository.delete(DocumentPersistenceMapper.toJpa(folder));
    }
}
