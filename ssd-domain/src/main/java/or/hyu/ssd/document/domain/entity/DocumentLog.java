package or.hyu.ssd.document.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import or.hyu.ssd.shared.persistence.BaseEntity;
import org.hibernate.annotations.Comment;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Builder
@Table(
        name = "document_logs",
        indexes = {
                @Index(name = "idx_document_log_document_id", columnList = "document_id")
        }
)
@Comment("문서 수정 기록 엔티티")
public class DocumentLog extends BaseEntity {

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
    private Document document;

    @Version
    private Long version;

    public static DocumentLog of(String editorName, String editorEmail, Document document) {
        return of(editorName, editorEmail, 0, 0, document);
    }

    public static DocumentLog of(String editorName, String editorEmail, int deletedBlockCount, int createdBlockCount, Document document) {
        return DocumentLog.builder()
                .editorName(editorName)
                .editorEmail(editorEmail)
                .deletedBlockCount(deletedBlockCount)
                .createdBlockCount(createdBlockCount)
                .document(document)
                .build();
    }
}
