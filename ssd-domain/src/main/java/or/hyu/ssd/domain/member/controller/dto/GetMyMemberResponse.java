package or.hyu.ssd.domain.member.controller.dto;

import or.hyu.ssd.domain.member.entity.Member;

public record GetMyMemberResponse(
        Long memberId,
        String name,
        String email,
        String profileImageUrl,
        String role
) {

    public static GetMyMemberResponse from(Member member) {
        return new GetMyMemberResponse(
                member.getId(),
                member.getName(),
                member.getEmail(),
                member.getProfileImageUrl(),
                member.getRole().name()
        );
    }
}
