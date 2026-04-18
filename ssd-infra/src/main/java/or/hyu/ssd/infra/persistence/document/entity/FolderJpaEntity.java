package or.hyu.ssd.infra.persistence.document.entity;

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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import or.hyu.ssd.infra.persistence.base.BaseJpaEntity;
import or.hyu.ssd.infra.persistence.member.entity.MemberJpaEntity;
import org.hibernate.annotations.Comment;
import jakarta.persistence.Column;

@Entity
@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "folders",
        indexes = {
                @Index(name = "idx_folder_member_id", columnList = "member_id"),
                @Index(name = "idx_folder_parent_id", columnList = "parent_id")
        }
)
@Comment("문서 폴더 엔티티")
public class FolderJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("폴더명")
    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Comment("폴더 색상 (HEX 등)")
    @Column(name = "color", length = 20)
    private String color;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private FolderJpaEntity parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberJpaEntity member;

    @Version
    private Long version;

    public static FolderJpaEntity of(String name, String color, FolderJpaEntity parent, MemberJpaEntity member) {
        return FolderJpaEntity.builder()
                .name(name)
                .color(color)
                .parent(parent)
                .member(member)
                .build();
    }

    public void updateIfPresent(String name, String color, FolderJpaEntity parent) {
        if (name != null) {
            this.name = name;
        }
        if (color != null) {
            this.color = color;
        }
        if (parent != null) {
            this.parent = parent;
        }
    }

    public void updateParent(FolderJpaEntity parent) {
        this.parent = parent;
    }
}
