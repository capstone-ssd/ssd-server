package or.hyu.ssd.domain.member.entity;

import jakarta.persistence.*;
import lombok.*;
import or.hyu.ssd.global.entity.BaseEntity;
import org.hibernate.annotations.Comment;
import org.springframework.util.StringUtils;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Builder
@Table(name = "members")
public class Member extends BaseEntity {

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
    @Column(name = "profile_image_key", nullable = true)
    private String profileImageKey;

    @Comment("회원의 인가 권한")
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;




    public static Member join(String name, String email, String profileImageUrl, String profileImageKey, Role role) {
        return Member.builder()
                .name(StringUtils.hasText(name) ? name : email)
                .email(email)
                .profileImageUrl(StringUtils.hasText(profileImageUrl) ? profileImageUrl : "")
                .profileImageKey(profileImageKey)
                .role(role)
                .build();
    }
}
