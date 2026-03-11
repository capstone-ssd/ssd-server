# EC2 Deploy Assets

이 디렉터리는 SSD dev 서버 배포에 필요한 비민감 설정을 형상관리합니다.

## 포함 파일
- `docker-compose.yml`: 공용 인프라(redis, network) 정의
- `nginx/site.conf.template`: dev 도메인 프록시 설정 템플릿
- `nginx/zzz-ssd-timeout.conf`: 장시간 AI 요청용 timeout 설정
- `install_infra.sh`: 서버에 정적 인프라 파일을 설치하고 redis를 기동
- `install_monitoring.sh`: Grafana/Prometheus/load-test 모니터링 스택 설치
- `blue_green_deploy.sh`: blue/green 무중단 배포 스크립트

## 민감 정보 분리
- `application-dev.yml`은 Git에 커밋하지 않습니다.
- CI에서 secret으로 생성한 뒤 서버 `/opt/ssd/config/application-dev.yml`로 배포합니다.
- 애플리케이션 컨테이너는 외부 설정 파일을 `/config/application-dev.yml`로 마운트해 사용합니다.

## 모니터링 설정
- 모니터링 리소스는 `k6/monitoring`에 형상관리합니다.
- `develop-cd`는 서버 `/opt/ssd/monitoring`에 해당 파일을 설치하고 docker compose로 Grafana/Prometheus를 기동합니다.
- 기본 접속 포트:
  - Grafana: `3000`
  - Prometheus: `9090`
  - k6 Web Dashboard: `5665`
