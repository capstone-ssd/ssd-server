# Mixed Terms

## 목적
- 현재 SSD 코드에서 같은 개념이 다른 이름으로 섞여 있는 사례를 기록한다.
- 이 문서는 즉시 rename하기 위한 체크리스트가 아니라, 후속 이슈에서 어떤 단어를 정리해야 하는지 추적하기 위한 문서다.

## 혼용 사례
| Canonical term | Current mixed terms | Example | Why it is a problem | Follow-up issue |
| --- | --- | --- | --- | --- |
| Member | `Member`, `user`, `findUser`, `userRepository` | `JWTService`의 `MemberRepository userRepository`와 `Member findUser` | 엔티티 공식 명칭과 내부 변수명이 어긋나서 역할과 모델이 분리되어 보인다. | `#143`, `#144` |
| Block / Paragraph | `Paragraph`, `Block`, `DocumentParagraph`, `ResolvedDocumentBlock` | `#139`에서 API DTO는 `CreateDocumentBlockRequest`, `DocumentBlockResponseItem`로 정리됐지만 저장 모델과 서비스 내부 보조 구조는 여전히 `Paragraph`와 `Block`을 함께 사용한다. | 텍스트 콘텐츠 개념과 문서-로컬 식별 단위가 섞이면 `Paragraph`와 `Block`의 스코프가 흐려진다. | `#140`, `#141`, `#142` |
| Review / Evaluation | `EvaluatorReview`, `review`, `evaluation`, `ExternalAiEvaluation*` | 평가자 서비스는 `Review`, AI 응답은 `Evaluation`, `Document` 필드는 둘 다 함께 가진다. | 사람 평가와 시스템 평가의 결과 모델이 섞여 보인다. | `#142` |
| Evaluator | `Evaluator`, `reviewer` | `EvaluatorReviewRepository.findByDocumentAndReviewer`, `EvaluatorReviewService.getReviewer`, `EvaluatorReview.reviewer` | 엔티티/API prefix는 evaluator인데 내부 변수/메서드/필드는 reviewer를 쓴다. | `#142` |
| Author | `member`, `authorId`, `authorName` | `GetDocumentResponse`는 `doc.getMember()`를 `authorId`, `authorName`으로 노출한다. | 저장 주체와 역할 표현이 같은 문맥에서 섞인다. | `#142` |
| Reissue / Rotate | `reissue`, `refreshRotate` | `/reissue` 엔드포인트는 유스케이스 이름이고 서비스 메서드는 전략 이름을 쓴다. | 같은 기능을 API와 서비스가 다른 이름으로 부르고 있어 추상화 레벨이 맞지 않는다. | `#144` |
| Blacklist | `blacklist`, `logout`, `invalidate` 의미 혼재 | `AccessTokenBlacklistRepository`는 `blacklist`를 쓰지만 설명과 일부 문맥은 `invalidate` 류 표현을 섞는다. | 토큰 무효화 저장/행위를 `blacklist`로 고정했으므로 다른 동사를 섞으면 naming 기준이 흔들린다. | `#144` |
| Summary / ShortSummary / Details | `summary`, `shortSummary`, `details` | `Document` 엔티티와 `ExternalAiService`가 세 종류의 요약 표현을 동시에 사용한다. | 이름은 유지하기로 했으므로, 후속 이슈에서는 rename보다 각 필드의 의미와 노출 방식이 일관한지만 확인하면 된다. | `#142` |

## 예시 위치
- `Member` / `user` 혼용
  - `ssd-auth/src/main/java/or/hyu/ssd/auth/jwt/service/JWTService.java`
- `Paragraph` / `Block` 혼용
  - `ssd-domain/src/main/java/or/hyu/ssd/document/application/service/DocumentCommandService.java`
  - `ssd-domain/src/main/java/or/hyu/ssd/document/domain/model/DocumentParagraph.java`
- `Author` / `Member` 혼용
  - `ssd-api/src/main/java/or/hyu/ssd/api/document/response/GetDocumentResponse.java`
- `Evaluator` / `reviewer` 혼용
  - `ssd-domain/src/main/java/or/hyu/ssd/document/repository/EvaluatorReviewRepository.java`
  - `ssd-domain/src/main/java/or/hyu/ssd/document/application/service/EvaluatorReviewService.java`
- `Reissue` / `Rotate` 혼용
  - `ssd-api/src/main/java/or/hyu/ssd/api/auth/controller/JWTController.java`
  - `ssd-auth/src/main/java/or/hyu/ssd/auth/jwt/service/JWTService.java`
