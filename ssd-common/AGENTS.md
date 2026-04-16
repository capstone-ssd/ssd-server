# SSD Common Module Guide

## Module Context
- 이 모듈은 전역 공통 규약과 재사용 컴포넌트를 제공한다.
- 주요 책임
  - 공통 응답/예외 모델(`ApiResponse`, `ErrorCode`, `GlobalExceptionHandler`)
  - 공통 설정/프로퍼티
  - 범용 유틸리티
- 다른 모듈에서 재사용되므로 변경 시 파급 범위가 크다.

## Tech Stack & Constraints
- Spring Boot 기반 공통 라이브러리 모듈(`java-library`)
- Web, 설정 바인딩, 공통 예외 규약
- 제약
  - `ssd-common`은 프로젝트의 다른 모듈에 의존하지 않는다.
  - 비즈니스 도메인 지식(문서/회원 정책)을 담지 않는다.
  - 전역 계약(`ApiResponse`, `ErrorCode`) 변경 시 API/도메인/인프라 영향도를 검토한다.

## Naming Reference
- 공통 유틸, 설정, 예외 이름은 `/Users/jeonjaeyeon/Desktop/capstone/ssd/docs/naming/patterns.md`를 따른다.
- 모호한 공통 클래스명은 `banned-words.md`를 기준으로 피한다.

## Implementation Patterns

### Exception and Response Pattern
- 사용자 예외는 `CustomException` 계층으로 추상화한다.
- 실제 예외는 도메인을 이용하여 `CustomException` 을 상속한 `Handler`를 구현하여 사용한다.
- 전역 예외 핸들러에서 HTTP 상태와 `ErrorCode` 매핑을 유지한다.
- 성공 응답은 `ApiResponse.ok(...)`, 실패 응답은 `ApiResponse.fail(...)` 규약을 따른다.

### Configuration Pattern
- 환경 설정 키는 공통 property 패키지 아래 클래스로 바인딩한다.
- 보안/외부연동 설정은 Config 클래스에서 명시적으로 주입한다.
- 설정 추가 시 필수값 검증과 기본값 전략을 함께 정의한다.

### Utility Pattern
- 유틸 클래스는 상태를 가지지 않도록 설계한다.
- JWT/쿠키/AI 응답 파싱 유틸 변경 시 호출처 시그니처 호환성을 유지한다.

## Testing Strategy
- 모듈 테스트 실행: `./gradlew :ssd-common:test`
- 권장 테스트 범위
  - ErrorCode와 HTTP 상태 매핑 검증
  - ConfigurationProperties 바인딩 검증
  - 공통 유틸과 공통 persistence 지원 클래스 검증
- 변경 시 최소 검증 항목
  - 공통 응답 스키마 역호환성
  - 예외 핸들링 누락 여부
  - Redis/Feign 설정 로딩 실패 여부

## Local Golden Rules
- Do
  - 공통 계약 변경 시 영향을 받는 모듈(`ssd-api`, `ssd-domain`, `ssd-auth`, `ssd-external`, `ssd-infra`)을 같이 점검한다.
  - 에러 코드 추가 시 코드/메시지/HTTP 상태를 한 세트로 관리한다.
  - 프로퍼티 키 변경 시 application 설정과 동기화한다.
- Don't
  - 도메인 특화 로직을 공통 모듈에 넣지 않는다.
  - 공통 유틸에 부작용(전역 상태 변경)을 추가하지 않는다.
  - 임시 하드코딩 값으로 설정 주입 문제를 우회하지 않는다.
