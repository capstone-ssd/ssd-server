package or.hyu.ssd.member.repository;

import or.hyu.ssd.member.domain.model.Member;

import java.util.Optional;

public interface MemberRepository {
    Optional<Member> findById(Long id);

    Optional<Member> findByEmail(String username);

    Boolean existsByEmail(String email);

    Member save(Member member);
}
