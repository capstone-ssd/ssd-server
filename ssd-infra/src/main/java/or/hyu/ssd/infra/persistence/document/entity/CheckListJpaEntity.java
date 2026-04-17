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
import jakarta.persistence.UniqueConstraint;
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
        name = "check_lists",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_checklist_doc_content", columnNames = {"document_id", "content"})
        },
        indexes = {
                @Index(name = "idx_checklist_document_id", columnList = "document_id")
        }
)
@Comment("작성자 체크리스트")
public class CheckListJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("체크리스트 본문")
    @Column(name = "content", nullable = false)
    private String content;

    @Comment("체크리스트 체크 여부")
    @Column(name = "checked", nullable = false)
    private boolean checked;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    private DocumentJpaEntity document;

    @Version
    private Long version;

    public static CheckListJpaEntity of(String content, DocumentJpaEntity document) {
        return CheckListJpaEntity.builder()
                .content(content)
                .checked(false)
                .document(document)
                .build();
    }

    public void updateChecked(boolean checked) {
        this.checked = checked;
    }
}
