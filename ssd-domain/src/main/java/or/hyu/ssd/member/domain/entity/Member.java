package or.hyu.ssd.member.domain.entity;

import lombok.*;
import or.hyu.ssd.shared.domain.AuditableDomainEntity;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Member extends AuditableDomainEntity {

    private Long id;

    private String name;

    private String email;

    private String profileImageUrl;

    private String profileImageKey;

    private Role role;




    public static Member join(String name, String email, String profileImageUrl, String profileImageKey, Role role) {
        return Member.builder()
                .name(hasText(name) ? name : email)
                .email(email)
                .profileImageUrl(hasText(profileImageUrl) ? profileImageUrl : "")
                .profileImageKey(profileImageKey)
                .role(role)
                .build();
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
