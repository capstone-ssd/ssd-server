package or.hyu.ssd.member.application.result;

import or.hyu.ssd.member.domain.model.Member;

public record GetMyMemberResult(
        Long memberId,
        String name,
        String email,
        String profileImageUrl,
        String role
) {

    public static GetMyMemberResult from(Member member) {
        return new GetMyMemberResult(
                member.getId(),
                member.getName(),
                member.getEmail(),
                member.getProfileImageUrl(),
                member.getRole().name()
        );
    }
}
