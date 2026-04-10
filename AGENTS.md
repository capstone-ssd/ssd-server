# SSD Multi-Module Agent Guide

## Project Context & Operations

### Project Context
- SSD는 사업계획서 문서를 생성/수정/분석하고 AI 기반 체크리스트, 요약, 평가를 제공하는 백엔드다.
- 멀티모듈 Gradle 구조이며 책임 분리는 다음과 같다.
  - `ssd-api`: Spring Boot 애플리케이션 엔트리포인트, REST API, 인증/인가 필터.
  - `ssd-domain`: 도메인 서비스, 엔티티, 도메인 리포지토리 인터페이스, AI/회원/문서 유스케이스.
  - `ssd-infra`: 도메인 리포지토리 인터페이스 구현체와 Spring Data JPA 어댑터.
  - `ssd-core`: 공통 설정, 예외/응답 규약, JWT 유틸, 전역 프로퍼티/유틸리티.

### Tech Stack
- Java 21, Gradle 8.x, Spring Boot 4.0.0
- Spring Security, Spring Data JPA, Spring Data Redis
- OpenFeign, Spring AI(OpenAI), Querydsl, PostgreSQL
- Swagger/OpenAPI (`springdoc-openapi`)

### Operational Commands
- 전체 빌드: `./gradlew clean build`
- 전체 테스트: `./gradlew test`
- 통합 테스트: `./gradlew integrationTest`
- 테스트 규칙 검증: `./gradlew verifyTestConventions`
- API 서버 실행(local): `./gradlew :ssd-api:bootRun --args='--spring.profiles.active=local'`
- API 서버 실행(dev): `./gradlew :ssd-api:bootRun --args='--spring.profiles.active=dev'`
- 모듈 단위 테스트
  - `./gradlew :ssd-api:test`
  - `./gradlew :ssd-domain:test`
  - `./gradlew :ssd-core:test`
  - `./gradlew :ssd-infra:test`
- 모듈 단위 통합 테스트
  - `./gradlew :ssd-api:integrationTest`
  - `./gradlew :ssd-domain:integrationTest`
  - `./gradlew :ssd-core:integrationTest`
  - `./gradlew :ssd-infra:integrationTest`
- 커버리지 리포트(API 모듈): `./gradlew :ssd-api:jacocoTestReport`
- CI 유사 검증: `./gradlew clean build jacocoTestReport -Dspring.profiles.active=test --no-daemon`
- Docker 이미지 빌드: `docker build -f Dockerfile -t <tag> .`

### Required Runtime Inputs
- 로컬 실행 시 환경변수/시크릿이 필요하다.
  - DB: `LOCAL_DB_URL`, `LOCAL_DB_USERNAME`, `LOCAL_DB_PASSWORD`
  - OAuth: `KAKAO_CLIENT_ID`
  - AI: `OPENAI_API_KEY` 및 프롬프트 관련 변수(`CHECK_SYS_PROMPT` 등)
- 시크릿은 코드/설정 파일에 하드코딩하지 않고 환경변수 또는 시크릿 스토어로 주입한다.

## Golden Rules

### Immutable
- 의존성 방향을 지킨다.
  - `ssd-api` -> `ssd-domain`/`ssd-core`/`ssd-infra`
  - `ssd-domain` -> `ssd-core`
  - `ssd-infra` -> `ssd-domain`/`ssd-core`
  - `ssd-core`는 다른 프로젝트 모듈에 의존하지 않는다.
- 웹 계층은 `ssd-api`만 담당한다. 컨트롤러/보안 필터를 다른 모듈에 추가하지 않는다.
- HTTP 요청/응답 DTO도 기본적으로 `ssd-api`에 둔다. `ssd-domain/.../controller/dto`는 레거시 위치로만 취급한다.
- 도메인 서비스는 도메인 리포지토리 인터페이스(`ssd-domain`)에만 의존하고, 구현체(`ssd-infra`)를 직접 참조하지 않는다.
- API 응답은 `ApiResponse` 포맷을 유지한다. 예외는 `ErrorCode` + `CustomException` 계열로 통일한다.
- 인증/인가 기본 정책은 `authenticated`이며, 화이트리스트는 명시적으로만 열어야 한다.

### Do
- 비즈니스 로직은 서비스 계층(`ssd-domain`)에 둔다.
- 트랜잭션 경계는 서비스 메서드에서 관리한다.
- 문서/폴더/회원 소유권 검증은 변경 작업 전에 수행한다.
- 신규 도메인 저장소가 필요하면
  - 인터페이스를 `ssd-domain`에 추가하고
  - 구현/JPA 어댑터를 `ssd-infra`에 추가한다.
- 신규 API는 Swagger 어노테이션과 입력 검증(`@Valid`)을 함께 제공한다.
- 신규 도메인 용어, 네이밍 정책, 금지 단어 변경 시 `docs/naming` 문서를 같은 PR에서 갱신한다.
- 규칙/아키텍처 변경 시 해당 모듈 `AGENTS.md`도 같은 PR에서 갱신한다.

### Don't
- 비밀번호, API 키, 토큰, DB 접속정보를 커밋하지 않는다.
- 컨트롤러에서 엔티티를 직접 반환하지 않는다.
- 도메인 서비스에서 `JpaRepository`를 직접 주입받지 않는다.
- 생성 코드(Querydsl 등)를 수동 수정하지 않는다.
- 모듈 경계를 우회하는 임시 참조(import)를 추가하지 않는다.

## Standards & References

### Code Standards
- 패키지 루트는 `or.hyu.ssd`를 유지한다.
- 클래스 네이밍
  - 컨트롤러: `*Controller`
  - 서비스: `*Service`
  - 도메인 리포지토리 인터페이스: `*Repository`
  - 인프라 구현체: `*RepositoryImpl`, `*JpaRepository`
  - 요청/응답 DTO: `*Request`, `*Response`
- null/권한 검증은 조기 반환보다 명시적 예외(`UserExceptionHandler` 등)로 처리한다.

### Naming References
- 네이밍 정본 문서: [`docs/naming/README.md`](./docs/naming/README.md)
- 도메인 용어 사전: [`docs/naming/glossary.md`](./docs/naming/glossary.md)
- 네이밍 패턴: [`docs/naming/patterns.md`](./docs/naming/patterns.md)
- 지양 단어: [`docs/naming/banned-words.md`](./docs/naming/banned-words.md)
- 용어 결정 이력: [`docs/naming/decision-log.md`](./docs/naming/decision-log.md)
- rename 사전 합의표: [`docs/naming/rename-mapping.md`](./docs/naming/rename-mapping.md)

### Architecture References
- DTO 경계 전략: [`docs/architecture/dto-boundary.md`](./docs/architecture/dto-boundary.md)
- 테스트 전략: [`docs/architecture/testing-strategy.md`](./docs/architecture/testing-strategy.md)

### Git & Collaboration
- 기본 브랜치 흐름: `feature/*`, `fix/*` -> `develop` -> `main`
- 브랜치 명명 규칙
  - 기능 작업: `feature/#이슈번호/요약`
  - 버그 수정: `fix/#이슈번호/요약`
  - 잡무/설정 작업: `chore/#이슈번호/요약`
- 커밋/이슈 prefix는 현재 템플릿 관례를 따른다.
  - `[FEATURE]`, `[FIX]`, `[CHORE]`, `[INIT]`
- 커밋 메세지는 아래와 같이 작성한다
  - git commit -m "`feat`:#`이슈번호` `커밋메세지`"
  - 커밋은 하나의 기능구현 단위로 나눈다
- PR에는 최소한 다음을 포함한다.
  - 관련 이슈
  - 변경 요약
  - 핵심 구현 상세

### Maintenance Policy
- 규칙과 코드가 충돌하면 코드를 우선 점검하고, 필요한 규칙 수정안을 즉시 제안한다.
- 신규 모듈/하위 컨텍스트가 생기면 해당 경로에 `AGENTS.md`를 추가하고 루트 Context Map을 업데이트한다.
- 모든 `AGENTS.md`는 500라인 미만으로 유지한다.

## Context Map (Action-Based Routing)
- **[API 엔드포인트/보안/실행 프로필 수정](./ssd-api/AGENTS.md)** — 컨트롤러, SecurityConfig, application 설정, API 계약 수정 시.
- **[도메인 서비스/엔티티/유스케이스 수정](./ssd-domain/AGENTS.md)** — 문서/회원/AI 비즈니스 로직, 트랜잭션, 도메인 규칙 수정 시.
- **[공통 설정/예외/프로퍼티 수정](./ssd-core/AGENTS.md)** — ErrorCode, ApiResponse, 공통 Config/JWT/유틸 수정 시.
- **[영속성 어댑터/JPA 구현 수정](./ssd-infra/AGENTS.md)** — RepositoryImpl, JpaRepository, 쿼리 성능/조회 로직 수정 시.
