package or.hyu.ssd.domain.member.service.support;

import or.hyu.ssd.domain.member.controller.dto.kakao.KaKaoUserInfoResponse;
import org.springframework.util.StringUtils;

/**
 * Kakao 사용자 정보 응답을 안전하게 파싱해 정규화된 프로필로 반환합니다.
 * - 동의/스코프에 따라 일부 필드가 누락될 수 있으므로, NPE 방지를 위해 단계별 체크를 수행합니다.
 * - 닉네임은 kakaoAccount.profile.nickname -> properties.nickname 순으로 폴백합니다.
 */
public final class KakaoProfileExtractor {

    private KakaoProfileExtractor() {}

    public static NormalizedKakaoProfile extract(KaKaoUserInfoResponse info) {
        if (info == null) {
            return new NormalizedKakaoProfile(null, null, null, null);
        }

        Long kakaoId = info.getId();

        String email = null;
        if (info.getKakaoAccount() != null) {
            email = info.getKakaoAccount().getEmail();
        }

        String nickname = null;
        if (info.getKakaoAccount() != null && info.getKakaoAccount().getProfile() != null) {
            nickname = info.getKakaoAccount().getProfile().getNickname();
        }
        if (!StringUtils.hasText(nickname) && info.getProperties() != null) {
            nickname = info.getProperties().getNickname();
        }

        String profileImageUrl = null;
        if (info.getProperties() != null) {
            profileImageUrl = info.getProperties().getProfileImage();
        }

        return new NormalizedKakaoProfile(kakaoId, email, nickname, profileImageUrl);
    }

    public record NormalizedKakaoProfile(Long kakaoId, String email, String nickname, String profileImageUrl) {}
}

