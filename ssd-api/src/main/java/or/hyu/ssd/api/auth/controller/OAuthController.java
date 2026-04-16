package or.hyu.ssd.api.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import or.hyu.ssd.auth.oauth.service.OAuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@Tag(name = "소셜로그인 관련 API", description = "카카오 OAuth 시작/콜백 및 JWT 발급")
public class OAuthController {

    private final OAuthService oAuthService;

    @GetMapping("/oauth/kakao/login")
    @Operation(
            summary = "카카오 로그인 시작",
            description = """
                    ### 개요
                    - 서버가 최종 클라이언트의 `/redirect` 경로를 계산해 state와 함께 저장한 뒤, 카카오 인증 서버로 리다이렉트합니다.
                    - 카카오 `redirect_uri`는 항상 서버 콜백(`/oauth/kakao/callback`)을 사용합니다.

                    ### 요청
                    - GET /oauth/kakao/login

                    ### 응답
                    - 302 Redirect: 카카오 인증 서버
                    """
    )
    public void kakaoOAuthLoginStart(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(oAuthService.requestRedirectToFixedRedirect(request));
    }

    @Operation(
            summary = "카카오 로그인 콜백",
            description = """
                    ### 개요
                    - 카카오 인가코드를 서버가 받아 로그인과 JWT 발급을 완료한 뒤, state에 저장된 redirect 주소로 다시 리다이렉트합니다.

                    ### 요청
                    - GET /oauth/kakao/callback?code=...&state=...

                    ### 응답
                    - 302 Redirect: {redirect}#accessToken=...&isNewUser=...
                    - 쿠키: refresh-token
                    """
    )
    @GetMapping(value = "/oauth/kakao/callback", params = "state")
    public void kakaoLoginCallbackRedirect(
            @RequestParam("code") String accessCode,
            @RequestParam("state") String state,
            HttpServletRequest request,
            HttpServletResponse response) {

        oAuthService.kakaoLoginAndRedirect(accessCode, state, request, response);
    }
}
