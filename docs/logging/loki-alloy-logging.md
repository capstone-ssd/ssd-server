# SSD 로그 수집 파이프라인

## 1. 목표

| 목표 | 내용 |
| --- | --- |
| 장애 추적 | Discord 예외 알림의 `requestId`로 Grafana Loki 로그를 역추적합니다. |
| 로그 상관관계 | Nginx access log와 Spring Boot application log를 같은 `requestId`로 연결합니다. |
| 운영 로그 분류 | 서버 예외, 외부 AI 호출, 문서/폴더 작업, Nginx, 인증/권한 로그를 분리해서 조회합니다. |
| 운영 지표화 | 로그를 기반으로 5xx 비율, 외부 AI 실패율, P95 지연시간을 대시보드에서 확인합니다. |

## 2. 전체 구조

~~~mermaid
flowchart LR
    C["Client"] --> N["Nginx"]
    N -->|"X-Request-Id 전달"| A["SSD API"]
    A -->|"JSON stdout"| D["Docker logs"]
    N -->|"JSON file log"| F["/var/log/nginx"]
    D --> AL["Grafana Alloy"]
    F --> AL
    AL --> L["Loki"]
    L --> G["Grafana"]
    A -->|"5xx + requestId"| DIS["Discord"]
~~~

## 3. MDC 필드

| 필드 | 설명 |
| --- | --- |
| `requestId` | 요청 단위 추적 ID입니다. `X-Request-Id`가 있으면 재사용하고, 없으면 서버에서 생성합니다. |
| `memberId` | 인증 성공 후 회원 ID를 기록합니다. |
| `method` | HTTP method입니다. |
| `uri` | 요청 URI입니다. |
| `clientIp` | `X-Forwarded-For` 또는 remote address입니다. |
| `userAgent` | User-Agent입니다. |
| `logType` | 로그 분류값입니다. |
| `documentId` | 문서 작업 대상 ID입니다. |
| `folderId` | 폴더 작업 대상 ID입니다. |
| `aiEndpoint` | 외부 AI 호출 endpoint입니다. |
| `elapsedMs` | 요청 또는 외부 호출 처리 시간입니다. |
| `responseStatus` | 응답 상태 코드입니다. |

## 4. 로그 타입

| `logType` | 대상 |
| --- | --- |
| `http_request` | 모든 HTTP 요청 완료 로그 |
| `server_exception` | 서버 예외 로그 |
| `client_exception` | 클라이언트 요청 오류 로그 |
| `external_ai` | 외부 AI 호출 시작/성공/실패 로그 |
| `document_crud` | 문서/폴더 생성, 조회, 수정, 삭제, 이동 로그 |
| `auth` | 인증 실패, 인가 실패 로그 |

## 5. Grafana 조회 예시

| 목적 | LogQL |
| --- | --- |
| requestId 기반 추적 | `{env="prod"} \| json \| requestId = "<requestId>"` |
| 서버 예외만 조회 | `{env="prod", service="ssd-api"} \| json \| logType = "server_exception"` |
| 외부 AI 실패 조회 | `{env="prod", service="ssd-api"} \| json \| logType = "external_ai" \| result = "실패"` |
| 인증/인가 실패 조회 | `{env="prod", service="ssd-api"} \| json \| logType = "auth"` |
| Nginx 5xx 조회 | `{env="prod", service="nginx", log_file="access"} \| json \| status >= 500` |

## 6. 대시보드 지표

| 패널 | 기준 |
| --- | --- |
| 서버 5xx 비율 | 전체 애플리케이션 요청 중 `responseStatus >= 500` 비율입니다. |
| 외부 AI 실패율 | 외부 AI 완료 로그 중 `result = "실패"` 비율입니다. |
| 요청 P95 | HTTP 요청 완료 로그의 `elapsedMs` 기준 P95입니다. |
| 외부 AI P95 | 외부 AI 완료 로그의 `elapsedMs` 기준 P95입니다. |
| Nginx 5xx 비율 | Nginx access log 중 `status >= 500` 비율입니다. |
| 느린 외부 AI | `elapsedMs >= 3000`인 외부 AI 호출 수입니다. |

## 7. 운영 주의사항

| 항목 | 기준 |
| --- | --- |
| 개인정보 | 본문, 프롬프트, 토큰, 문서 내용은 로그에 남기지 않습니다. |
| Loki label | `requestId`는 cardinality가 높으므로 label이 아니라 JSON field로 유지합니다. |
| 보관 기간 | Loki retention은 48시간으로 제한합니다. |
| 알림 연계 | Discord 5xx 알림에는 `requestId`와 Grafana 로그 추적 링크를 포함합니다. |
| 스키마 관리 | 로그 필드 변경 시 `docs/logging/log-schema.md`를 함께 갱신합니다. |

## 8. 환경변수

| 환경변수 | 설명 |
| --- | --- |
| `GRAFANA_BASE_URL` | Discord 예외 알림에 포함할 Grafana 기본 URL입니다. 예: `http://localhost:3001` |
| `GRAFANA_LOGGING_DASHBOARD_UID` | 로그 대시보드 UID입니다. 기본값은 `ssd-logging-overview`입니다. |
