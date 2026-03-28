package or.hyu.ssd.domain.member.controller.dto.kakao;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KaKaoUserInfoResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("카카오 user/me 응답의 snake_case 필드를 내부 DTO까지 매핑한다")
    void shouldMapSnakeCaseFieldsForNestedKakaoAccount() throws Exception {
        String json = """
                {
                  "id": 4818858663,
                  "connected_at": "2026-03-28T18:17:37Z",
                  "properties": {
                    "nickname": "tester",
                    "profile_image": "https://example.com/profile.png"
                  },
                  "kakao_account": {
                    "email": "tester@example.com",
                    "has_email": true,
                    "email_needs_agreement": false,
                    "is_email_valid": true,
                    "is_email_verified": true,
                    "profile": {
                      "nickname": "tester",
                      "is_default_nickname": false
                    }
                  }
                }
                """;

        KaKaoUserInfoResponse response = objectMapper.readValue(json, KaKaoUserInfoResponse.class);

        assertThat(response.getId()).isEqualTo(4818858663L);
        assertThat(response.getConnectedAt()).isEqualTo("2026-03-28T18:17:37Z");
        assertThat(response.getProperties().getNickname()).isEqualTo("tester");
        assertThat(response.getProperties().getProfileImage()).isEqualTo("https://example.com/profile.png");
        assertThat(response.getKakaoAccount().getEmail()).isEqualTo("tester@example.com");
        assertThat(response.getKakaoAccount().getHasEmail()).isTrue();
        assertThat(response.getKakaoAccount().getEmailNeedsAgreement()).isFalse();
        assertThat(response.getKakaoAccount().getIsEmailValid()).isTrue();
        assertThat(response.getKakaoAccount().getIsEmailVerified()).isTrue();
        assertThat(response.getKakaoAccount().getProfile().getNickname()).isEqualTo("tester");
        assertThat(response.getKakaoAccount().getProfile().getIsDefaultNickname()).isFalse();
    }
}
