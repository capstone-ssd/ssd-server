package or.hyu.ssd.infra.persistence.document.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import or.hyu.ssd.infra.persistence.base.BaseJpaEntity;
import org.hibernate.annotations.Comment;

@Entity
@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "document_logs",
        indexes = {
                @Index(name = "idx_document_log_document_id", columnList = "document_id")
        }
)
@Comment("문서 수정 기록 엔티티")
public class DocumentLogJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("수정한 사용자 이름 (입력: member.name)")
    @Column(name = "editor_name", nullable = false, length = 100)
    private String editorName;

    @Comment("수정한 사용자 이메일 (입력: member.email)")
    @Column(name = "editor_email", length = 150)
    private String editorEmail;

    @Comment("마지막 수정에서 삭제된 블록 개수")
    @Column(name = "deleted_block_count", nullable = false)
    private int deletedBlockCount;

    @Comment("마지막 수정에서 생성된 블록 개수")
    @Column(name = "created_block_count", nullable = false)
    private int createdBlockCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private DocumentJpaEntity document;

    @Version
    private Long version;

    public static DocumentLogJpaEntity of(String editorName, String editorEmail, DocumentJpaEntity document) {
        return of(editorName, editorEmail, 0, 0, document);
    }

    public static DocumentLogJpaEntity of(
            String editorName,
            String editorEmail,
            int deletedBlockCount,
            int createdBlockCount,
            DocumentJpaEntity document
    ) {
        return DocumentLogJpaEntity.builder()
                .editorName(editorName)
                .editorEmail(editorEmail)
                .deletedBlockCount(deletedBlockCount)
                .createdBlockCount(createdBlockCount)
                .document(document)
                .build();
    }
}
