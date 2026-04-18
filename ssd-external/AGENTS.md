# SSD External Module Guide

## Module Context
- 이 모듈은 AI, 스토리지, 알림 등 외부 시스템 연동 클라이언트를 담당한다.
- 주요 책임
  - `external.ai.client`: 외부 AI HTTP client
  - `external.config`: Feign, S3, 외부 연동 프로퍼티/설정
  - `external.storage.s3.*`: S3 보조 컴포넌트
  - `external.alert.discord`: Discord 알림 연동
- 도메인 포트 구현체는 필요 시 `ssd-infra` 어댑터에서 조합한다.

## Constraints
- `ssd-external`은 `ssd-common`에만 의존한다.
- 비즈니스 정책이나 영속성 구현을 포함하지 않는다.

## Testing
- 모듈 테스트 실행: `./gradlew :ssd-external:test`
