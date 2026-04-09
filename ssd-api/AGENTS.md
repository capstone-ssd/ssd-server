# SSD API Module Guide

## Module Context
- 이 모듈은 애플리케이션 진입점(`SsdApplication`)과 HTTP API 계층을 담당한다.
- 주요 책임
  - REST 컨트롤러 제공
  - 인증/인가 필터 체인 구성
  - 요청 입력 검증 및 응답 포맷 고정
  - 도메인 서비스 호출 오케스트레이션
- 비즈니스 계산/정책 결정은 `ssd-domain`에 위임한다.

## Tech Stack & Constraints
- Spring Boot Web, Spring Security, OpenFeign, Springdoc OpenAPI, JWT
- 설정 파일은 `ssd-api/src/main/resources` 기준으로 관리한다.
- 실행 프로필
  - `local`: 환경변수 기반 로컬 개발
  - `dev`: 배포/개발 서버 설정
- 제약
  - 컨트롤러는 `ResponseEntity<ApiResponse<...>>`를 반환한다.
  - 컨트롤러에서 엔티티를 직접 노출하지 않는다.
  - 보안 예외/일반 예외 처리는 전역 핸들러(`ssd-core`) 규약을 따른다.

## Naming Reference
- 공통 네이밍 기준은 `docs/naming/README.md`를 따른다.
- API 요청/응답 DTO 이름과 도메인 용어가 충돌하면 `glossary.md`를 먼저 확인한다.
- DTO 경계 원칙은 `docs/architecture/dto-boundary.md`를 따른다.

## Implementation Patterns

### Endpoint Pattern
- 경로는 `/api/v1/...`를 기본으로 유지한다.
- 인증 사용자 정보는 `@AuthenticationPrincipal CustomUserDetails`로 받는다.
- 입력은 `@RequestBody` + `@Valid`를 사용한다.
- Swagger 문서화
  - `@Tag`, `@Operation`, `@Parameter`를 사용해 API 스펙을 유지한다.

### Controller Rules
- 컨트롤러에서는 아래 작업만 수행한다.
  - 요청 파싱/검증
  - 인증 정보 주입
  - 도메인 서비스 호출
  - `ApiResponse.ok(...)`로 래핑
- API 요청/응답 DTO는 이 모듈의 `controller/dto`에 둔다.
- 컨트롤러는 API DTO를 그대로 도메인에 누수시키지 말고, 필요하면 도메인 command/result로 경계에서 변환한다.
- 금지
  - 트랜잭션/비즈니스 분기 직접 구현
  - DB 접근 코드 작성
  - 도메인 예외코드 우회 처리
  - 외부 시스템 client payload를 API 응답 DTO처럼 재사용

### Security Rules
- `SecurityConfig`의 기본 정책은 `anyRequest().authenticated()`를 유지한다.
- 신규 공개 엔드포인트는 `WhiteListConfig`와 보안 설정을 함께 갱신한다.
- JWT 필터 순서는 `UsernamePasswordAuthenticationFilter` 이전에 유지한다.
- CORS 변경 시 허용 Origin과 노출 헤더를 최소 권한 원칙으로 유지한다.

## Testing Strategy
- 모듈 테스트 실행: `./gradlew :ssd-api:test`
- 권장 테스트 계층
  - 컨트롤러 슬라이스 테스트: 인증/인가/응답코드/검증 실패 케이스
  - 통합 테스트: 실제 요청-응답 포맷과 예외 매핑 검증
- 변경 시 최소 검증 항목
  - 인증 필요 엔드포인트 접근 통제
  - `ApiResponse` 코드/메시지 구조
  - JSON 파싱 실패 및 잘못된 메서드 호출 시 ErrorCode 매핑

## Local Golden Rules
- Do
  - 도메인 DTO/서비스를 이용해 응답을 구성한다.
  - 실패 응답은 `ErrorCode` 기반으로 일관되게 처리되도록 예외를 던진다.
  - application 설정 키를 추가할 때 `ssd-core`의 프로퍼티 클래스와 정합성을 확인한다.
- Don't
  - `application*.yml`에 실제 시크릿 값을 커밋하지 않는다.
  - 인증이 필요한 기능을 임시로 `permitAll` 처리한 상태로 머지하지 않는다.
  - 컨트롤러에 비즈니스 로직을 누적하지 않는다.
