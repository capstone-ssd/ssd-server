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
@Table(name = "check_lists")
public class CheckList extends BaseEntity {

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
    private Document document;

    @Version
    private Long version;

    public static CheckList of(String content, Document document) {
        return CheckList.builder()
                .content(content)
                .checked(false)
                .document(document)
                .build();
    }

    public void updateChecked(boolean checked) {
        this.checked = checked;
    }
}
