# SSD k6 / Monitoring Bootstrap

## 목적
- SSD 부하테스트를 위한 모니터링 스택을 동일 서버에 구성합니다.
- Grafana/Prometheus는 서버 주소로 직접 접근하고, k6 web dashboard도 서버 포트로 직접 접근합니다.

## 포함 구성
- `k6/monitoring/docker-compose.monitoring.yml`
  - Prometheus
  - Grafana
  - node-exporter
  - cadvisor
- `k6/monitoring/grafana/dashboards`
  - 호스트/컨테이너 모니터링 대시보드
  - k6 메트릭 대시보드
  - JVM 메트릭 대시보드

## 서버 설치
`develop-cd`에서 `deploy/ec2/install_monitoring.sh`가 실행되면 자동으로 설치됩니다.

직접 설치가 필요하면 서버에서 아래를 실행합니다.

```bash
sudo /tmp/deploy/ec2/install_monitoring.sh
```

## 접속 주소
- Grafana: `http://<SERVER_IP>:3000`
- Prometheus: `http://<SERVER_IP>:9090`
- k6 Web Dashboard: `http://<SERVER_IP>:5665`

## JVM 메트릭
- SSD 애플리케이션은 `/actuator/prometheus`를 공개해 Prometheus가 JVM 메트릭을 수집합니다.
- Grafana의 `SSD Load Test JVM Overview` 대시보드에서 아래 항목을 확인할 수 있습니다.
  - JVM Up
  - Process Uptime
  - Heap / Non-heap Memory
  - Live / Daemon / Peak Threads
  - GC Activity
  - HikariCP Connections

## k6 메트릭을 Prometheus로 보내는 방법
Prometheus는 remote write receiver를 활성화해 두었습니다.

```bash
K6_PROMETHEUS_RW_SERVER_URL=http://<SERVER_IP>:9090/api/v1/write \
K6_PROMETHEUS_RW_TREND_STATS=p(95),p(99),avg,max \
K6_WEB_DASHBOARD=true \
K6_WEB_DASHBOARD_HOST=0.0.0.0 \
K6_WEB_DASHBOARD_PORT=5665 \
k6 run -o experimental-prometheus-rw <scenario.js>
```

## 참고 사항
- `5665/tcp`, `3000/tcp`, `9090/tcp`는 서버 `ufw`에서 허용합니다.
- 외부에서 직접 접속하려면 AWS Security Group도 동일 포트를 열어야 합니다.
- SSD 애플리케이션 메트릭(`/actuator/prometheus`)은 아직 포함하지 않았습니다. 해당 엔드포인트를 노출하면 Prometheus scrape job만 추가하면 됩니다.
