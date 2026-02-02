package or.hyu.ssd.infra.repository.jpa;

import or.hyu.ssd.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberJpaRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String username);

    Boolean existsByEmail(String email);
}
