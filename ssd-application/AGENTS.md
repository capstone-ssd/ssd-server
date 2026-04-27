# SSD Application Module Guide

## Module Context
- 이 모듈은 API 계층과 도메인 유스케이스 사이의 애플리케이션 facade를 담당한다.
- 주요 책임
  - 트랜잭션 경계 관리
  - 읽기/쓰기 facade 분리
  - 도메인 유스케이스 호출 조합
- 비즈니스 규칙은 `ssd-domain`에 두고, 이 모듈은 실행 경계만 관리한다.

## Tech Stack & Constraints
- Spring Context, Spring TX
- 제약
  - HTTP 요청/응답 DTO를 정의하지 않는다.
  - JPA 엔티티와 `JpaRepository`를 직접 참조하지 않는다.
  - 외부 client 구현체를 직접 호출하지 않는다.
  - facade는 도메인 유스케이스와 도메인 command/result만 사용한다.

## Implementation Patterns
- 쓰기 facade 메서드는 `@Transactional`을 적용한다.
- 조회 facade 메서드는 `@Transactional(readOnly = true)`를 적용한다.
- facade가 비대해지면 command/query 또는 컨텍스트 기준으로 분리한다.
- facade 안에는 트랜잭션 경계 외의 비즈니스 분기를 누적하지 않는다.

## Testing Strategy
- 모듈 테스트 실행: `./gradlew :ssd-application:test`
- 변경 시 최소 검증 항목
  - 트랜잭션 readOnly 여부가 유스케이스 성격과 맞는지 확인한다.
  - API 컨트롤러가 domain service를 직접 호출하지 않는지 확인한다.
