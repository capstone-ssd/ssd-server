# EC2 Deploy Assets

이 디렉터리는 SSD dev 서버 배포에 필요한 비민감 설정을 형상관리합니다.

## 포함 파일
- `docker-compose.yml`: 공용 인프라(redis, network) 정의
- `nginx/site.conf.template`: dev 도메인 프록시 설정 템플릿
- `nginx/zzz-ssd-timeout.conf`: 장시간 AI 요청용 timeout 설정
- `install_infra.sh`: 서버에 정적 인프라 파일을 설치하고 redis를 기동
- `install_prod_monitoring.sh`: 운영용 Grafana/Prometheus/Alertmanager 모니터링 스택 설치
- `install_monitoring.sh`: 부하테스트용 Grafana/Prometheus/k6 모니터링 스택 설치
- `blue_green_deploy.sh`: blue/green 무중단 배포 스크립트

## 민감 정보 분리
- `application-dev.yml`은 Git에 커밋하지 않습니다.
- CI에서 secret으로 생성한 뒤 서버 `/opt/ssd/config/application-dev.yml`로 배포합니다.
- 애플리케이션 컨테이너는 외부 설정 파일을 `/config/application-dev.yml`로 마운트해 사용합니다.

## 모니터링 설정
- 운영 모니터링 리소스는 `deploy/ec2/monitoring`에 형상관리합니다.
- `develop-cd`는 서버 `/opt/ssd/monitoring-prod`에 해당 파일을 설치하고 docker compose로 Grafana/Prometheus/Alertmanager를 기동합니다.
- 운영 모니터링은 SSH 터널링 기준으로 접근합니다.
  - Grafana: `3001`
  - Prometheus: `9091`
  - Alertmanager: `9093`
- 부하테스트용 모니터링 리소스는 `k6/monitoring`에 형상관리합니다.
- 부하테스트용 모니터링은 평시 서버 RAM 절약을 위해 기본적으로 기동하지 않습니다.
- 부하테스트 모니터링이 필요할 때만 `LOADTEST_MONITORING_ENABLED=true`로 `install_monitoring.sh`를 실행합니다.

## JVM/GC 설정
- dev 애플리케이션 컨테이너는 기본적으로 아래 JVM 옵션으로 실행됩니다.
  - `-Xms256m`
  - `-Xmx256m`
  - `-Xlog:gc*,safepoint:file=/logs/gc.log:time,uptime,level,tags:filecount=5,filesize=10M`
- 호스트의 GC 로그 위치는 `/opt/ssd/logs/gc.log*`입니다.
- 필요하면 CD 실행 환경에서 `APP_JAVA_TOOL_OPTIONS`와 `APP_LOG_DIR`로 값을 덮어쓸 수 있습니다.
