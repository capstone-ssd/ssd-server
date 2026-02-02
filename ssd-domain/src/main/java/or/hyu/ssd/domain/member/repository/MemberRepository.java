package or.hyu.ssd.domain.member.repository;

import or.hyu.ssd.domain.member.entity.Member;

import java.util.Optional;

public interface MemberRepository {
    Optional<Member> findById(Long id);

    Optional<Member> findByEmail(String username);

    Boolean existsByEmail(String email);

    Member save(Member member);
}
