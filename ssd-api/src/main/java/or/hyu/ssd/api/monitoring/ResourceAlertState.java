package or.hyu.ssd.api.monitoring;

import java.time.Duration;
import java.time.Instant;

class ResourceAlertState {

    private int consecutiveCount;
    private Instant lastNotifiedAt;

    void recordFailure() {
        consecutiveCount++;
    }

    void reset() {
        consecutiveCount = 0;
    }

    int consecutiveCount() {
        return consecutiveCount;
    }

    boolean isReadyToNotify(int requiredConsecutiveCount, Instant now, Duration cooldown) {
        if (consecutiveCount < requiredConsecutiveCount) {
            return false;
        }
        return lastNotifiedAt == null || !lastNotifiedAt.plus(cooldown).isAfter(now);
    }

    void markNotified(Instant now) {
        lastNotifiedAt = now;
    }
}
