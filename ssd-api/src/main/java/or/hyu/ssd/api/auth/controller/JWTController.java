package or.hyu.ssd.api.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.hyu.ssd.auth.principal.CustomUserDetails;
import or.hyu.ssd.common.api.ApiResponse;
import or.hyu.ssd.auth.config.JWTConfig;
import or.hyu.ssd.auth.jwt.support.BearerTokenExtractor;
import or.hyu.ssd.auth.jwt.service.JWTService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "토큰 관련 API", description = "JWT 액세스/리프레시 발급 및 재발급")
public class JWTController {

    private final JWTService jwtService;
    private final JWTConfig jwtConfig;

    @GetMapping("/access")
    @Operation(
            summary = "테스트용 액세스 토큰 발급기",
            description = """
                    ### 개요
                    - 개발/테스트용 액세스 토큰을 헤더에 발급합니다.

                    ### 요청
                    - GET /access

                    ### 응답
                    - 200 OK
                    - 헤더: access 또는 Authorization 헤더에 토큰
                    - data: "테스트용 액세스 토큰이 발급되었습니다. 헤더를 확인해주세요"
                    """
    )
    public ResponseEntity<ApiResponse<String>> issueTestAccessToken(HttpServletResponse response) {
        jwtService.issueTestAccessToken(response);

        return ResponseEntity.ok(ApiResponse.ok("테스트용 액세스 토큰이 발급되었습니다. 헤더를 확인해주세요"));
    }


    @GetMapping("/access-test")
    @Operation(
            summary = "액세스 토큰 사용 방법",
            description = """
                    ### 개요
                    - Access 토큰을 헤더에 담아 호출하면 `@AuthenticationPrincipal` 로 회원 정보를 확인할 수 있습니다.

                    ### 요청
                    - GET /access-test
                    - 헤더: Authorization: Bearer {accessToken}

                    ### 응답
                    - 200 OK
                    - data: "액세스 토큰이 성공적으로 작동합니다"
                    """
    )
    public ResponseEntity<ApiResponse<String>> accessTest(@AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info(userDetails.getEmail());
        return ResponseEntity.ok(ApiResponse.ok("액세스 토큰이 성공적으로 작동합니다"));
    }

    @PostMapping("/reissue")
    @Operation(
            summary = "리프레시,액세스 토큰 재발급 API",
            description = """
                    ### 개요
                    - Refresh Rotate 방식으로 리프레시/액세스 토큰을 함께 재발급합니다.

                    ### 요청
                    - POST /reissue
                    - 헤더: Authorization에 만료된 액세스 토큰을 제외하고 리프레시 토큰을 쿠키로 전송

                    ### 응답
                    - 200 OK
                    - 새 액세스/리프레시 토큰 발급

                    ### 주의
                    - 만료된 액세스 토큰은 헤더에서 제거하고 요청하세요.
                    """
    )
    public ResponseEntity<ApiResponse<String>> reissue(HttpServletRequest request, HttpServletResponse response) {
        jwtService.reissueTokens(request, response);

        return ResponseEntity.ok(ApiResponse.ok("성공적으로 재생성되었습니다"));
    }

    @PostMapping("/logout")
    @Operation(
            summary = "로그아웃 API",
            description = """
                    ### 개요
                    - 현재 로그인한 사용자의 리프레시 토큰을 무효화하고 리프레시 쿠키를 만료시킵니다.

                    ### 요청
                    - POST /logout
                    - 헤더: Authorization: Bearer {accessToken}

                    ### 응답
                    - 200 OK
                    - refresh-token 쿠키 만료

                    ### 주의
                    - 로그아웃 시 access 토큰은 즉시 블랙리스트에 등록되어 무효화됩니다.
                    """
    )
    public ResponseEntity<ApiResponse<String>> logout(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                      HttpServletRequest request,
                                                      HttpServletResponse response) {
        String accessToken = BearerTokenExtractor.extract(request.getHeader(jwtConfig.getHeader()));

        jwtService.logout(userDetails.getMember().getId(), accessToken, response);
        return ResponseEntity.ok(ApiResponse.ok(null, "성공적으로 로그아웃되었습니다"));
    }
}
