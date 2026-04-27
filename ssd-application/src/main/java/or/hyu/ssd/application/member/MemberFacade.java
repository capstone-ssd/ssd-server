package or.hyu.ssd.application.member;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.member.application.result.GetMyMemberResult;
import or.hyu.ssd.member.application.service.MemberService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberFacade {

    private final MemberService memberService;

    public GetMyMemberResult getMyInfo(Long memberId) {
        return memberService.getMyInfo(memberId);
    }
}
