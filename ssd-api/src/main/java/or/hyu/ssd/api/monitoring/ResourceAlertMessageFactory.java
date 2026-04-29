package or.hyu.ssd.api.monitoring;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import or.hyu.ssd.api.config.ResourceAlertProperties;
import or.hyu.ssd.external.alert.ResourceAlertMessage;

import java.time.Duration;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class ResourceAlertMessageFactory {

    static ResourceAlertMessage ratio(
            String level,
            String target,
            double currentRatio,
            double thresholdRatio,
            int consecutiveCount,
            ResourceAlertProperties properties,
            String possibleCause,
            String actionGuide
    ) {
        return new ResourceAlertMessage(
                level,
                target,
                formatPercent(currentRatio),
                formatPercent(thresholdRatio),
                consecutiveCount,
                properties.getConsecutiveThreshold(),
                formatDuration(properties.getCooldown()),
                possibleCause,
                actionGuide
        );
    }

    static ResourceAlertMessage raw(
            String level,
            String target,
            double currentValue,
            double threshold,
            int consecutiveCount,
            ResourceAlertProperties properties,
            String possibleCause,
            String actionGuide
    ) {
        return new ResourceAlertMessage(
                level,
                target,
                formatNumber(currentValue),
                formatNumber(threshold),
                consecutiveCount,
                properties.getConsecutiveThreshold(),
                formatDuration(properties.getCooldown()),
                possibleCause,
                actionGuide
        );
    }

    static ResourceAlertMessage status(
            String level,
            String target,
            String currentValue,
            String threshold,
            int consecutiveCount,
            ResourceAlertProperties properties,
            String possibleCause,
            String actionGuide
    ) {
        return new ResourceAlertMessage(
                level,
                target,
                currentValue,
                threshold,
                consecutiveCount,
                properties.getConsecutiveThreshold(),
                formatDuration(properties.getCooldown()),
                possibleCause,
                actionGuide
        );
    }

    private static String formatPercent(double value) {
        return "%.2f%%".formatted(value * 100);
    }

    private static String formatNumber(double value) {
        return "%.2f".formatted(value);
    }

    private static String formatDuration(Duration duration) {
        if (duration == null || duration.isZero()) {
            return "0s";
        }
        long seconds = duration.toSeconds();
        if (seconds % 60 == 0) {
            return (seconds / 60) + "m";
        }
        return seconds + "s";
    }
}
