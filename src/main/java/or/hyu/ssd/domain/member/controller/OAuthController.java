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
@Tag(name = "소셜로그인 관련 API")
public class OAuthController {

    private final OAuthService oAuthService;



    @GetMapping("/oauth/kakao")
    @Operation(summary = "카카오 소셜로그인 API",
            description = "카카오로 로그인 요청을 전송합니다. <br><br>" +
                    "카카오 인증서버로 요청을 보내고 그 후에 **리다이렉트 주소**로 리다이렉트 됩니다. <br><br>" +
                    "리다이렉트 주소에 포함되어 있는 인가코드를 밑의 API의 파라미터에 넣어주세요")
    public void kakaoOAuthCallback(HttpServletRequest request, HttpServletResponse response) throws IOException {

        /**
         * 동적 리다이렉트 방식
         * - 요청의 Origin을 기반으로 redirect_uri를 계산하고
         * - state(난수)를 발급/저장하여 CSRF/재사용을 방지한 뒤
         * - 카카오 인증 서버로 302 리다이렉트 합니다.
         * */
        String redirectAddress = oAuthService.requestRedirect(request);
        response.sendRedirect(redirectAddress);
    }


    @Operation(summary = "카카오 인증서버 토큰 검증 API",
    description = "프론트 콜백에서 받은 code/state를 서버로 전달하면 서버가 토큰 교환/회원처리/JWT발급을 완료합니다.<br>"
            + "액세스 토큰은 헤더에, 리프레시 토큰은 쿠키에 담아 반환합니다.")
    @PostMapping("/oauth/kakao/callback")
    public ResponseEntity<ApiResponse<Boolean>> kakaoLoginCallback(
            @RequestParam("code") String accessCode,
            @RequestParam("state") String state,
            HttpServletRequest request,
            HttpServletResponse response) {

        Boolean result = oAuthService.kakaoLoginWithState(accessCode, state, request, response);
        return ResponseEntity.ok(ApiResponse.ok(result, "성공적으로 로그인이 완료되었습니다"));
    }


    @Operation(summary = "카카오 콜백(GET) 지원",
            description = "카카오가 서버로 직접 리다이렉트하는 경우를 위한 GET 콜백입니다. 내부적으로 state를 검증하고 로그인 처리를 수행합니다.")
    @GetMapping("/oauth/kakao/callback")
    public ResponseEntity<ApiResponse<Boolean>> kakaoLoginCallbackGet(
            @RequestParam("code") String accessCode,
            @RequestParam("state") String state,
            HttpServletRequest request,
            HttpServletResponse response) {

        Boolean result = oAuthService.kakaoLoginWithState(accessCode, state, request, response);
        return ResponseEntity.ok(ApiResponse.ok(result, "성공적으로 로그인이 완료되었습니다"));
    }


    @Operation(summary = "카카오 로그인 시작 - 서버에서 모든 로직을 처리",
            description = "항상 서버 콜백으로 처리하는 시작 엔드포인트입니다. 서버가 자체 콜백 URL을 계산하고 state를 발급하여 카카오로 리다이렉트합니다.")
    @GetMapping("/oauth/kakao/server")
    public void kakaoOAuthServerStart(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 서버 콜백 전용 시작점
        // - 클라이언트 Origin 여부와 상관없이 서버의 스킴/호스트/포트를 기준으로 redirect_uri를 산출합니다
        String redirectAddress = oAuthService.requestRedirectServer(request);
        response.sendRedirect(redirectAddress);
    }


    @Operation(summary = "카카오 서버 콜백", description = "카카오가 서버로 직접 리다이렉트하는 콜백 엔드포인트입니다.")
    @GetMapping("/oauth/kakao/server/callback")
    public ResponseEntity<ApiResponse<Boolean>> kakaoOAuthServerCallback(
            @RequestParam("code") String code,
            @RequestParam("state") String state,
            HttpServletRequest request,
            HttpServletResponse response) {

        Boolean result = oAuthService.kakaoLoginServer(code, state, request, response);
        return ResponseEntity.ok(ApiResponse.ok(result, "성공적으로 로그인이 완료되었습니다"));
    }
}
