# Glossary

## 목적
- SSD에서 사용하는 핵심 도메인 용어를 고정한다.
- 같은 개념을 다른 단어로 혼용하지 않도록 기준을 제공한다.

## 작성 규칙
- `Term`: 공식 용어
- `Definition`: SSD 문맥에서의 정확한 의미
- `Allowed aliases`: 문맥상 허용 가능한 보조 표현
- `Disallowed aliases`: 혼동을 유발해 지양하는 표현
- `Follow-up issue`: 실제 rename 또는 구조 반영 이슈 번호

## 용어 목록
| Term | Definition | Allowed aliases | Disallowed aliases | Follow-up issue |
| --- | --- | --- | --- | --- |
| Document | SSD에서 저장, 조회, 요약, 평가의 기준이 되는 사업계획서 문서 aggregate다. | 문서 | file, paper | `#139`, `#141`, `#142` |
| Paragraph | 문서 내용 중 문단/문장 같은 텍스트 콘텐츠를 가리키는 도메인 용어다. `Paragraph` 자체는 식별 스코프 이름이 아니라 텍스트 내용 개념을 뜻한다. | text paragraph, 문단 | block(문서-로컬 식별 단위를 가리킬 때), image | `#139`, `#140` |
| Block | 하나의 문서 안에서 각 내용 항목을 식별하고, 순서/주석/변경 비교 기준으로 삼는 문서-로컬 단위다. 현재는 `blockId`와 함께 text/image 항목을 공통 처리하는 문맥에 사용한다. | document-local block, 블록 | paragraph(텍스트 내용 자체를 가리킬 때) | `#138`, `#139`, `#140`, `#141`, `#142` |
| Review | 사람이 남긴 평가 결과다. SSD에서는 특히 `Evaluator`가 남긴 리뷰를 뜻한다. | evaluator review, human review | evaluation, ai review | `#140` |
| Evaluation | 시스템 또는 외부 AI가 산출한 평가 결과다. 사람 리뷰와 구분되는 개념으로 유지한다. | ai evaluation, system evaluation | review | `#140`, `#143` |
| Checklist | 평가 기준 또는 판단 항목의 집합이다. 리뷰나 평가 결과 자체가 아니라 판단 근거를 뜻한다. | criteria set, 체크 항목 | review, evaluation | `#140` |
| Summary | 문서 내용을 요약한 기본 요약 텍스트다. 현재 SSD에서는 주로 AI가 생성한 대표 요약을 뜻하며, 필드명은 그대로 `summary`를 유지한다. | basic summary, 요약 | details, description | `#140` |
| Keyword | 문서에서 추출한 핵심 키워드 결과다. 태그나 일반 메타데이터와 구분한다. | keyword list, 핵심 키워드 | tag, label | `#140` |
| Member | 인증, 영속성, 권한 부여의 기준이 되는 공식 회원 엔티티 명칭이다. 내부 코드에서는 `User`보다 `Member`를 우선한다. | account(외부 설명 한정), 회원 | user(내부 코드 기준) | `#143`, `#144` |
| Author | `Member`가 문서 작성자 또는 문서 소유자 문맥에서 사용될 때의 역할명이다. SSD에서는 `Author`와 `Owner`를 같은 개념으로 본다. 저장 엔티티 이름 자체를 대체하지는 않는다. | owner, 작성자 | member(역할 문맥에서), user | `#139`, `#140` |
| Evaluator | `Member`가 평가자 역할로 사용될 때의 역할명이다. `Reviewer`와 혼용하지 않는다. | reviewer(외부 설명 한정), 평가자 | reviewer(내부 공식 명칭으로 사용) | `#140` |
| Blacklist | 로그아웃되었거나 더 이상 허용하지 않는 access token 식별자를 저장하는 무효화 저장소이자, 해당 등록 행위를 설명하는 공식 용어다. | token blacklist | revoke list, invalid token store | `#144` |
| Reissue | refresh token을 이용해 access/refresh token을 다시 발급하는 유스케이스 이름이다. API 명칭과 서비스 명칭 모두 이 용어를 우선한다. | token reissue, 재발급 | rotate | `#144` |
| Rotate | 재발급 과정에서 기존 refresh token을 폐기하고 새 refresh token으로 교체하는 내부 전략 이름이다. 유스케이스 이름 자체로 사용하지 않는다. | refresh rotation | reissue(전부를 가리킬 때) | `#144` |

## 우선 정리 대상
- Document
- Paragraph
- Block
- Review
- Evaluation
- Checklist
- Summary
- Keyword
- Member
- Author
- Evaluator
- Blacklist
- Reissue
- Rotate

## 보조 메모
- `summary`는 기본 요약이다. 현재 `Document.summary` 주석 기준으로는 세 줄 요약 성격을 가진다.
- `shortSummary`는 더 짧은 카드형 요약이다. 현재 `ExternalAiService`에서는 외부 응답의 `small` 값을 여기에 저장한다.
- `details`는 상세 요약 또는 상세 설명 텍스트다. 이름은 유지하되, `summary`보다 긴 설명이라는 의미로 고정한다.
- 이 세 필드는 당장 rename하지 않는다. 후속 이슈에서는 의미 충돌이 있는지만 검토한다.
