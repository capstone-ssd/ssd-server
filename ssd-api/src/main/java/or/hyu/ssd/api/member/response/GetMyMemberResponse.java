package or.hyu.ssd.api.member.response;

public record GetMyMemberResponse(
        Long memberId,
        String name,
        String email,
        String profileImageUrl,
        String role
) {

    public static GetMyMemberResponse from(or.hyu.ssd.member.application.result.GetMyMemberResult response) {
        return new GetMyMemberResponse(
                response.memberId(),
                response.name(),
                response.email(),
                response.profileImageUrl(),
                response.role()
        );
    }
}
