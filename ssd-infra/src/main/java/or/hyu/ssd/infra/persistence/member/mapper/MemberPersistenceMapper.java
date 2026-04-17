package or.hyu.ssd.infra.persistence.member.mapper;

import or.hyu.ssd.infra.persistence.base.BaseJpaEntity;
import or.hyu.ssd.infra.persistence.member.entity.MemberJpaEntity;
import or.hyu.ssd.member.domain.entity.Member;
import or.hyu.ssd.shared.domain.AuditableDomainEntity;

public final class MemberPersistenceMapper {

    private MemberPersistenceMapper() {
    }

    public static Member toDomain(MemberJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        Member member = new Member();
        copyAudit(entity, member);
        member.setId(entity.getId());
        member.setName(entity.getName());
        member.setEmail(entity.getEmail());
        member.setProfileImageUrl(entity.getProfileImageUrl());
        member.setProfileImageKey(entity.getProfileImageKey());
        member.setRole(entity.getRole());
        return member;
    }

    public static MemberJpaEntity toJpa(Member member) {
        if (member == null) {
            return null;
        }

        MemberJpaEntity entity = new MemberJpaEntity();
        copyAudit(member, entity);
        entity.setId(member.getId());
        entity.setName(member.getName());
        entity.setEmail(member.getEmail());
        entity.setProfileImageUrl(member.getProfileImageUrl());
        entity.setProfileImageKey(member.getProfileImageKey());
        entity.setRole(member.getRole());
        return entity;
    }

    public static MemberJpaEntity toRef(Member member) {
        if (member == null || member.getId() == null) {
            return null;
        }
        MemberJpaEntity entity = new MemberJpaEntity();
        entity.setId(member.getId());
        return entity;
    }

    private static void copyAudit(BaseJpaEntity source, AuditableDomainEntity target) {
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
    }

    private static void copyAudit(AuditableDomainEntity source, BaseJpaEntity target) {
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
    }
}
