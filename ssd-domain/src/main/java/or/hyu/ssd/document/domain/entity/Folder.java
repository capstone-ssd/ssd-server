package or.hyu.ssd.document.domain.entity;

import lombok.*;
import or.hyu.ssd.member.domain.entity.Member;
import or.hyu.ssd.shared.domain.AuditableDomainEntity;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Folder extends AuditableDomainEntity {

    private Long id;

    private String name;

    private String color;

    private Folder parent;

    private Member member;

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
