# Naming Decision Log

## 목적
- 용어 충돌, 허용 예외, naming 방향 결정의 근거를 기록한다.
- 같은 논의를 반복하지 않도록 결정 이력을 남긴다.

## 기록 규칙
- `Date`: 결정 날짜
- `Issue/PR`: 관련 이슈 또는 PR
- `Decision`: 무엇을 어떻게 정했는지
- `Reason`: 왜 그렇게 정했는지
- `Impact`: 어떤 후속 변경에 영향을 주는지

## 기록
| Date | Issue/PR | Decision | Reason | Impact |
| --- | --- | --- | --- | --- |
| 2026-04-08 | `#137` | 내부 공식 사용자 용어는 `Member`로 고정한다. | 실제 엔티티, 저장소 인터페이스, 권한 모델이 `Member`를 기준으로 설계되어 있다. `user`는 외부 설명 문맥에만 제한적으로 허용한다. | `JWTService`, OAuth/JWT naming 정리 (`#143`, `#144`) |
| 2026-04-08 | `#137` | 문서 내부 공통 단위는 `Block`, 텍스트 하위 개념은 `Paragraph`로 구분한다. | 현재 코드에서 이미지와 텍스트가 모두 `blockId` 기준으로 관리된다. `Paragraph`를 전체 단위처럼 쓰면 이미지 문맥에서 의미가 무너진다. | Document DTO 및 `DocumentService` naming 정리 (`#138`, `#139`, `#140`, `#141`, `#142`) |
| 2026-04-08 | `#137` | 사람 평가에는 `Review`, AI/시스템 평가에는 `Evaluation`을 사용한다. | 평가자 리뷰와 외부 AI 평가를 같은 단어로 처리하면 응답 모델과 서비스 책임이 흐려진다. | External AI/평가자 도메인 DTO 정리 (`#140`) |
| 2026-04-08 | `#137` | `Reissue`는 유스케이스 이름, `Rotate`는 refresh 교체 전략 이름으로 분리한다. | 현재 `/reissue` API와 `refreshRotate()` 메서드가 다른 추상화 레벨의 용어를 섞고 있다. | JWT/Auth naming 정리 (`#144`) |
| 2026-04-08 | `#137` | 무효화된 access token 저장 개념은 `Blacklist`로 유지한다. | 이미 저장소 이름과 의도가 `AccessTokenBlacklistRepository`로 드러나 있고, 이번 단계에서는 저장소 개념을 유지하는 편이 가장 작다. | JWT/Auth 문서화와 naming 정리 (`#144`) |
