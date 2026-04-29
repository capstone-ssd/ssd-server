package or.hyu.ssd.common.alert;

public record ResourceAlertContext(
        String level,
        String target,
        String metric,
        String currentValue,
        String threshold,
        String description
) {
}
