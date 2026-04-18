package or.hyu.ssd.member.application.service;

import or.hyu.ssd.member.application.result.GetMyMemberResult;
import or.hyu.ssd.member.domain.model.Member;
import or.hyu.ssd.member.domain.model.Role;
import or.hyu.ssd.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    @Test
    @DisplayName("getMyInfo()는 로그인한 회원의 최신 정보를 반환한다")
    void getMyInfo_returnsLatestMemberInfo() {
        // given
        Member principalMember = member(1L, "principal@example.com", "기존 이름");
        Member persistedMember = member(1L, "principal@example.com", "최신 이름");
        Long user = principalMember.getId();
        when(memberRepository.findById(1L)).thenReturn(Optional.of(persistedMember));

        // when
        GetMyMemberResult response = memberService.getMyInfo(1L);

        // then
        assertThat(response.memberId()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("최신 이름");
        assertThat(response.email()).isEqualTo("principal@example.com");
        assertThat(response.profileImageUrl()).isEqualTo("https://image.example.com/profile.png");
        assertThat(response.role()).isEqualTo("ROLE_AUTHOR");
    }

    @Test
    @DisplayName("getMyInfo()는 회원이 없으면 MEMBER_NOT_FOUND를 던진다")
    void getMyInfo_throwsWhenMemberMissing() {
        // given
        Long user = member(1L, "principal@example.com", "이름").getId();
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> memberService.getMyInfo(1L))
                .isInstanceOf(or.hyu.ssd.common.exception.UserExceptionHandler.class)
                .hasMessage("회원을 찾지 못했습니다");
    }

    private Member member(Long id, String email, String name) {
        return Member.builder()
                .id(id)
                .name(name)
                .email(email)
                .profileImageUrl("https://image.example.com/profile.png")
                .profileImageKey("profile-key")
                .role(Role.ROLE_AUTHOR)
                .build();
    }
}
