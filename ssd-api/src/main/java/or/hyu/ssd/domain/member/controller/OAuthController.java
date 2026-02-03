package or.hyu.ssd.domain.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.hyu.ssd.domain.member.service.CustomUserDetails;
import or.hyu.ssd.domain.member.service.OAuthService;
import or.hyu.ssd.global.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "소셜로그인 관련 API", description = "카카오 OAuth 시작/콜백 및 JWT 발급")
public class OAuthController {

    private final OAuthService oAuthService;



    @GetMapping("/oauth/kakao")
    @Operation(
            summary = "카카오 로그인 시작(동적 콜백)",
            description = """
                    ### 개요
                    - 클라이언트 Origin 또는 요청 서버 주소를 기반으로 동적 콜백을 계산해 카카오로 리다이렉트합니다.

                    ### 요청
                    - GET /oauth/kakao
                    - Origin이 있으면 화이트리스트 검증 후 사용

                    ### 응답
                    - 302 Redirect: 카카오 인증 서버
                    """
    )
    public void kakaoOAuthCallback(HttpServletRequest request, HttpServletResponse response) throws IOException {

        /**
         * 동적 리다이렉트 방식
         * - Origin(있으면 화이트리스트 검증) 또는 요청의 스킴/호스트/포트를 기반으로
         *   {base}/oauth/kakao/callback 콜백 URL을 계산해 카카오로 302 리다이렉트 합니다.
         */
        String redirectAddress = oAuthService.requestRedirect(request);
        response.sendRedirect(redirectAddress);
    }


    @Operation(
            summary = "카카오 콜백(POST)",
            description = """
                    ### 개요
                    - 프론트 콜백에서 받은 code를 전달하면 서버가 토큰 교환/회원 처리/JWT 발급을 수행합니다.

                    ### 요청
                    - POST /oauth/kakao/callback
                    - Query/Form: code (카카오 인가코드)

                    ### 응답
                    - 200 OK
                    - 헤더: 액세스 토큰
                    - 쿠키: 리프레시 토큰
                    - data: true/false (로그인 성공 여부)
                    """
    )
    @PostMapping("/oauth/kakao/callback")
    public ResponseEntity<ApiResponse<Boolean>> kakaoLoginCallback(
            @RequestParam("code") String accessCode,
            HttpServletRequest request,
            HttpServletResponse response) {

        Boolean result = oAuthService.kakaoLoginNoState(accessCode, request, response);
        return ResponseEntity.ok(ApiResponse.ok(result, "성공적으로 로그인이 완료되었습니다"));
    }


    @Operation(
            summary = "카카오 콜백(GET) 지원",
            description = """
                    ### 개요
                    - 카카오/프론트가 서버로 리다이렉트하는 경우를 위한 GET 콜백입니다. code로 로그인 처리합니다.

                    ### 요청
                    - GET /oauth/kakao/callback?code=...

                    ### 응답
                    - 200 OK
                    - 헤더/쿠키: JWT 발급 (액세스/리프레시)
                    - data: true/false
                    """
    )
    @GetMapping("/oauth/kakao/callback")
    public ResponseEntity<ApiResponse<Boolean>> kakaoLoginCallbackGet(
            @RequestParam("code") String accessCode,
            HttpServletRequest request,
            HttpServletResponse response) {

        Boolean result = oAuthService.kakaoLoginNoState(accessCode, request, response);
        return ResponseEntity.ok(ApiResponse.ok(result, "성공적으로 로그인이 완료되었습니다"));
    }


    @Operation(
            summary = "카카오 로그인 시작(서버 콜백 고정)",
            description = """
                    ### 개요
                    - 서버 설정된 redirect_uri로 카카오에 리다이렉트합니다. 항상 서버 콜백으로 처리합니다.

                    ### 요청
                    - GET /oauth/kakao/server

                    ### 응답
                    - 302 Redirect: 카카오 인증 서버
                    """
    )
    @GetMapping("/oauth/kakao/server")
    public void kakaoOAuthServerStart(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 서버 콜백 전용 시작점
        // - yml에 설정된 서버 콜백 redirect_uri를 사용합니다
        String redirectAddress = oAuthService.requestRedirectServer(request);
        response.sendRedirect(redirectAddress);
    }


    @Operation(
            summary = "카카오 서버 콜백",
            description = """
                    ### 개요
                    - 카카오가 서버로 직접 리다이렉트하는 콜백입니다. state 없이 code만으로 처리합니다.

                    ### 요청
                    - GET /oauth/kakao/server/callback?code=...

                    ### 응답
                    - 200 OK
                    - 헤더/쿠키: JWT 발급 (액세스/리프레시)
                    - data: true/false
                    """
    )
    @GetMapping("/oauth/kakao/server/callback")
    public ResponseEntity<ApiResponse<Boolean>> kakaoOAuthServerCallback(
            @RequestParam("code") String code,
            HttpServletRequest request,
            HttpServletResponse response) {

        Boolean result = oAuthService.kakaoLoginServer(code, request, response);
        return ResponseEntity.ok(ApiResponse.ok(result, "성공적으로 로그인이 완료되었습니다"));
    }
}
