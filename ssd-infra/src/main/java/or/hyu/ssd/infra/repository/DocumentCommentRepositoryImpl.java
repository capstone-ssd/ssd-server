package or.hyu.ssd.infra.repository;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.domain.document.entity.Document;
import or.hyu.ssd.domain.document.entity.DocumentComment;
import or.hyu.ssd.domain.document.repository.DocumentCommentRepository;
import or.hyu.ssd.infra.repository.jpa.DocumentCommentJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DocumentCommentRepositoryImpl implements DocumentCommentRepository {
    private final DocumentCommentJpaRepository documentCommentJpaRepository;

    @Override
    public Optional<DocumentComment> findById(Long id) {
        return documentCommentJpaRepository.findById(id);
    }

    @Override
    public DocumentComment save(DocumentComment comment) {
        return documentCommentJpaRepository.save(comment);
    }

    @Override
    public List<DocumentComment> findAllByDocumentOrderByCreatedAtAsc(Document document) {
        return documentCommentJpaRepository.findAllByDocumentOrderByCreatedAtAsc(document);
    }

    @Override
    public void deleteAllByDocument(Document document) {
        documentCommentJpaRepository.deleteAllByDocument(document);
    }

    @Override
    public void delete(DocumentComment comment) {
        documentCommentJpaRepository.delete(comment);
    }
}
