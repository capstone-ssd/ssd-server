# Mixed Terms

## 목적
- 현재 SSD 코드에서 같은 개념이 다른 이름으로 섞여 있는 사례를 기록한다.
- 이 문서는 즉시 rename하기 위한 체크리스트가 아니라, 후속 이슈에서 어떤 단어를 정리해야 하는지 추적하기 위한 문서다.

## 혼용 사례
| Canonical term | Current mixed terms | Example | Why it is a problem | Follow-up issue |
| --- | --- | --- | --- | --- |
| Member | `Member`, `user`, `findUser`, `userRepository` | `JWTService`의 `MemberRepository userRepository`와 `Member findUser` | 엔티티 공식 명칭과 내부 변수명이 어긋나서 역할과 모델이 분리되어 보인다. | `#143`, `#144` |
| Block / Paragraph | `Paragraph`, `Block`, `DocumentParagraph`, `CreateDocumentParagraphRequest`, `ResolvedDocumentBlock` | `DocumentService`는 `blocks`를 순회하지만 저장은 `DocumentParagraph`로 한다. | 이미지와 텍스트를 함께 다루는 순간 `Paragraph`가 상위 개념처럼 보이게 되어 경계가 무너진다. | `#138`, `#139`, `#140`, `#141`, `#142` |
| Review / Evaluation | `EvaluatorReview`, `review`, `evaluation`, `ExternalAiEvaluation*` | 평가자 서비스는 `Review`, AI 응답은 `Evaluation`, `Document` 필드는 둘 다 함께 가진다. | 사람 평가와 시스템 평가의 결과 모델이 섞여 보인다. | `#140` |
| Evaluator | `Evaluator`, `reviewer` | `EvaluatorReviewRepository.findByDocumentAndReviewer`, `EvaluatorReviewService.getReviewer` | 도메인 이름은 evaluator인데 내부 변수/메서드는 reviewer를 쓴다. | `#140` |
| Author | `member`, `authorId`, `authorName` | `GetDocumentResponse`는 `doc.getMember()`를 `authorId`, `authorName`으로 노출한다. | 저장 주체와 역할 표현이 같은 문맥에서 섞인다. | `#139`, `#140` |
| Reissue / Rotate | `reissue`, `refreshRotate` | `/reissue` 엔드포인트는 유스케이스 이름이고 서비스 메서드는 전략 이름을 쓴다. | 같은 기능을 API와 서비스가 다른 이름으로 부르고 있어 추상화 레벨이 맞지 않는다. | `#144` |
| Blacklist / Revoke | `blacklist`, `logout`, `invalidate` 의미 혼재 | `AccessTokenBlacklistRepository`는 저장소 이름이고 로그아웃 설명은 무효화 행위에 초점이 있다. | 저장 개념과 행위 개념이 섞이면 메서드명 설계가 흔들린다. | `#144` |
| Summary / ShortSummary / Details | `summary`, `shortSummary`, `details` | `Document` 엔티티와 `ExternalAiService`가 세 종류의 요약 표현을 동시에 사용한다. | 요약 결과의 계층이 코드만 보고 즉시 이해되지 않는다. | `#140` |

## 예시 위치
- `Member` / `user` 혼용
  - `/Users/jeonjaeyeon/Desktop/capstone/ssd/ssd-domain/src/main/java/or/hyu/ssd/global/jwt/service/JWTService.java`
- `Paragraph` / `Block` 혼용
  - `/Users/jeonjaeyeon/Desktop/capstone/ssd/ssd-domain/src/main/java/or/hyu/ssd/domain/document/service/DocumentService.java`
  - `/Users/jeonjaeyeon/Desktop/capstone/ssd/ssd-domain/src/main/java/or/hyu/ssd/domain/document/controller/dto/CreateDocumentParagraphRequest.java`
- `Author` / `Member` 혼용
  - `/Users/jeonjaeyeon/Desktop/capstone/ssd/ssd-domain/src/main/java/or/hyu/ssd/domain/document/controller/dto/GetDocumentResponse.java`
- `Evaluator` / `reviewer` 혼용
  - `/Users/jeonjaeyeon/Desktop/capstone/ssd/ssd-domain/src/main/java/or/hyu/ssd/domain/document/repository/EvaluatorReviewRepository.java`
  - `/Users/jeonjaeyeon/Desktop/capstone/ssd/ssd-domain/src/main/java/or/hyu/ssd/domain/document/service/EvaluatorReviewService.java`
- `Reissue` / `Rotate` 혼용
  - `/Users/jeonjaeyeon/Desktop/capstone/ssd/ssd-api/src/main/java/or/hyu/ssd/global/jwt/controller/JWTController.java`
  - `/Users/jeonjaeyeon/Desktop/capstone/ssd/ssd-domain/src/main/java/or/hyu/ssd/global/jwt/service/JWTService.java`
