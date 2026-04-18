package or.hyu.ssd.infra.persistence.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import or.hyu.ssd.infra.persistence.base.BaseJpaEntity;
import or.hyu.ssd.member.domain.model.Role;
import org.hibernate.annotations.Comment;
import org.springframework.util.StringUtils;

@Entity
@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "members")
public class MemberJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("회원의 이름")
    @Column(name = "name")
    private String name;

    @Comment("회원의 이메일 주소")
    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Comment("회원의 프로필 이미지 url")
    @Column(name = "profile_image_url", nullable = false)
    private String profileImageUrl;

    @Comment("회원의 프로필 이미지 Key")
    @Column(name = "profile_image_key")
    private String profileImageKey;

    @Comment("회원의 인가 권한")
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    public static MemberJpaEntity join(String name, String email, String profileImageUrl, String profileImageKey, Role role) {
        return MemberJpaEntity.builder()
                .name(StringUtils.hasText(name) ? name : email)
                .email(email)
                .profileImageUrl(StringUtils.hasText(profileImageUrl) ? profileImageUrl : "")
                .profileImageKey(profileImageKey)
                .role(role)
                .build();
    }
}
