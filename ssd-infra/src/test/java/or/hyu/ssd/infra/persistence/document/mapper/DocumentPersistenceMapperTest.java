package or.hyu.ssd.infra.persistence.document.mapper;

import org.junit.jupiter.api.Test;
import or.hyu.ssd.document.domain.model.Document;
import or.hyu.ssd.document.domain.model.DocumentBlockType;
import or.hyu.ssd.document.domain.model.DocumentParagraph;
import or.hyu.ssd.document.domain.model.Folder;
import or.hyu.ssd.infra.persistence.document.entity.DocumentCommentJpaEntity;
import or.hyu.ssd.infra.persistence.document.entity.DocumentJpaEntity;
import or.hyu.ssd.infra.persistence.document.entity.DocumentParagraphJpaEntity;
import or.hyu.ssd.infra.persistence.document.entity.FolderJpaEntity;
import or.hyu.ssd.infra.persistence.member.entity.MemberJpaEntity;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentPersistenceMapperTest {

    @Test
    void toDomain_preservesExternalAiProcessed() {
        // given
        DocumentJpaEntity entity = DocumentJpaEntity.builder()
                .id(1L)
                .title("문서")
                .content("본문")
                .externalAiProcessed(true)
                .build();

        // when
        Document document = DocumentPersistenceMapper.toDomain(entity);

        // then
        assertThat(document.isExternalAiProcessed()).isTrue();
    }

    @Test
    void toJpa_preservesExternalAiProcessed() {
        // given
        Document document = Document.builder()
                .id(1L)
                .title("문서")
                .content("본문")
                .externalAiProcessed(true)
                .build();

        // when
        DocumentJpaEntity entity = DocumentPersistenceMapper.toJpa(document);

        // then
        assertThat(entity.isExternalAiProcessed()).isTrue();
    }

    @Test
    void toJpa_preservesDocumentVersionForParagraphDocumentRef() {
        // given
        Document document = Document.builder()
                .id(1L)
                .version(7L)
                .build();

        DocumentParagraph paragraph = DocumentParagraph.builder()
                .content("문단")
                .type(DocumentBlockType.PARAGRAPH)
                .role("body")
                .pageNumber(1)
                .blockId(10)
                .document(document)
                .build();

        // when
        DocumentParagraphJpaEntity entity = DocumentPersistenceMapper.toJpa(paragraph);

        // then
        assertThat(entity.getDocument()).isNotNull();
        assertThat(entity.getDocument().getId()).isEqualTo(1L);
        assertThat(entity.getDocument().getVersion()).isEqualTo(7L);
    }

    @Test
    void toDomain_preservesDocumentVersionForNestedDocumentRef() {
        // given
        DocumentJpaEntity documentJpaEntity = DocumentJpaEntity.builder()
                .id(1L)
                .version(5L)
                .build();

        DocumentCommentJpaEntity entity = DocumentCommentJpaEntity.builder()
                .id(2L)
                .blockId(3)
                .comment("코멘트")
                .document(documentJpaEntity)
                .member(MemberJpaEntity.builder().id(4L).build())
                .build();

        // when
        var comment = DocumentPersistenceMapper.toDomain(entity);

        // then
        assertThat(comment.getDocument()).isNotNull();
        assertThat(comment.getDocument().getId()).isEqualTo(1L);
        assertThat(comment.getDocument().getVersion()).isEqualTo(5L);
    }

    @Test
    void toJpa_preservesFolderVersionForFolderRef() {
        // given
        Folder parent = Folder.builder()
                .id(11L)
                .version(3L)
                .build();

        Folder folder = Folder.builder()
                .id(12L)
                .name("하위 폴더")
                .color("#ffffff")
                .parent(parent)
                .build();

        // when
        FolderJpaEntity entity = DocumentPersistenceMapper.toJpa(folder);

        // then
        assertThat(entity.getParent()).isNotNull();
        assertThat(entity.getParent().getId()).isEqualTo(11L);
        assertThat(entity.getParent().getVersion()).isEqualTo(3L);
    }
}
