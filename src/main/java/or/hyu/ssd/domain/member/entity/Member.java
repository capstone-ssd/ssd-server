package or.hyu.ssd.domain.member.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Builder
@Table(name = "members")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("회원의 이름")
    @Column(name = "name")
    private String name;

    @Comment("회원의 이메일 주소")
    @Column(name = "email", unique = true)
    private String email;

    @Comment("회원의 프로필 이미지 url")
    @Column(name = "profile_image_url", nullable = false)
    private String profileImageUrl;

    @Comment("회원의 프로필 이미지 Key")
    @Column(name = "profile_image_key", nullable = false)
    private String profileImageKey;

    @Comment("회원의 인가 권한")
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;
}
