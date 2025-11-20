package or.hyu.ssd.domain.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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
    public void kakaoOAuthCallback(HttpServletResponse response) throws IOException {

        String redirectAddress = oAuthService.requestRedirect();
        response.sendRedirect(redirectAddress);
    }


    @Operation(summary = "카카오 인증서버 토큰 검증 API",
    description = "리다이렉트에서 AccessCode를 가지고 서버로 돌아오기 위한 엔드포인트입니다 <br><br><br>" +
            "해당 코드를 이용해서 사용자 정보를 파싱하고 **액세스 토큰는 헤더에, 리프레시 토큰은 쿠키에 담아** 반환합니다")
    @GetMapping("/oauth/kakao/callback")
    public ResponseEntity<ApiResponse<Boolean>> kakaoLogin(@RequestParam("code") String accessCode, HttpServletResponse response) {

        Boolean result = oAuthService.kakaoLogin(accessCode, response);

        return ResponseEntity.ok(ApiResponse.ok(result, "성공적으로 로그인이 완료되었습니다"));
    }
}
