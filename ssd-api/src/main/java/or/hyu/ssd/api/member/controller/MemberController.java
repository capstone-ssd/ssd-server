package or.hyu.ssd.api.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import or.hyu.ssd.api.member.response.GetMyMemberResponse;
import or.hyu.ssd.domain.member.service.CustomUserDetails;
import or.hyu.ssd.domain.member.service.MemberService;
import or.hyu.ssd.global.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "회원 API", description = "회원 조회 관련 엔드포인트")
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/v1/members/me")
    @Operation(
            summary = "내 회원 정보 조회",
            description = """
                    ### 개요
                    - 로그인한 회원의 최신 정보를 조회합니다.

                    ### 인증
                    - Authorization: Bearer {accessToken}

                    ### 응답
                    - 200 OK
                    - data.memberId: 회원 ID
                    - data.name: 회원명
                    - data.email: 이메일
                    - data.profileImageUrl: 프로필 이미지 URL
                    - data.role: 회원 권한
                    """
    )
    public ResponseEntity<ApiResponse<GetMyMemberResponse>> getMyInfo(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        GetMyMemberResponse response = GetMyMemberResponse.from(memberService.getMyInfo(user));
        return ResponseEntity.ok(ApiResponse.ok(response, "회원 정보가 조회되었습니다"));
    }
}
