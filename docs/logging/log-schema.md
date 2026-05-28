# SSD 운영 로그 스키마

## 1. 기본 원칙

| 원칙 | 내용 |
| --- | --- |
| 추적 기준 | 모든 애플리케이션 로그는 `requestId`를 기준으로 연결합니다. |
| 라벨 기준 | Loki label은 `env`, `service`, `logType`, `level`처럼 cardinality가 낮은 값만 사용합니다. |
| 검색 기준 | `requestId`, `memberId`, `documentId`, `folderId`는 JSON field로 남기고 LogQL `| json` 이후 검색합니다. |
| 민감정보 | 토큰, 프롬프트, 문서 본문, 개인 식별 가능 원문은 로그에 남기지 않습니다. |

## 2. 공통 필드

| 필드 | 타입 | 예시 | 설명 |
| --- | --- | --- | --- |
| `timestamp` | string | `2026-05-28T09:14:37+09:00` | 로그 발생 시각입니다. |
| `level` | string | `INFO`, `WARN`, `ERROR` | 로그 레벨입니다. |
| `service` | string | `ssd-api`, `nginx` | 로그를 생성한 서비스입니다. |
| `env` | string | `prod`, `dev`, `local` | 실행 환경입니다. |
| `logType` | string | `http_request`, `server_exception` | 로그 분류입니다. |
| `requestId` | string | `018f2d3c-...` | 요청 단위 추적 ID입니다. |
| `message` | string | `HTTP 요청 처리가 완료되었습니다.` | 사람이 읽는 로그 메시지입니다. |

## 3. HTTP 요청 로그

| 필드 | 타입 | 예시 | 설명 |
| --- | --- | --- | --- |
| `method` | string | `GET` | HTTP method입니다. |
| `uri` | string | `/api/v1/documents` | 요청 URI입니다. |
| `clientIp` | string | `218.235.241.148` | 클라이언트 IP입니다. |
| `userAgent` | string | `Mozilla/5.0` | User-Agent입니다. |
| `responseStatus` | number | `200` | 응답 상태 코드입니다. |
| `elapsedMs` | number | `153` | 요청 처리 시간입니다. |
| `memberId` | string | `1` | 인증 성공 시 회원 ID입니다. |

## 4. 서버 예외 로그

| 필드 | 타입 | 예시 | 설명 |
| --- | --- | --- | --- |
| `errorCode` | string | `SERVER50001` | SSD 공통 에러 코드입니다. |
| `responseStatus` | number | `500` | HTTP 응답 상태입니다. |
| `exceptionClass` | string | `DataIntegrityViolationException` | 예외 클래스명입니다. |
| `message` | string | `처리되지 않은 예외가 발생했습니다.` | 예외 요약 메시지입니다. |
| `requestId` | string | `018f2d3c-...` | Discord 알림과 Grafana 검색을 연결하는 키입니다. |

## 5. 외부 AI 호출 로그

| 필드 | 타입 | 예시 | 설명 |
| --- | --- | --- | --- |
| `aiEndpoint` | string | `/evaluate` | 외부 AI 호출 endpoint입니다. |
| `method` | string | `POST` | 외부 호출 method입니다. |
| `result` | string | `시작`, `성공`, `실패` | 외부 호출 결과입니다. |
| `externalStatus` | number | `200`, `502` | 외부 AI 응답 상태입니다. |
| `elapsedMs` | number | `1320` | 외부 호출 소요 시간입니다. |
| `memberId` | string | `1` | 요청 회원 ID입니다. |

## 6. 문서/폴더 작업 로그

| 필드 | 타입 | 예시 | 설명 |
| --- | --- | --- | --- |
| `action` | string | `문서 생성`, `폴더 이동` | 수행한 작업입니다. |
| `result` | string | `성공`, `실패` | 작업 결과입니다. |
| `documentId` | string | `20` | 작업 대상 문서 ID입니다. |
| `folderId` | string | `3` | 작업 대상 폴더 ID입니다. |
| `memberId` | string | `1` | 작업 수행 회원 ID입니다. |

## 7. 인증/권한 로그

| 필드 | 타입 | 예시 | 설명 |
| --- | --- | --- | --- |
| `reason` | string | `JWT 토큰이 유효하지 않습니다.` | 인증/인가 실패 이유입니다. |
| `responseStatus` | number | `401`, `403` | 응답 상태입니다. |
| `method` | string | `GET` | 요청 method입니다. |
| `uri` | string | `/api/v1/documents` | 요청 URI입니다. |
| `clientIp` | string | `218.235.241.148` | 요청 IP입니다. |

## 8. Nginx 로그

| 필드 | 타입 | 예시 | 설명 |
| --- | --- | --- | --- |
| `requestId` | string | `018f2d3c-...` | Nginx와 Spring 로그를 연결하는 ID입니다. |
| `remoteAddr` | string | `218.235.241.148` | 클라이언트 IP입니다. |
| `method` | string | `POST` | HTTP method입니다. |
| `uri` | string | `/api/v1/documents` | 요청 URI입니다. |
| `status` | number | `200`, `500` | Nginx 응답 상태입니다. |
| `upstreamStatus` | string | `200` | upstream 서버 응답 상태입니다. |
| `requestTime` | number | `0.153` | Nginx 기준 요청 처리 시간입니다. |
| `upstreamResponseTime` | string | `0.151` | upstream 응답 시간입니다. |

## 9. 대표 LogQL

| 목적 | LogQL |
| --- | --- |
| requestId 추적 | `{env="prod"} | json | requestId = "<requestId>"` |
| 서버 예외 조회 | `{env="prod", service="ssd-api"} | json | logType = "server_exception"` |
| 외부 AI 실패 조회 | `{env="prod", service="ssd-api"} | json | logType = "external_ai" | result = "실패"` |
| 문서 작업 조회 | `{env="prod", service="ssd-api"} | json | logType = "document_crud"` |
| 인증/권한 실패 조회 | `{env="prod", service="ssd-api"} | json | logType = "auth"` |
| Nginx 5xx 조회 | `{env="prod", service="nginx", log_file="access"} | json | status >= 500` |

## 10. 변경 규칙

| 상황 | 규칙 |
| --- | --- |
| 새 로그 타입 추가 | `logType` 값을 먼저 문서에 추가한 뒤 구현합니다. |
| 새 필드 추가 | 필드명, 타입, 예시, 설명을 이 문서에 추가합니다. |
| 필드명 변경 | 기존 Grafana 대시보드와 LogQL 영향을 먼저 확인합니다. |
| 민감정보 가능성 | 로그 추가 전 토큰, 본문, 프롬프트 포함 여부를 확인합니다. |
