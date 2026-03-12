# SSD k6 / Monitoring Bootstrap

## 목적
- SSD의 부하테스트를 로컬 `k6` 실행 기준으로 시작할 수 있는 최소 구조를 제공합니다.
- 현재는 `POST /api/v1/documents`의 CPU bound 검증에 집중합니다.
- Grafana/Prometheus는 서버 주소로 직접 접근하고, k6는 로컬에서 서버를 타격합니다.

## 현재 구조
- `k6/monitoring`
  - Prometheus, Grafana, node-exporter, cadvisor
- `k6/lib/config.js`
  - 공통 환경변수 파싱
  - 토큰/결과 경로/목표 SLO 관리
- `k6/lib/document_payloads.js`
  - 문서 생성 payload fixture(S/M/L/XL)
- `k6/lib/common.js`
  - 인증 헤더, 문서 생성 요청, 응답 검증, 공통 커스텀 메트릭
- `k6/lib/summary.js`
  - 실행 결과를 `k6/results`에 파일로 저장
- `k6/scenarios/cpu`
  - `smoke.js`
  - `payload-curve.js`
  - `target.js`
  - `stress.js`
- `k6/results`
  - 시나리오별 결과와 보고서 저장 위치
  - 예시
    - `k6/results/document-create/1st-test/report.md`
    - `k6/results/document-create/1st-test/smoke/result.json`
    - `k6/results/external-ai/canary/pass-1/result.json`
- `k6/secrets`
  - 로컬 토큰 파일 보관 위치 (`.gitignore` 대상)

## 서버 모니터링 접속 주소
- Grafana: 내부 모니터링 호스트의 `:3000`
- Prometheus: 내부 모니터링 호스트의 `:9090`
- Prometheus Targets: 내부 모니터링 호스트의 `/targets`
- k6 Web Dashboard: 내부 모니터링 호스트의 `:5665`
  - 실제 주소는 저장소에 기록하지 않고 내부 운영 문서 또는 시크릿 변수로 관리합니다.
  - 이 포트는 k6를 `K6_WEB_DASHBOARD=true`로 실행할 때만 열립니다.

## JVM 메트릭
- SSD 앱은 `/actuator/prometheus`를 공개합니다.
- Grafana `SSD Load Test JVM Overview`에서 아래 항목을 확인할 수 있습니다.
  - JVM Up
  - Process Uptime
  - Heap / Non-heap Memory
  - Live / Peak Threads
  - GC Activity
  - HikariCP Connections

## CPU bound 테스트 전제
- 타깃 API: `POST /api/v1/documents`
- 큰 `text` JSON 파싱
- `resolveTitle()`의 본문 스캔
- `documents.content(TEXT)` insert
- `document_paragraphs`, `document_logs` insert
- 현재 스크립트는 이 경로를 payload 크기별로 반복 호출해 CPU/GC/DB 대기 추세를 관측합니다.

## 토큰 준비
SSD에는 테스트용 일반 로그인 API가 없으므로 access token을 직접 준비해야 합니다.
다음 셋 중 하나를 사용합니다.

1. 단일 토큰
```bash
export ACCESS_TOKEN='Bearer 없이 access token 값만 입력'
```

2. 여러 토큰
```bash
export ACCESS_TOKENS='token1,token2,token3'
```

3. 파일 입력
```bash
cat <<'EOF2' > k6/secrets/access_tokens.txt
token1
token2
token3
EOF2
export ACCESS_TOKENS_FILE='k6/secrets/access_tokens.txt'
```

## 주요 환경변수
- `BASE_URL`
  - 기본값: `https://dev-api.simsaimdang.shop`
- `TARGET_PATH`
  - 기본값: `/api/v1/documents`
- `ACCESS_TOKEN`
- `ACCESS_TOKENS`
- `ACCESS_TOKENS_FILE`
- `REQUEST_TIMEOUT`
  - 기본값: `30s`
- `THINK_TIME_SECONDS`
  - 기본값: `1`
- `FOLDER_ID`
  - 기본값: `0`
- `RUN_LABEL`
  - 결과 파일 이름 구분용 라벨
- `TARGET_P95_MS`
  - 기본값: `500`
- `TARGET_P99_MS`
  - 기본값: `1000`
- `TARGET_ERROR_RATE`
  - 기본값: `0.01`

## payload 크기
`k6/lib/document_payloads.js` 기준으로 아래 4단계를 사용합니다.
- `small`: base text 1회
- `medium`: base text 2회
- `large`: base text 4회
- `xlarge`: base text 8회

`large`가 기본값이며, 실제 문제로 보고 있는 큰 문서 생성 payload를 가정한 값입니다.

## 실행 순서
### 1. smoke
연결/인증/응답 구조 검증

```bash
BASE_URL=https://dev-api.simsaimdang.shop \
ACCESS_TOKENS_FILE=k6/secrets/access_tokens.txt \
RUN_LABEL=smoke-local \
k6 run k6/scenarios/cpu/smoke.js
```

### 2. payload curve
payload 크기별 응답시간 증가 곡선 확인

```bash
BASE_URL=https://dev-api.simsaimdang.shop \
ACCESS_TOKENS_FILE=k6/secrets/access_tokens.txt \
CURVE_VUS=10 \
RUN_LABEL=payload-curve \
k6 run k6/scenarios/cpu/payload-curve.js
```

### 3. target
목표 150 VU 구간 확인

```bash
BASE_URL=https://dev-api.simsaimdang.shop \
ACCESS_TOKENS_FILE=k6/secrets/access_tokens.txt \
PAYLOAD_SIZE=large \
TARGET_P95_MS=500 \
TARGET_P99_MS=1000 \
RUN_LABEL=target-150vu \
k6 run k6/scenarios/cpu/target.js
```

### 4. stress
150 VU 초과 구간 한계점 탐색

```bash
BASE_URL=https://dev-api.simsaimdang.shop \
ACCESS_TOKENS_FILE=k6/secrets/access_tokens.txt \
PAYLOAD_SIZE=large \
RUN_LABEL=stress-300vu \
k6 run k6/scenarios/cpu/stress.js
```

## Prometheus/Grafana 연동 실행 예시
로컬에서 k6를 돌리더라도 메트릭은 서버 Prometheus로 보낼 수 있습니다.

```bash
BASE_URL=https://dev-api.simsaimdang.shop \
ACCESS_TOKENS_FILE=k6/secrets/access_tokens.txt \
RUN_LABEL=target-150vu \
K6_PROMETHEUS_RW_SERVER_URL=http://<monitoring-host>:9090/api/v1/write \
K6_PROMETHEUS_RW_TREND_STATS=p(95),p(99),avg,max \
K6_WEB_DASHBOARD=true \
K6_WEB_DASHBOARD_HOST=0.0.0.0 \
K6_WEB_DASHBOARD_PORT=5665 \
k6 run -o experimental-prometheus-rw k6/scenarios/cpu/target.js
```

## 결과 파일
각 시나리오는 실행 후 주제별 하위 디렉터리에 결과를 정리합니다.
- 문서 생성 예시
  - `k6/results/document-create/<test-name>/<scenario>/result.json`
  - `k6/results/document-create/<test-name>/<scenario>/result.txt`
- 외부 AI 예시
  - `k6/results/external-ai/<test-name>/<scenario>/result.json`
  - `k6/results/external-ai/<test-name>/<scenario>/result.txt`
- 상세 구조는 `k6/results/README.md`를 기준으로 맞춥니다.

## 해석 기준
- 앱 CPU 상승 + 응답시간 증가
  - CPU bound 가능성 높음
- `hikaricp_connections_pending` 증가 + 응답시간 증가
  - DB/커넥션 풀 병목 가능성
- `jvm_gc_pause_seconds_*` 급증 + swap 사용량 증가
  - 메모리 압박 가능성
- payload size가 커질수록 p95가 가파르게 증가
  - 문자열 파싱/대형 TEXT insert 영향 가능성

## 주의
- 현재 방식은 로컬에서 서버로 요청하므로 네트워크 지연이 일부 섞입니다.
- 따라서 절대 capacity 수치보다는 payload 크기별 상대 비교와 병목 위치 파악에 의미를 둡니다.
- dev 설정에서 SQL 로그가 켜져 있으면 결과가 왜곡되므로 테스트 전 비활성화가 권장됩니다.
