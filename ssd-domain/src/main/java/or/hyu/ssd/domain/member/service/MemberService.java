package or.hyu.ssd.domain.member.service;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.domain.member.controller.dto.GetMyMemberResponse;
import or.hyu.ssd.domain.member.entity.Member;
import or.hyu.ssd.domain.member.repository.MemberRepository;
import or.hyu.ssd.common.exception.ErrorCode;
import or.hyu.ssd.common.exception.UserExceptionHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    public GetMyMemberResponse getMyInfo(CustomUserDetails user) {
        Long memberId = user.getMember().getId();

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new UserExceptionHandler(ErrorCode.MEMBER_NOT_FOUND));

        return GetMyMemberResponse.from(member);
    }
}
