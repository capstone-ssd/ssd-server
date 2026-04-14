# Testing Strategy

## 목적
- 단위 테스트와 통합 테스트의 경계를 명확히 한다.
- 테스트 메서드는 `given/when/then` 흐름이 코드에서 바로 읽히도록 유지한다.
- 테스트 가독성 리팩터링은 우선 테스트 코드 내부 구조 개선으로 해결하고, 프로덕션 구조 변경은 별도 이슈에서 다룬다.

## 분류 기준
- 단위 테스트
  - 위치: `src/test/java`
  - 파일명: `*Test.java`
  - 대상: 서비스 규칙, 유틸, validator, mapper, 계산/변환 로직
  - 원칙: Mockito 또는 순수 객체 중심으로 검증한다.
- 통합 테스트
  - 위치: `src/integrationTest/java`
  - 파일명: `*IntegrationTest.java`
  - 대상: Spring Context, MVC, JPA, Security Filter Chain, 실제 빈 조합
  - 원칙: `@SpringBootTest`, `@DataJpaTest`, `@WebMvcTest`, `@JdbcTest` 계열은 통합 테스트로 취급한다.

## 작성 규칙
- 각 테스트 메서드는 반드시 아래 주석을 포함한다.
  - `// given`
  - `// when`
  - `// then`
- 하나의 테스트는 하나의 기능 또는 하나의 실패 조건만 검증한다.
- 공통 fixture가 반복될 경우 우선 같은 테스트 클래스 내부 `private` helper 메서드로 정리한다.
- 테스트 가독성을 위해 검증 포인트가 두 개 이상이면 테스트를 분리하는 것을 우선 고려한다.

## Gradle 규칙
- `test`: 단위 테스트 실행
- `integrationTest`: 통합 테스트 실행
- `verifyTestConventions`: 테스트 위치, 이름, `given/when/then` 주석 규칙 검증
- `check`: `test`, `integrationTest`, `verifyTestConventions`를 모두 포함한다.
