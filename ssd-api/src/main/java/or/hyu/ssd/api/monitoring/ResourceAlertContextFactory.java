package or.hyu.ssd.api.monitoring;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import or.hyu.ssd.common.alert.ResourceAlertContext;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class ResourceAlertContextFactory {

    static ResourceAlertContext ratio(
            String level,
            String target,
            String metric,
            double currentRatio,
            double thresholdRatio,
            String description
    ) {
        return new ResourceAlertContext(
                level,
                target,
                metric,
                formatPercent(currentRatio),
                formatPercent(thresholdRatio),
                description
        );
    }

    static ResourceAlertContext raw(
            String level,
            String target,
            String metric,
            double currentValue,
            double threshold,
            String description
    ) {
        return new ResourceAlertContext(
                level,
                target,
                metric,
                formatNumber(currentValue),
                formatNumber(threshold),
                description
        );
    }

    private static String formatPercent(double value) {
        return "%.2f%%".formatted(value * 100);
    }

    private static String formatNumber(double value) {
        return "%.2f".formatted(value);
    }
}
