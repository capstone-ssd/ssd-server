package or.hyu.ssd.domain.member.controller.dto.kakao;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 카카오 사용자 정보 조회 응답 DTO
 * /v2/user/me 응답과 매핑됨
 */
@Getter
@Setter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KaKaoUserInfoResponse {

    /** 카카오 회원 번호 (고유 ID) */
    private Long id;

    /** 서비스와 계정 연결 시각 */
    private String connectedAt;

    /** 프로필 기본 정보 */
    private Properties properties;

    /** 상세 계정 정보 (이메일, 프로필 등) */
    private KakaoAccount kakaoAccount;

    @Getter
    @Setter
    public static class Properties {

        /** 카카오 닉네임 */
        private String nickname;

        /** 프로필 이미지 URL */
        private String profileImage;

        /** 썸네일 이미지 URL */
        private String thumbnailImage;
    }

    @Getter
    @Setter
    public static class KakaoAccount {

        /** 이메일 */
        private String email;

        /** 이메일 유효 여부 (형식 등) */
        private Boolean isEmailValid;

        /** 이메일 인증 여부 */
        private Boolean isEmailVerified;

        /** 이메일 보유 여부 */
        private Boolean hasEmail;

        /** 닉네임 제공을 위해 동의가 필요한지 여부 */
        private Boolean profileNicknameNeedsAgreement;

        /** 이메일 제공을 위해 동의가 필요한지 여부 */
        private Boolean emailNeedsAgreement;

        /** 프로필 정보 객체 */
        private Profile profile;

        @Getter
        @Setter
        public static class Profile {

            /** 프로필 닉네임 */
            private String nickname;

            /** 기본 닉네임 사용 여부 (true면 기본값) */
            private Boolean isDefaultNickname;
        }
    }
}