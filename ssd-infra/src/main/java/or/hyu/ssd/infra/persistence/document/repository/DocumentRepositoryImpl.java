package or.hyu.ssd.infra.persistence.document.repository;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.document.domain.model.Document;
import or.hyu.ssd.document.repository.DocumentRepository;
import or.hyu.ssd.infra.persistence.document.mapper.DocumentPersistenceMapper;
import or.hyu.ssd.infra.persistence.document.repository.jpa.DocumentJpaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DocumentRepositoryImpl implements DocumentRepository {
    private final DocumentJpaRepository documentJpaRepository;

    @Override
    public Document save(Document document) {
        return DocumentPersistenceMapper.toDomain(documentJpaRepository.save(DocumentPersistenceMapper.toJpa(document)));
    }

    @Override
    public Optional<Document> findById(Long id) {
        return documentJpaRepository.findById(id).map(DocumentPersistenceMapper::toDomain);
    }

    @Override
    public List<Document> findAllByMember_Id(Long memberId, Sort sort) {
        return documentJpaRepository.findAllByMember_Id(memberId, sort).stream()
                .map(DocumentPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Document> findAllByMember_IdAndTitleContaining(Long memberId, String keyword, Sort sort) {
        return documentJpaRepository.findAllByMember_IdAndTitleContaining(memberId, keyword, sort).stream()
                .map(DocumentPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Document> findAllByMember_IdAndTitleStartingWith(Long memberId, String keyword, Sort sort) {
        return documentJpaRepository.findAllByMember_IdAndTitleStartingWith(memberId, keyword, sort).stream()
                .map(DocumentPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Document> findAllByMember_IdAndFolder_Id(Long memberId, Long folderId, Sort sort) {
        return documentJpaRepository.findAllByMember_IdAndFolder_Id(memberId, folderId, sort).stream()
                .map(DocumentPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Document> findAllByMember_IdAndFolderIsNull(Long memberId, Sort sort) {
        return documentJpaRepository.findAllByMember_IdAndFolderIsNull(memberId, sort).stream()
                .map(DocumentPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Document> findAllByFolder_Id(Long folderId) {
        return documentJpaRepository.findAllByFolder_Id(folderId).stream()
                .map(DocumentPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public void delete(Document document) {
        if (document.getId() != null) {
            documentJpaRepository.deleteById(document.getId());
            return;
        }
        documentJpaRepository.delete(DocumentPersistenceMapper.toJpa(document));
    }

    @Override
    public void flush() {
        documentJpaRepository.flush();
    }
}
