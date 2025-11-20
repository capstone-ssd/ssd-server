package or.hyu.ssd.domain.member.service;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import or.hyu.ssd.domain.member.entity.Member;
import or.hyu.ssd.global.api.ErrorCode;
import or.hyu.ssd.global.api.handler.UserExceptionHandler;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    /**
     * 여기에 회원 정보를 저장해서 우리가 회원정보를 가져올 수 있게 됩니다!
     *
     * 해당 세션의 생명주기는 하나의 요청에 한정됩니다
     * 새로운 요청이 들어오면 UserDetail 은 초기화 됩니다
     * */
    private final Member member;

    // Role 반환
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (member == null) {
            throw new UserExceptionHandler(ErrorCode.MEMBER_NOT_FOUND);
        }
        if (member.getRole() == null) {
            throw new UserExceptionHandler(ErrorCode.MEMBER_ROLE_EXCEPTION);
        }

        Collection<GrantedAuthority> collection = new ArrayList<>();
        collection.add(() -> member.getRole().name());
        return collection;
    }


    /**
     * 비밀번호가 존재하지 않으므로 getPassword 메서드는 구현되지 않습니다
     * */
    @Override
    public String getPassword() {
        return "";
    }

    // 소셜로그인이기에 username이 존재하지 않음
    @Override
    public String getUsername() {
        return "";
    }

    public String getEmail() {
        return member != null ? member.getEmail() : null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
