package or.hyu.ssd.infra.persistence.member.repository.jpa;

import or.hyu.ssd.member.domain.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberJpaRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String username);

    Boolean existsByEmail(String email);
}
