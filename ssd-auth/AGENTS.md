# SSD Auth Module Guide

## Module Context
- 이 모듈은 JWT, OAuth, principal 등 인증 기능 자체를 담당한다.
- 주요 책임
  - 토큰 발급/재발급/무효화
  - OAuth 로그인 흐름 조합
  - 인증 상태 저장소 인터페이스
  - 인증 주체 모델과 필터 보조 컴포넌트
- HTTP 보안 체인 조립은 `ssd-api`가 담당한다.

## Constraints
- `ssd-auth`는 `ssd-common`, `ssd-domain`에만 의존한다.
- 인증 정책은 제공하지만 컨트롤러/보안 체인 설정은 두지 않는다.
- provider별 통신 모델은 provider 하위 패키지에 둔다.

## Testing
- 모듈 테스트 실행: `./gradlew :ssd-auth:test`
