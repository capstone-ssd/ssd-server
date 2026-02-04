package or.hyu.ssd.domain.document.entity;

import jakarta.persistence.*;
import lombok.*;
import or.hyu.ssd.domain.member.entity.Member;
import or.hyu.ssd.global.entity.BaseEntity;
import org.hibernate.annotations.Comment;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Builder
@Table(
        name = "folders",
        indexes = {
                @Index(name = "idx_folder_member_id", columnList = "member_id"),
                @Index(name = "idx_folder_parent_id", columnList = "parent_id")
        }
)
@Comment("문서 폴더 엔티티")
public class Folder extends BaseEntity {

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
    private Folder parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Version
    private Long version;

    public static Folder of(String name, String color, Folder parent, Member member) {
        return Folder.builder()
                .name(name)
                .color(color)
                .parent(parent)
                .member(member)
                .build();
    }

    public void updateIfPresent(String name, String color, Folder parent) {
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

    public void updateParent(Folder parent) {
        this.parent = parent;
    }
}
