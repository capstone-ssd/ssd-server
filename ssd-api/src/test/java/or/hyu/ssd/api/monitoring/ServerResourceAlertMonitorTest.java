package or.hyu.ssd.api.monitoring;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import or.hyu.ssd.api.config.ResourceAlertProperties;
import or.hyu.ssd.document.port.ExternalAiPort;
import or.hyu.ssd.common.alert.ResourceAlertContext;
import or.hyu.ssd.common.alert.ResourceAlertNotifier;
import or.hyu.ssd.document.port.dto.ExternalAiHealthStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServerResourceAlertMonitorTest {

    @Mock
    private ResourceAlertNotifier resourceAlertNotifier;

    @Mock
    private ExternalAiPort externalAiPort;

    @Test
    @DisplayName("checkNow()는 CPU 사용률이 임계치를 넘으면 리소스 알림을 보낸다")
    void checkNow_notifiesWhenCpuUsageExceedsThreshold() {
        // given
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        AtomicReference<Double> cpuUsage = new AtomicReference<>(0.90);
        Gauge.builder("system.cpu.usage", cpuUsage, AtomicReference::get).register(meterRegistry);
        ServerResourceAlertMonitor monitor = new ServerResourceAlertMonitor(
                meterRegistry,
                properties(),
                List.of(resourceAlertNotifier),
                externalAiPort
        );

        // when
        monitor.checkNow();

        // then
        ArgumentCaptor<ResourceAlertContext> contextCaptor = ArgumentCaptor.forClass(ResourceAlertContext.class);
        verify(resourceAlertNotifier).notify(contextCaptor.capture());
        assertThat(contextCaptor.getValue().metric()).isEqualTo("CPU 사용률");
        assertThat(contextCaptor.getValue().currentValue()).isEqualTo("90.00%");
        assertThat(contextCaptor.getValue().threshold()).isEqualTo("80.00%");
    }

    @Test
    @DisplayName("checkNow()는 CPU 사용률이 임계치 미만이면 리소스 알림을 보내지 않는다")
    void checkNow_doesNotNotifyWhenCpuUsageIsBelowThreshold() {
        // given
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        AtomicReference<Double> cpuUsage = new AtomicReference<>(0.50);
        Gauge.builder("system.cpu.usage", cpuUsage, AtomicReference::get).register(meterRegistry);
        ServerResourceAlertMonitor monitor = new ServerResourceAlertMonitor(
                meterRegistry,
                properties(),
                List.of(resourceAlertNotifier),
                externalAiPort
        );

        // when
        monitor.checkNow();

        // then
        verify(resourceAlertNotifier, never()).notify(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("checkNow()는 외부 AI 서버 Health가 DOWN이면 리소스 알림을 보낸다")
    void checkNow_notifiesWhenExternalAiHealthIsDown() {
        // given
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        ResourceAlertProperties properties = properties();
        properties.setExternalAiHealthEnabled(true);
        when(externalAiPort.health()).thenReturn(ExternalAiHealthStatus.down("외부 AI 서버에 연결할 수 없습니다."));
        ServerResourceAlertMonitor monitor = new ServerResourceAlertMonitor(
                meterRegistry,
                properties,
                List.of(resourceAlertNotifier),
                externalAiPort
        );

        // when
        monitor.checkNow();

        // then
        ArgumentCaptor<ResourceAlertContext> contextCaptor = ArgumentCaptor.forClass(ResourceAlertContext.class);
        verify(resourceAlertNotifier).notify(contextCaptor.capture());
        assertThat(contextCaptor.getValue().target()).isEqualTo("External AI Server");
        assertThat(contextCaptor.getValue().metric()).isEqualTo("외부 AI 서버 Health");
        assertThat(contextCaptor.getValue().currentValue()).isEqualTo("DOWN");
    }

    private ResourceAlertProperties properties() {
        ResourceAlertProperties properties = new ResourceAlertProperties();
        properties.setEnabled(true);
        properties.setCooldown(Duration.ZERO);
        properties.setConsecutiveThreshold(1);
        properties.setCpuUsageThreshold(0.80);
        properties.setDiskUsageThreshold(1.0);
        properties.setHeapUsageThreshold(1.0);
        properties.setGcPauseAverageThresholdMs(Double.MAX_VALUE);
        properties.setHikariPendingThreshold(Double.MAX_VALUE);
        properties.setExternalAiHealthEnabled(false);
        return properties;
    }
}
