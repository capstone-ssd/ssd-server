package or.hyu.ssd.infra.repository;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.domain.document.entity.Document;
import or.hyu.ssd.domain.document.repository.DocumentRepository;
import or.hyu.ssd.infra.repository.jpa.DocumentJpaRepository;
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
        return documentJpaRepository.save(document);
    }

    @Override
    public Optional<Document> findById(Long id) {
        return documentJpaRepository.findById(id);
    }

    @Override
    public List<Document> findAllByMember_Id(Long memberId, Sort sort) {
        return documentJpaRepository.findAllByMember_Id(memberId, sort);
    }

    @Override
    public void delete(Document document) {
        documentJpaRepository.delete(document);
    }

    @Override
    public void flush() {
        documentJpaRepository.flush();
    }
}
