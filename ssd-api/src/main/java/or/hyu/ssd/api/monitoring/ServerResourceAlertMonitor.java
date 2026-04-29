package or.hyu.ssd.api.monitoring;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.hyu.ssd.api.config.ResourceAlertProperties;
import or.hyu.ssd.document.port.ExternalAiPort;
import or.hyu.ssd.common.alert.ResourceAlertContext;
import or.hyu.ssd.common.alert.ResourceAlertNotifier;
import or.hyu.ssd.document.port.dto.ExternalAiHealthStatus;
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
    private final List<ResourceAlertNotifier> resourceAlertNotifiers;
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
        resolveDiskUsage().ifPresent(usage -> evaluate(
                ResourceAlertType.DISK_USAGE,
                usage,
                properties.getDiskUsageThreshold(),
                "Disk 사용률",
                "서버 디스크 사용률이 임계치에 도달했습니다. Docker 이미지, 로그, 볼륨 정리가 필요할 수 있습니다."
        ));
    }

    private void checkCpuUsage() {
        OptionalDouble cpuUsage = findGaugeValue("system.cpu.usage");
        if (cpuUsage.isEmpty()) {
            cpuUsage = findGaugeValue("process.cpu.usage");
        }
        cpuUsage.ifPresent(usage -> evaluate(
                ResourceAlertType.CPU_USAGE,
                usage,
                properties.getCpuUsageThreshold(),
                "CPU 사용률",
                "CPU 사용률이 임계치에 도달했습니다. 요청 증가 또는 CPU bound 작업을 확인해야 합니다."
        ));
    }

    private void checkHeapUsage() {
        OptionalDouble heapUsed = findGaugeValue("jvm.memory.used", "area", "heap");
        OptionalDouble heapMax = findGaugeValue("jvm.memory.max", "area", "heap");
        if (heapUsed.isEmpty() || heapMax.isEmpty() || heapMax.getAsDouble() <= 0) {
            return;
        }

        double heapUsage = heapUsed.getAsDouble() / heapMax.getAsDouble();
        evaluate(
                ResourceAlertType.HEAP_USAGE,
                heapUsage,
                properties.getHeapUsageThreshold(),
                "JVM Heap 사용률",
                "JVM Heap 사용률이 임계치에 도달했습니다. 메모리 누수 또는 과도한 객체 생성을 확인해야 합니다."
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
        evaluateRawValue(
                ResourceAlertType.GC_PAUSE,
                averagePauseMs,
                properties.getGcPauseAverageThresholdMs(),
                "GC 평균 Pause",
                "최근 측정 구간의 GC 평균 pause 시간이 임계치에 도달했습니다. Stop-the-world로 인한 응답 지연 가능성이 있습니다."
        );
    }

    private void checkHikariPendingConnection() {
        OptionalDouble pendingConnections = sumGaugeValues("hikaricp.connections.pending");
        pendingConnections.ifPresent(count -> evaluateRawValue(
                ResourceAlertType.HIKARI_PENDING,
                count,
                properties.getHikariPendingThreshold(),
                "DB 커넥션 풀 대기",
                "HikariCP pending connection이 발생했습니다. DB 병목 또는 커넥션 풀 고갈 가능성이 있습니다."
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

    private void evaluate(ResourceAlertType type, double currentRatio, double thresholdRatio, String metric, String description) {
        evaluate(
                type,
                currentRatio >= thresholdRatio,
                ResourceAlertContextFactory.ratio(LEVEL_WARNING, "SSD Server", metric, currentRatio, thresholdRatio, description)
        );
    }

    private void evaluateRawValue(ResourceAlertType type, double currentValue, double threshold, String metric, String description) {
        evaluate(
                type,
                currentValue >= threshold,
                ResourceAlertContextFactory.raw(LEVEL_WARNING, "SSD Server", metric, currentValue, threshold, description)
        );
    }

    private void notifyExternalAiHealthFailure(String message) {
        String safeMessage = message == null || message.isBlank() ? "외부 AI 서버 health check에 실패했습니다." : message;
        evaluate(
                ResourceAlertType.EXTERNAL_AI_HEALTH,
                true,
                new ResourceAlertContext(
                        LEVEL_WARNING,
                        "External AI Server",
                        "외부 AI 서버 Health",
                        "DOWN",
                        "UP",
                        safeMessage
                )
        );
    }

    private void evaluate(ResourceAlertType type, boolean breached, ResourceAlertContext context) {
        ResourceAlertState state = states.computeIfAbsent(type, ignored -> new ResourceAlertState());
        if (!breached) {
            state.reset();
            return;
        }

        state.recordFailure();
        Instant now = Instant.now(clock);
        if (!state.isReadyToNotify(properties.getConsecutiveThreshold(), now, properties.getCooldown())) {
            return;
        }

        resourceAlertNotifiers.forEach(notifier -> notifier.notify(context));
        state.markNotified(now);
    }

    private void reset(ResourceAlertType type) {
        states.computeIfAbsent(type, ignored -> new ResourceAlertState()).reset();
    }

    private record GcPauseSnapshot(long count, double totalTimeMs) {
    }
}
