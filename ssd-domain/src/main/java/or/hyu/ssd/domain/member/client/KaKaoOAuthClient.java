package or.hyu.ssd.domain.member.client;
import or.hyu.ssd.domain.member.controller.dto.kakao.KaKaoOAuthTokenDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "kakaoOAuthClient",
        url = "https://kauth.kakao.com"
)
public interface KaKaoOAuthClient {

    /**
     * 클라이언트의 액세스 코드를 가지고 액세스 토큰을 받아오는 메서드입니다
     *
     * 이전에 설정해놓은 리다이렉트 주소를 통해 요청이 액세스 코드를 가져오면
     * 이를 가지고 액세스 토큰을 반환받습니다
     *
     * @param grant_type: 해당 파라미터는 authorization_code 로 고정됩니다
     * */
    @PostMapping("/oauth/token")
    KaKaoOAuthTokenDTO getToken(@RequestParam("grant_type") String grant_type,
                                @RequestParam("client_id") String client_id,
                                @RequestParam("redirect_uri") String redirect_uri,
                                @RequestParam("code") String code);

}
