package or.hyu.ssd.infra.persistence.member.mapper;

import or.hyu.ssd.infra.persistence.base.BaseJpaEntity;
import or.hyu.ssd.infra.persistence.member.entity.MemberJpaEntity;
import or.hyu.ssd.member.domain.model.Member;
import or.hyu.ssd.shared.domain.AuditableDomainEntity;

public final class MemberPersistenceMapper {

    private MemberPersistenceMapper() {
    }

    public static Member toDomain(MemberJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        Member.MemberBuilder<?, ?> builder = Member.builder()
                .id(entity.getId())
                .name(entity.getName())
                .email(entity.getEmail())
                .profileImageUrl(entity.getProfileImageUrl())
                .profileImageKey(entity.getProfileImageKey())
                .role(entity.getRole());
        applyAudit(entity, builder);
        return builder.build();
    }

    public static MemberJpaEntity toJpa(Member member) {
        if (member == null) {
            return null;
        }

        MemberJpaEntity.MemberJpaEntityBuilder<?, ?> builder = MemberJpaEntity.builder()
                .id(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .profileImageUrl(member.getProfileImageUrl())
                .profileImageKey(member.getProfileImageKey())
                .role(member.getRole());
        applyAudit(member, builder);
        return builder.build();
    }

    public static MemberJpaEntity toRef(Member member) {
        if (member == null || member.getId() == null) {
            return null;
        }

        return MemberJpaEntity.builder()
                .id(member.getId())
                .build();
    }

    private static void applyAudit(
            BaseJpaEntity source,
            AuditableDomainEntity.AuditableDomainEntityBuilder<?, ?> builder
    ) {
        builder.createdAt(source.getCreatedAt());
        builder.updatedAt(source.getUpdatedAt());
    }

    private static void applyAudit(
            AuditableDomainEntity source,
            BaseJpaEntity.BaseJpaEntityBuilder<?, ?> builder
    ) {
        builder.createdAt(source.getCreatedAt());
        builder.updatedAt(source.getUpdatedAt());
    }
}
