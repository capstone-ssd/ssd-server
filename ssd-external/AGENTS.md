# SSD External Module Guide

## Module Context
- 이 모듈은 AI, 스토리지, 알림 등 외부 시스템 연동 클라이언트를 담당한다.
- 주요 책임
  - 외부 HTTP client
  - S3/알림 연동 보조 컴포넌트
  - 외부 통신 DTO와 설정
- 도메인 포트 구현체는 필요 시 `ssd-infra` 어댑터에서 조합한다.

## Constraints
- `ssd-external`은 `ssd-common`에만 의존한다.
- 비즈니스 정책이나 영속성 구현을 포함하지 않는다.

## Testing
- 모듈 테스트 실행: `./gradlew :ssd-external:test`
