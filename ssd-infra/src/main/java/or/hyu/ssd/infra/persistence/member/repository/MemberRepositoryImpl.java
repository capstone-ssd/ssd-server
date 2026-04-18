package or.hyu.ssd.infra.persistence.member.repository;

import lombok.RequiredArgsConstructor;
import or.hyu.ssd.infra.persistence.member.mapper.MemberPersistenceMapper;
import or.hyu.ssd.member.domain.model.Member;
import or.hyu.ssd.member.repository.MemberRepository;
import or.hyu.ssd.infra.persistence.member.repository.jpa.MemberJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepository {
    private final MemberJpaRepository memberJpaRepository;

    @Override
    public Optional<Member> findById(Long id) {
        return memberJpaRepository.findById(id).map(MemberPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Member> findByEmail(String username) {
        return memberJpaRepository.findByEmail(username).map(MemberPersistenceMapper::toDomain);
    }

    @Override
    public Boolean existsByEmail(String email) {
        return memberJpaRepository.existsByEmail(email);
    }

    @Override
    public Member save(Member member) {
        return MemberPersistenceMapper.toDomain(memberJpaRepository.save(MemberPersistenceMapper.toJpa(member)));
    }
}
