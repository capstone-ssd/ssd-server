package or.hyu.ssd.shared.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AuditableDomainEntity {

    protected LocalDateTime createdAt;
    protected LocalDateTime updatedAt;
}
