package or.hyu.ssd.member.application.service;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.member.domain.entity.Member;
import or.hyu.ssd.member.repository.MemberRepository;
import or.hyu.ssd.common.exception.ErrorCode;
import or.hyu.ssd.common.exception.UserExceptionHandler;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member user = memberRepository.findByEmail(username)
                .orElseThrow(() -> new UserExceptionHandler(ErrorCode.MEMBER_NOT_FOUND));

        CustomUserDetails customUserDetails = new CustomUserDetails(user);
        return customUserDetails;
    }

    // JWT 토큰에서 ID 기반으로 조회할 때 사용하는 메서드
    public CustomUserDetails loadUserById(Long id) {
        Member user = memberRepository.findById(id)
                .orElseThrow(() -> new UserExceptionHandler(ErrorCode.MEMBER_NOT_FOUND));
        return new CustomUserDetails(user);
    }
}
