package or.hyu.ssd.api.monitoring;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.hyu.ssd.api.config.ResourceAlertProperties;
import or.hyu.ssd.document.port.ExternalAiPort;
import or.hyu.ssd.document.port.dto.ExternalAiHealthStatus;
import or.hyu.ssd.external.alert.ResourceAlertMessage;
import or.hyu.ssd.external.alert.ResourceAlertSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class ServerResourceAlertMonitor {

    private static final String LEVEL_WARNING = "WARNING";

    private final MeterRegistry meterRegistry;
    private final ResourceAlertProperties properties;
    private final List<ResourceAlertSender> resourceAlertSenders;
    private final ExternalAiPort externalAiPort;
    private final Clock clock = Clock.systemDefaultZone();
    private final Map<ResourceAlertType, ResourceAlertState> states = new EnumMap<>(ResourceAlertType.class);

    private GcPauseSnapshot previousGcPauseSnapshot;

    @Scheduled(fixedDelayString = "${monitoring.resource-alert.check-interval-ms:60000}")
    public void checkNow() {
        if (!properties.isEnabled()) {
            return;
        }

        checkDiskUsage();
        checkCpuUsage();
        checkHeapUsage();
        checkGcPause();
        checkHikariPendingConnection();
        checkExternalAiHealth();
    }

    private void checkDiskUsage() {
        resolveDiskUsage().ifPresent(usage -> evaluateRatio(
                ResourceAlertType.DISK_USAGE,
                usage,
                properties.getDiskUsageThreshold(),
                "Disk 사용률",
                "Docker 이미지, 컨테이너 로그, 볼륨 데이터가 누적되었을 수 있습니다.",
                "서버 디스크 사용량과 Docker 이미지/볼륨/로그 정리 필요 여부를 확인하세요."
        ));
    }

    private void checkCpuUsage() {
        OptionalDouble cpuUsage = findGaugeValue("system.cpu.usage");
        if (cpuUsage.isEmpty()) {
            cpuUsage = findGaugeValue("process.cpu.usage");
        }
        cpuUsage.ifPresent(usage -> evaluateRatio(
                ResourceAlertType.CPU_USAGE,
                usage,
                properties.getCpuUsageThreshold(),
                "CPU 사용률",
                "요청량 증가, CPU bound 작업, 외부 프로세스 점유가 원인일 수 있습니다.",
                "Grafana Host/JVM 대시보드에서 CPU 추이와 최근 요청량을 함께 확인하세요."
        ));
    }

    private void checkHeapUsage() {
        OptionalDouble heapUsed = findGaugeValue("jvm.memory.used", "area", "heap");
        OptionalDouble heapMax = findGaugeValue("jvm.memory.max", "area", "heap");
        if (heapUsed.isEmpty() || heapMax.isEmpty() || heapMax.getAsDouble() <= 0) {
            return;
        }

        double heapUsage = heapUsed.getAsDouble() / heapMax.getAsDouble();
        evaluateRatio(
                ResourceAlertType.HEAP_USAGE,
                heapUsage,
                properties.getHeapUsageThreshold(),
                "JVM Heap 사용률",
                "대용량 문서 처리, 메모리 누수, 과도한 객체 생성이 원인일 수 있습니다.",
                "JVM Heap, GC Pause, 최근 대용량 요청 여부를 확인하세요."
        );
    }

    private void checkGcPause() {
        Timer timer = meterRegistry.find("jvm.gc.pause").timer();
        if (timer == null) {
            return;
        }

        GcPauseSnapshot currentSnapshot = new GcPauseSnapshot(
                timer.count(),
                timer.totalTime(TimeUnit.MILLISECONDS)
        );
        if (previousGcPauseSnapshot == null) {
            previousGcPauseSnapshot = currentSnapshot;
            return;
        }

        long countDelta = currentSnapshot.count() - previousGcPauseSnapshot.count();
        double totalTimeDelta = currentSnapshot.totalTimeMs() - previousGcPauseSnapshot.totalTimeMs();
        previousGcPauseSnapshot = currentSnapshot;

        if (countDelta <= 0 || totalTimeDelta <= 0) {
            reset(ResourceAlertType.GC_PAUSE);
            return;
        }

        double averagePauseMs = totalTimeDelta / countDelta;
        evaluateRaw(
                ResourceAlertType.GC_PAUSE,
                averagePauseMs,
                properties.getGcPauseAverageThresholdMs(),
                "GC 평균 Pause",
                "Heap 압박 또는 Full GC 증가로 Stop-the-world 시간이 길어졌을 수 있습니다.",
                "JVM Heap 사용률, GC 횟수, 대용량 요청 또는 메모리 누수 가능성을 확인하세요."
        );
    }

    private void checkHikariPendingConnection() {
        OptionalDouble pendingConnections = sumGaugeValues("hikaricp.connections.pending");
        pendingConnections.ifPresent(count -> evaluateRaw(
                ResourceAlertType.HIKARI_PENDING,
                count,
                properties.getHikariPendingThreshold(),
                "DB 커넥션 풀 대기",
                "DB 응답 지연, 커넥션 풀 고갈, 장시간 트랜잭션이 원인일 수 있습니다.",
                "HikariCP active/pending, DB slow query, 트랜잭션 지속 시간을 확인하세요."
        ));
    }

    private void checkExternalAiHealth() {
        if (!properties.isExternalAiHealthEnabled()) {
            return;
        }

        try {
            ExternalAiHealthStatus health = externalAiPort.health();
            if (health.available()) {
                reset(ResourceAlertType.EXTERNAL_AI_HEALTH);
                return;
            }
            notifyExternalAiHealthFailure(health.message());
        } catch (Exception e) {
            notifyExternalAiHealthFailure(e.getMessage());
        }
    }

    private OptionalDouble resolveDiskUsage() {
        try {
            FileStore fileStore = Files.getFileStore(Path.of(properties.getDiskPath()));
            long totalSpace = fileStore.getTotalSpace();
            long usableSpace = fileStore.getUsableSpace();
            if (totalSpace <= 0) {
                return OptionalDouble.empty();
            }
            return OptionalDouble.of(1.0 - ((double) usableSpace / totalSpace));
        } catch (IOException | RuntimeException e) {
            log.warn("디스크 사용률 측정에 실패했습니다. path={}", properties.getDiskPath(), e);
            return OptionalDouble.empty();
        }
    }

    private OptionalDouble findGaugeValue(String meterName) {
        Gauge gauge = meterRegistry.find(meterName).gauge();
        if (gauge == null || Double.isNaN(gauge.value())) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of(gauge.value());
    }

    private OptionalDouble findGaugeValue(String meterName, String tagKey, String tagValue) {
        Gauge gauge = meterRegistry.find(meterName).tag(tagKey, tagValue).gauge();
        if (gauge == null || Double.isNaN(gauge.value())) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of(gauge.value());
    }

    private OptionalDouble sumGaugeValues(String meterName) {
        List<Gauge> gauges = meterRegistry.find(meterName).gauges().stream().toList();
        if (gauges.isEmpty()) {
            return OptionalDouble.empty();
        }

        double sum = gauges.stream()
                .mapToDouble(Gauge::value)
                .filter(value -> !Double.isNaN(value))
                .sum();
        return OptionalDouble.of(sum);
    }

    private void evaluateRatio(
            ResourceAlertType type,
            double currentRatio,
            double thresholdRatio,
            String target,
            String possibleCause,
            String actionGuide
    ) {
        evaluate(type, currentRatio >= thresholdRatio, state -> ResourceAlertMessageFactory.ratio(
                LEVEL_WARNING,
                target,
                currentRatio,
                thresholdRatio,
                state.consecutiveCount(),
                properties,
                possibleCause,
                actionGuide
        ));
    }

    private void evaluateRaw(
            ResourceAlertType type,
            double currentValue,
            double threshold,
            String target,
            String possibleCause,
            String actionGuide
    ) {
        evaluate(type, currentValue >= threshold, state -> ResourceAlertMessageFactory.raw(
                LEVEL_WARNING,
                target,
                currentValue,
                threshold,
                state.consecutiveCount(),
                properties,
                possibleCause,
                actionGuide
        ));
    }

    private void notifyExternalAiHealthFailure(String message) {
        String safeMessage = message == null || message.isBlank() ? "외부 AI 서버 health check에 실패했습니다." : message;
        evaluate(ResourceAlertType.EXTERNAL_AI_HEALTH, true, state -> ResourceAlertMessageFactory.status(
                LEVEL_WARNING,
                "외부 AI 서버 Health",
                "DOWN",
                "UP",
                state.consecutiveCount(),
                properties,
                safeMessage,
                "외부 AI 서버 URL, 네트워크 연결, 서버 프로세스 상태를 확인하세요."
        ));
    }

    private void evaluate(ResourceAlertType type, boolean breached, ResourceAlertMessageBuilder messageBuilder) {
        ResourceAlertState state = states.computeIfAbsent(type, ignored -> new ResourceAlertState());
        if (!breached) {
            state.reset();
            return;
        }

        state.recordFailure();
        Instant now = Instant.now(clock);
        ResourceAlertMessage message = messageBuilder.build(state);
        if (!state.isReadyToNotify(properties.getConsecutiveThreshold(), now, properties.getCooldown())) {
            log.debug(
                    "리소스 임계치 초과를 감지했습니다. type={}, target={}, currentValue={}, threshold={}, consecutive={}/{}, possibleCause={}",
                    type,
                    message.target(),
                    message.currentValue(),
                    message.threshold(),
                    message.consecutiveCount(),
                    message.requiredConsecutiveCount(),
                    message.possibleCause()
            );
            return;
        }

        log.warn(
                "리소스 알림을 전송합니다. type={}, target={}, currentValue={}, threshold={}, consecutive={}/{}, possibleCause={}, actionGuide={}",
                type,
                message.target(),
                message.currentValue(),
                message.threshold(),
                message.consecutiveCount(),
                message.requiredConsecutiveCount(),
                message.possibleCause(),
                message.actionGuide()
        );
        resourceAlertSenders.forEach(sender -> sender.send(message));
        state.markNotified(now);
    }

    private void reset(ResourceAlertType type) {
        states.computeIfAbsent(type, ignored -> new ResourceAlertState()).reset();
    }

    @FunctionalInterface
    private interface ResourceAlertMessageBuilder {
        ResourceAlertMessage build(ResourceAlertState state);
    }

    private record GcPauseSnapshot(long count, double totalTimeMs) {
    }
}
