# SSD Domain Module Guide

## Module Context
- 이 모듈은 핵심 비즈니스 로직과 도메인 모델을 담당한다.
- 주요 책임
  - 유스케이스 서비스(`*Service`)
  - 도메인 엔티티/값 규칙
  - 도메인 리포지토리 인터페이스 정의
  - AI 프롬프트 조합 및 외부 호출 추상화
- `ssd-api`는 이 모듈의 서비스/DTO를 호출하고, `ssd-infra`는 저장소 인터페이스를 구현한다.

## Tech Stack & Constraints
- Spring Boot Starter, Spring Data JPA, Validation, Security, OAuth2 Client
- OpenFeign, Spring AI(OpenAI), Querydsl APT
- 제약
  - 도메인 서비스는 인프라 구현체를 직접 참조하지 않는다.
  - 저장소는 인터페이스로만 선언하고 구현은 `ssd-infra`에 둔다.
  - 트랜잭션 경계는 서비스 계층에 둔다.
  - 엔티티의 상태 변경은 의도된 메서드(`update*`, `of`)를 통해 수행한다.

## Implementation Patterns

### Service Pattern
- 권한/소유권 검증을 가장 먼저 수행한다.
- 예외는 `ErrorCode` + `UserExceptionHandler`로 통일한다.
- 읽기 전용 조회는 `@Transactional(readOnly = true)`를 우선 적용한다.
- 동시성 충돌 가능 작업은 낙관적 락/재시도 유틸을 사용한다.

### Domain Repository Pattern
- 인터페이스 위치: `ssd-domain/src/main/java/.../repository`
- 저장소 인터페이스는 도메인 요구사항 중심 메서드만 노출한다.
- JPA 세부 사항(`JpaRepository`, 쿼리 어노테이션)은 도메인 계층에 두지 않는다.

### Entity and DTO Pattern
- 엔티티는 생성 팩토리(`of`)와 의미 있는 업데이트 메서드를 유지한다.
- 요청/응답 DTO는 현재 구조(`domain/.../controller/dto`)를 유지하되, API 직렬화 계약 변경 시 하위호환 영향을 점검한다.
- Querydsl 생성 파일은 생성 산출물로 간주하고 수동 수정하지 않는다.

## Testing Strategy
- 모듈 테스트 실행: `./gradlew :ssd-domain:test`
- 권장 테스트 범위
  - 서비스 단위 테스트: 권한 체크, 예외 매핑, 분기 처리
  - 영속성 연계 테스트: 엔티티 관계/삭제 전파/정렬 조회 검증
  - AI 응답 파싱 테스트: JSON/Markdown 포맷 강건성 검증
- 변경 시 최소 검증 항목
  - `DOCUMENT_FORBIDDEN` 등 권한 오류가 정확히 발생하는지
  - 폴더/문서 연관 삭제와 로그 저장이 누락되지 않는지
  - 동시 수정 시 충돌 처리 정책이 깨지지 않는지

## Local Golden Rules
- Do
  - 서비스 메서드에서 사용자/리소스 권한 검증을 일관되게 유지한다.
  - 상태 변경이 있는 엔티티는 메서드 단위로 캡슐화한다.
  - 새 도메인 기능 추가 시 인터페이스-구현 분리 원칙을 먼저 반영한다.
- Don't
  - `@RestController`, `ResponseEntity` 같은 웹 계층 요소를 추가하지 않는다.
  - 외부 라이브러리 응답 모델을 엔티티에 직접 바인딩하지 않는다.
  - 시크릿/토큰/외부 API 키를 코드에 하드코딩하지 않는다.
