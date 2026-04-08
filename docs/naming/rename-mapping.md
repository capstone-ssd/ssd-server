# Rename Mapping

## 목적
- 현재 이름과 목표 이름을 연결해, 대규모 rename을 한 번에 하지 않고 이슈별로 추적한다.
- 실제 rename은 구조 리팩터링 이슈에서 수행하고, 이 문서는 사전 합의와 추적 용도로 사용한다.

## 작성 규칙
- `Current name`: 현재 코드에 존재하는 이름
- `Target name`: 목표 이름
- `Reason`: 왜 바꾸는지
- `Follow-up issue`: 실제 반영 이슈
- `Status`: `planned`, `in-progress`, `done`

## 매핑
| Current name | Target name | Reason | Follow-up issue | Status |
| --- | --- | --- | --- | --- |
| `userRepository` | `memberRepository` | 내부 공식 엔티티 용어를 `Member`로 맞춘다. | `#144` | planned |
| `findUser` | `member` 또는 `foundMember` | 조회 결과 변수명도 공식 용어에 맞춘다. | `#144` | planned |
| `refreshRotate` | `reissueWithRotation` 또는 `reissueTokens` | 유스케이스 이름과 내부 전략 이름을 분리한다. | `#144` | planned |
| `CreateDocumentParagraphRequest` | `CreateDocumentBlockRequest` | 요청 모델이 `blockId`, `type`, 이미지/텍스트 payload를 함께 다루는 문서-로컬 block 입력이라는 점을 반영한다. | `#139` | done |
| `DocumentParagraphDto` | `DocumentBlockResponseItem` | 응답 모델이 텍스트 내용 자체보다 문서-로컬 block 단위를 표현한다는 점을 드러낸다. | `#139` | done |
| `saveCreateParagraphsIfPresent` | `saveCreateBlocksIfPresent` | 서비스 내부 메서드와 실제 처리 대상을 일치시킨다. | `#141` | planned |
| `replaceParagraphsAndSyncComments` | `replaceBlocksAndSyncComments` | 삭제/생성/주석 동기화 기준이 block 단위라는 점을 반영한다. | `#142` | planned |
| `findByDocumentAndReviewer` | `findByDocumentAndEvaluator` | 평가자 역할명과 저장소 메서드명을 일치시킨다. | `#140` | planned |
| `existsByDocumentAndReviewer` | `existsByDocumentAndEvaluator` | 평가자 역할명과 저장소 메서드명을 일치시킨다. | `#140` | planned |
| `reviewer` 필드/변수/헬퍼 | `evaluator` 필드/변수/헬퍼 | `Evaluator*` 엔티티/API prefix와 내부 명칭을 일치시킨다. | `#140` | planned |
| `summary` / `shortSummary` / `details` | 이름 유지, 의미만 고정 | 현재 저장/응답 필드가 이미 쓰이고 있어 이 단계에서는 rename보다 의미 정리가 우선이다. | `#140` | planned |
