# DTO Boundary Strategy

## 목적
- SSD에서 DTO를 어떤 책임으로 나누고, 어떤 모듈/패키지에 둘지 고정한다.
- 현재 `ssd-domain/.../controller/dto`에 섞여 있는 웹 계약 DTO, 외부 연동 payload, 내부 보조 모델을 분리할 기준을 만든다.
- 실제 파일 이동은 후속 이슈에서 단계적으로 진행하고, 이 문서는 그 기준 문서로 사용한다.

## 이 문서가 다루는 것
- HTTP 요청/응답 DTO
- 도메인 유스케이스 입력/출력 모델
- 외부 시스템 연동 payload
- 서비스/지원 계층 내부 보조 모델

## 이 문서가 다루지 않는 것
- 실제 파일 이동
- 공개 API 스키마 변경
- 대규모 rename

## 핵심 원칙
1. HTTP 계약 DTO는 `ssd-api`에 둔다.
2. 도메인 유스케이스 입력/출력 모델은 `ssd-domain`에 두되, 웹 의미의 `Request`/`Response`로 부르지 않는다.
3. 외부 API 연동 payload는 해당 클라이언트 옆에 둔다.
4. 한 서비스에서만 쓰이는 보조 모델은 전역 `dto` 패키지로 빼지 않는다.
5. 기존 `ssd-domain/.../controller/dto`는 레거시 위치로 간주하고, 새 파일을 추가하지 않는다.

## DTO 분류 기준
| 분류 | 책임 | 목표 모듈 | 목표 패키지 | 네이밍 기준 |
| --- | --- | --- | --- | --- |
| API 계약 DTO | HTTP 요청 본문, 응답 JSON, multipart 메타데이터 | `ssd-api` | `or.hyu.ssd.domain.<context>.controller.dto` | `*Request`, `*Response`, `*ListItemResponse`, `*DetailResponse` |
| 도메인 유스케이스 모델 | 서비스 입력/출력, 검색 조건, 내부 호출 계약 | `ssd-domain` | `or.hyu.ssd.domain.<context>.usecase.command`, `...usecase.result`, 필요 시 `...usecase.query` | `*Command`, `*Result`, `*Criteria` |
| 외부 연동 payload | Kakao, External AI 등 외부 시스템 요청/응답 스키마 | `ssd-domain` | `or.hyu.ssd.domain.<context>.client.dto`, provider 세분화가 필요하면 `...client.dto.<provider>` | provider 계약에 맞추되 `controller/dto` 금지 |
| 내부 보조 모델 | 한 서비스/서포트 컴포넌트 내부 전달용 구조 | 소비 모듈과 가장 가까운 위치 | `...service.support`, `...controller.support`, 중첩 record/class 우선 | 필요 최소한의 이름, 전역 `dto` 패키지 금지 |

## 목표 패키지 전략

### 1. API 계약 DTO
- 위치:
  - `ssd-api/src/main/java/or/hyu/ssd/domain/<context>/controller/dto`
- 대상:
  - 컨트롤러 입력 `@RequestBody`, `@RequestPart`, `@PathVariable` 조합용 모델
  - 컨트롤러가 직접 반환하는 JSON 응답 모델
- 예시:
  - `CreateDocumentRequest`
  - `GetDocumentResponse`
  - `EvaluatorReviewCreateRequest`
  - `ExternalAiSummaryResponse`
  - `GetMyMemberResponse`

### 2. 도메인 유스케이스 모델
- 위치:
  - `ssd-domain/src/main/java/or/hyu/ssd/domain/<context>/usecase/command`
  - `ssd-domain/src/main/java/or/hyu/ssd/domain/<context>/usecase/result`
  - 검색/필터 전용은 `.../usecase/query`
- 대상:
  - 웹이 아닌 서비스 호출 전용 입력/출력 구조
  - 컨트롤러와 서비스 사이에서만 쓰이는 비즈니스 의미 모델
- 규칙:
  - `Request`/`Response` 대신 `Command`/`Result`를 사용한다.
  - 서비스 내부에만 머무르면 DTO 패키지보다 중첩 record나 `service.support`가 더 우선이다.

### 3. 외부 연동 payload
- 위치:
  - `ssd-domain/src/main/java/or/hyu/ssd/domain/<context>/client/dto`
  - provider 별 분리가 필요하면 `.../client/dto/kakao`, `.../client/dto/externalai`
- 대상:
  - Feign client 요청/응답 모델
  - 외부 시스템 응답 역직렬화 클래스
- 예시:
  - `KaKaoOAuthTokenDTO`
  - `KaKaoUserInfoResponse`
  - `ExternalEvaluationRequest`
  - `ExternalEvaluationResponse`
  - `ExternalCheckNewTextRequest`

### 4. 내부 보조 모델
- 위치:
  - 한 클래스 내부에서만 쓰이면 중첩 `record`/`class`
  - 여러 서비스 지원 컴포넌트가 공유하면 `.../service/support`
- 대상:
  - 업로드 파트 묶음
  - 블록 해석 결과
  - 임시 정렬/변환 구조
- 예시:
  - `DocumentImageUploadPart`
  - `ResolvedDocumentBlock`

## 현재 코드 분류 예시

### A. `ssd-api`로 가야 하는 웹 계약 DTO
- `CreateDocumentRequest`
- `UpdateDocumentRequest`
- `CreateDocumentBlockRequest`
- `CreateFolderRequest`
- `UpdateFolderRequest`
- `DocumentImageMetaRequest`
- `ExternalDocumentIdRequest`
- `GetDocumentResponse`
- `DocumentListItemResponse`
- `DocumentCommentResponse`
- `EvaluatorReviewDetailResponse`
- `ExternalAiSummaryResponse`
- `GetMyMemberResponse`

> `#139`에서 Document 핵심 CRUD DTO(`CreateDocumentRequest`, `UpdateDocumentRequest`, `CreateDocumentBlockRequest`, `GetDocumentResponse`, `DocumentImageMetaRequest`)는 `ssd-api`로 이동했다.
>
> `#140`에서 Document 목록/북마크, Folder, Comment, Log, EvaluatorReview, External AI 요청/응답 DTO도 같은 기준으로 정리했다.

### B. `ssd-domain/.../client/dto`로 가야 하는 외부 연동 payload
- `ExternalEvaluationRequest`
- `ExternalEvaluationResponse`
- `ExternalEvaluationReportResponse`
- `ExternalEvaluatorMetricResponse`
- `ExternalSummarizationBasicRequest`
- `ExternalSummarizationBasicResponse`
- `ExternalSummarizationKeywordRequest`
- `ExternalSummarizationKeywordResponse`
- `ExternalCheckNewTextRequest`
- `ExternalCheckNewTextBlockRequest`
- `ExternalCheckNewTextResponse`
- `KaKaoOAuthTokenDTO`
- `KaKaoUserInfoResponse`
- `KaKaoCallbackResponse`

> `#140`에서 External AI payload는 `ssd-domain/.../client/dto`로 이동했다.

### C. 전역 `controller/dto` 대신 가까운 위치로 내려야 하는 내부 보조 모델
- `DocumentImageUploadPart`
- 후속 리팩터링에서의 `ResolvedDocumentBlock` 같은 지원 구조

## 마이그레이션 규칙
1. 새 DTO를 추가할 때는 `ssd-domain/.../controller/dto`에 넣지 않는다.
2. 새 API 요청/응답 DTO는 `ssd-api`에 만든다.
3. 새 외부 연동 payload는 해당 `client` 패키지 옆에 만든다.
4. 서비스 시그니처에 새 모델이 필요하면 `Command`/`Result`를 우선 검토한다.
5. 기존 레거시 DTO는 후속 이슈에서 단계적으로 이동한다.

## 후속 이슈 연결
- `#139`, `#140`
  - Document/Folder/Review/External AI 관련 DTO를 분류 기준에 맞게 이동
- `#141`, `#142`
  - `DocumentService`에서 레거시 DTO 의존을 줄이고 내부 지원 모델을 정리
- `#143`
  - Member/Kakao DTO를 웹 DTO와 외부 client payload로 분리
- `#144`
  - JWT/Auth 쪽에서 API 계약 DTO와 도메인 모델 경계 재점검

## 현재 남은 레거시 DTO
- `ssd-domain/.../controller/dto` 아래에서 아직 남아 있는 것은 Member/Kakao 관련 DTO뿐이다.
- Document 컨텍스트의 신규 DTO 추가는 모두 이 문서의 경계 기준을 따라야 한다.
