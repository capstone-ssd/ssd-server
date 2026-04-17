package or.hyu.ssd.shared.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AuditableDomainEntity {

    protected LocalDateTime createdAt;
    protected LocalDateTime updatedAt;
}
