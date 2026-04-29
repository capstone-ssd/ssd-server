package or.hyu.ssd.external.alert;

import io.swagger.v3.oas.annotations.media.Schema;

public record ResourceAlertMessage(
        @Schema(description = "알림 등급")
        String level,

        @Schema(description = "알림 대상")
        String target,

        @Schema(description = "현재 측정값")
        String currentValue,

        @Schema(description = "알림 기준값")
        String threshold,

        @Schema(description = "현재까지 연속으로 임계치를 초과한 횟수")
        int consecutiveCount,

        @Schema(description = "알림 발생에 필요한 연속 초과 횟수")
        int requiredConsecutiveCount,

        @Schema(description = "동일 알림 재전송 제한 시간")
        String cooldown,

        @Schema(description = "가능한 원인")
        String possibleCause,

        @Schema(description = "확인 또는 대응 가이드")
        String actionGuide
) {
}
