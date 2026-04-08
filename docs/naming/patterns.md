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
- 무효화: `revoke`, `blacklist`

## boolean 이름
- `is`, `has`, `can`, `should` 접두사를 우선 사용한다.
- 도메인 진술을 그대로 쓰는 경우에도 긍정형을 기본으로 한다.
- 이중 부정이나 부정형 플래그는 피한다.

## 권장 사항
- `user`, `member`처럼 이미 공식 용어가 있는 개념은 하나로 통일한다.
- `paragraph`, `block`처럼 포함 관계가 있는 단어는 상위/하위 개념을 먼저 정의한 뒤 사용한다.
- `review`, `evaluation`처럼 사람 평가와 시스템 평가가 분리되는 경우 용어 경계를 먼저 고정한다.
