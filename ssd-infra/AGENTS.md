# SSD Infra Module Guide

## Module Context
- 이 모듈은 도메인 저장소 인터페이스를 실제 영속성 기술로 구현하는 어댑터 계층이다.
- 주요 책임
  - `ssd-domain` 저장소 인터페이스 구현(`*RepositoryImpl`)
  - Spring Data JPA 리포지토리(`*JpaRepository`) 제공
  - 조회/정렬/필터링 쿼리 구현
- 비즈니스 정책 판단은 `ssd-domain` 서비스에서 수행하고, infra는 데이터 접근에 집중한다.

## Tech Stack & Constraints
- Spring Data JPA 기반 `java-library` 모듈
- `ssd-domain` 엔티티를 직접 사용하며 별도 ORM 엔티티를 분리하지 않는다.
- 제약
  - 구현 클래스는 반드시 도메인 저장소 인터페이스를 구현한다.
  - 인프라 계층에서 `ResponseEntity`, 컨트롤러, 보안 필터를 추가하지 않는다.
  - 권한/인증 판단 로직은 도메인 계층으로 유지한다.

## Naming Reference
- 저장소/어댑터 네이밍 규칙은 `/Users/jeonjaeyeon/Desktop/capstone/ssd/docs/naming/patterns.md`를 따른다.
- 도메인 용어와 충돌하는 조회 메서드명은 `glossary.md` 기준으로 정리한다.

## Implementation Patterns

### Repository Adapter Pattern
- 패키지 구조
  - `infra/.../repository/*RepositoryImpl`: 도메인 저장소 구현체
  - `infra/.../repository/jpa/*JpaRepository`: Spring Data 인터페이스
- 구현체는 JPA 리포지토리에 위임하고, 도메인 인터페이스 시그니처를 그대로 맞춘다.
- 낙관적 락/강제 flush가 필요한 케이스는 도메인 서비스 계약에 맞춰 메서드를 제공한다.

### Query Pattern
- 단순 조회는 메서드 네이밍 쿼리를 우선한다.
- 복잡 조회/동적 조건이 필요하면 Querydsl 또는 명시적 쿼리로 확장한다.
- 정렬/페이징 요구사항이 있는 경우 도메인 계약에 `Sort`/`Pageable`을 노출하고 구현체에서 정확히 반영한다.
- 쿼리 메서드를 구현할때는 반드시 N+1을 유의하여 구현해야한다

## Testing Strategy
- 모듈 테스트 실행: `./gradlew :ssd-infra:test`
- 권장 테스트 범위
  - `@DataJpaTest` 기반 리포지토리 동작 검증
  - 정렬/조건 조회 시 반환 순서 및 null 처리 검증
  - 삭제/flush 동작과 연관 데이터 영향 검증
- 변경 시 최소 검증 항목
  - 도메인 인터페이스 계약 불일치 여부
  - 잘못된 조인/지연로딩으로 인한 N+1 리스크
  - 트랜잭션 경계 밖 LazyInitialization 예외 가능성

## Local Golden Rules
- Do
  - 저장소 구현체는 단순하고 예측 가능하게 유지한다.
  - 쿼리 변경 시 실제 사용 서비스 시나리오(목록/상세/삭제)를 함께 검증한다.
  - 인덱스가 필요한 조회 패턴은 DB 관점에서 명시적으로 기록한다.
- Don't
  - 인프라 계층에서 도메인 정책 예외를 임의로 변환하지 않는다.
  - 서비스 계층 책임(권한 검증, 유스케이스 분기)을 인프라로 끌어오지 않는다.
  - 테스트 없이 쿼리 시그니처를 바꾸지 않는다.
