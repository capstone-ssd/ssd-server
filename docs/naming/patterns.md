# Naming Patterns

## 기본 원칙
1. 같은 개념에는 같은 단어를 사용한다.
2. 이름은 구현 방식보다 역할과 의도를 드러내야 한다.
3. API 용어와 도메인 용어가 다를 경우, 경계에서 변환하고 내부에서는 도메인 용어를 우선한다.
4. 클래스명은 책임, 메서드명은 행위, 변수명은 문맥 속 대상을 표현해야 한다.

## 클래스명
- Controller: `*Controller`
- Service: `*Service`
- Repository interface: `*Repository`
- JPA repository: `*JpaRepository`
- Port: `*Port`
- Adapter: `*Adapter`
- Config: `*Config`
- Properties: `*Properties`
- Exception handler: `*Handler`

## DTO
- API 요청: `*Request`
- API 응답: `*Response`
- 내부 유스케이스 입력: `*Command` 검토
- 내부 유스케이스 결과: `*Result` 검토
- 리스트 아이템 응답: `*ListItemResponse`
- 상세 응답: `*DetailResponse`

## 메서드명
- 조회: `get`, `find`, `list`, `load`
- 생성: `create`, `issue`, `generate`
- 수정: `update`, `change`, `replace`, `sync`
- 삭제: `delete`, `remove`
- 검증: `validate`, `require`, `check`
- 변환/해석: `resolve`, `map`, `convert`, `extract`
- 무효화: `blacklist`

## 동사 의미 고정
- `get`: 반드시 존재해야 하는 단일 값을 조회한다. 실패 시 예외를 던지는 문맥에 우선 사용한다.
- `find`: 없을 수도 있는 값을 조회한다. `Optional`, nullable, 빈 결과와 함께 쓰는 쪽에 우선 사용한다.
- `list`: 복수 결과를 순서 있는 컬렉션으로 조회한다.
- `load`: 외부 저장소/지연 로딩/복원처럼 가져오는 과정이 강조될 때 사용한다.
- `create`: 새로운 도메인 객체나 영속 데이터를 만든다.
- `issue`: 토큰, 인증 코드, 링크처럼 발급 성격이 강한 결과에 사용한다.
- `generate`: 계산, 요약, AI 결과처럼 파생 산출물을 만든다.
- `update`: 기존 객체의 일부 상태를 수정한다.
- `replace`: 기존 값을 새 값 전체로 교체한다.
- `sync`: 내부 상태를 외부 상태 또는 다른 저장 표현과 맞춘다.
- `delete`: 영속 데이터나 리소스를 제거한다.
- `remove`: 컬렉션, 관계, 연결을 끊는 문맥에 우선 사용한다.
- `validate`: 규칙 검증 전체를 수행한다. 실패 시 예외를 동반해도 된다.
- `require`: 반드시 만족해야 하는 전제나 존재성을 강제한다.
- `check`: 가벼운 검사 또는 boolean 판단에 우선 사용한다.
- `resolve`: 식별자, 요청 payload, 외부 입력을 실제 도메인 값으로 해석한다.
- `extract`: 헤더, 문자열, 토큰, 본문에서 일부 값을 꺼낸다.
- `map` / `convert`: 표현 형식을 다른 표현으로 변환한다.
- `blacklist`: access token 같은 무효화 대상을 블랙리스트 저장소에 등록하거나, 블랙리스트 여부를 기준으로 다루는 문맥에 사용한다.

## boolean 이름
- `is`, `has`, `can`, `should` 접두사를 우선 사용한다.
- 도메인 진술을 그대로 쓰는 경우에도 긍정형을 기본으로 한다.
- 이중 부정이나 부정형 플래그는 피한다.

## 권장 사항
- `user`, `member`처럼 이미 공식 용어가 있는 개념은 하나로 통일한다.
- `paragraph`, `block`처럼 내용 개념과 식별 개념이 섞인 단어는 스코프 차이를 먼저 정의한 뒤 사용한다.
- `review`, `evaluation`처럼 사람 평가와 시스템 평가가 분리되는 경우 용어 경계를 먼저 고정한다.
