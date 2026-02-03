package or.hyu.ssd.infra.member.repository;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.domain.member.entity.Member;
import or.hyu.ssd.domain.member.repository.MemberRepository;
import or.hyu.ssd.infra.member.repository.jpa.MemberJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepository {
    private final MemberJpaRepository memberJpaRepository;

    @Override
    public Optional<Member> findById(Long id) {
        return memberJpaRepository.findById(id);
    }

    @Override
    public Optional<Member> findByEmail(String username) {
        return memberJpaRepository.findByEmail(username);
    }

    @Override
    public Boolean existsByEmail(String email) {
        return memberJpaRepository.existsByEmail(email);
    }

    @Override
    public Member save(Member member) {
        return memberJpaRepository.save(member);
    }
}
