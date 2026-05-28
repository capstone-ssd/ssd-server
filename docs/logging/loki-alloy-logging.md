# SSD 로그 수집 파이프라인

## 1. 목표

| 목표 | 내용 |
| --- | --- |
| 장애 추적 | Discord 예외 알림의 `requestId`로 Grafana Loki 로그를 역추적합니다. |
| 로그 상관관계 | Nginx access log와 Spring Boot application log를 같은 `requestId`로 연결합니다. |
| 운영 로그 분류 | 서버 예외, 외부 AI 호출, 문서/폴더 작업, Nginx, 인증/권한 로그를 분리해서 조회합니다. |

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
| `external_ai` | 외부 AI 호출 시작/성공/실패 로그 |
| `document_crud` | 문서/폴더 생성, 조회, 수정, 삭제, 이동 로그 |
| `auth` | 인증 실패, 인가 실패 로그 |

## 5. Grafana 조회 예시

| 목적 | LogQL |
| --- | --- |
| requestId 기반 추적 | `{env="prod"} | json | requestId = "<requestId>"` |
| 서버 예외만 조회 | `{env="prod", service="ssd-api"} | json | logType = "server_exception"` |
| 외부 AI 실패 조회 | `{env="prod", service="ssd-api"} | json | logType = "external_ai" | result = "실패"` |
| 인증/인가 실패 조회 | `{env="prod", service="ssd-api"} | json | logType = "auth"` |
| Nginx 5xx 조회 | `{env="prod", service="nginx", log_file="access"} | json | status >= 500` |

## 6. 운영 주의사항

| 항목 | 기준 |
| --- | --- |
| 개인정보 | 본문, 프롬프트, 토큰, 문서 내용은 로그에 남기지 않습니다. |
| Loki label | `requestId`는 cardinality가 높으므로 label이 아니라 JSON field로 유지합니다. |
| 보관 기간 | Loki retention은 168시간으로 제한합니다. |
| 알림 연계 | Discord 5xx 알림에는 `requestId`를 포함합니다. |
