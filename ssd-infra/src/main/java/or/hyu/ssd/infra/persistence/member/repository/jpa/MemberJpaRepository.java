package or.hyu.ssd.infra.persistence.member.repository.jpa;

import or.hyu.ssd.infra.persistence.member.entity.MemberJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberJpaRepository extends JpaRepository<MemberJpaEntity, Long> {
    Optional<MemberJpaEntity> findByEmail(String username);

    Boolean existsByEmail(String email);
}
