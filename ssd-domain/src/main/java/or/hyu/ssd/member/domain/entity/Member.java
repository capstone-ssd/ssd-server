package or.hyu.ssd.member.domain.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import or.hyu.ssd.shared.domain.AuditableDomainEntity;

@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
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
