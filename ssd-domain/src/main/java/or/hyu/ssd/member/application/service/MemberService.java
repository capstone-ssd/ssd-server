package or.hyu.ssd.member.application.service;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.member.application.result.GetMyMemberResult;
import or.hyu.ssd.member.domain.model.Member;
import or.hyu.ssd.member.repository.MemberRepository;
import or.hyu.ssd.common.exception.ErrorCode;
import or.hyu.ssd.common.exception.UserExceptionHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    public GetMyMemberResult getMyInfo(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new UserExceptionHandler(ErrorCode.MEMBER_NOT_FOUND));

        return GetMyMemberResult.from(member);
    }
}
