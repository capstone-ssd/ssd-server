package or.hyu.ssd.domain.document.entity;

import jakarta.persistence.*;
import lombok.*;
import or.hyu.ssd.global.entity.BaseEntity;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Version
    private Long version;

    public static DocumentLog of(String editorName, Document document) {
        return DocumentLog.builder()
                .editorName(editorName)
                .document(document)
                .build();
    }
}
