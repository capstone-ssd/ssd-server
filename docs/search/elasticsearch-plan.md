# Elasticsearch 검색엔진 최적화 구현 계획

## 1. 왜 Elasticsearch까지 도입하는가

현재 문서 검색은 PostgreSQL 기반으로 단계적으로 개선되어 있다.

| 단계 | 방식 | 장점 | 한계 |
| --- | --- | --- | --- |
| 1 | `LIKE '%keyword%'` | 구현이 단순함 | 대량 데이터에서 순차 스캔 비용이 큼 |
| 2 | B-Tree Prefix 검색 | `keyword%` 검색은 빠름 | 중간 단어 포함 검색에 약함 |
| 3 | `pg_trgm + GIN` | 포함 검색과 유사도 검색 개선 | DB가 검색 부하까지 함께 부담함 |
| 4 | Elasticsearch | 검색 전용 엔진으로 확장 가능 | 색인 동기화와 운영 복잡도 증가 |

Elasticsearch 도입의 핵심 목적은 단순히 검색 시간을 줄이는 것이 아니라, 검색 책임을 DB에서 분리하고 검색 품질을 높이는 것이다.

| 목표 | 설명 |
| --- | --- |
| 검색 부하 분리 | PostgreSQL은 원본 저장소, Elasticsearch는 검색용 Read Model로 분리 |
| 한글 검색 품질 개선 | Nori analyzer를 활용해 한글 형태소 기반 검색 적용 |
| 관련도 기반 정렬 | 단순 최신순이 아니라 검색어와의 관련도 점수를 활용 |
| 관련 검색어 확장 | 제목, 키워드 기반 suggestion 기능 고도화 |
| 향후 본문 검색 확장 | 현재는 제목/키워드 중심, 이후 문서 본문 검색으로 확장 가능 |

## 2. 전체 구조

~~~mermaid
flowchart LR
    A["Client"] --> B["ssd-api"]
    B --> C["ssd-application"]
    C --> D["ssd-domain Search UseCase"]
    D --> E["DocumentSearchRepository Port"]

    E --> F["PostgreSQL Search Adapter"]
    E --> G["Elasticsearch Search Adapter"]

    H["Document 생성/수정/삭제"] --> I["PostgreSQL 저장"]
    I --> J["After Commit Event"]
    J --> K["Elasticsearch 색인 동기화"]
~~~

기본 원칙은 다음과 같다.

| 구분 | 역할 |
| --- | --- |
| PostgreSQL | 원본 데이터 저장소 |
| Elasticsearch | 검색 전용 Read Model |
| 기존 `pg_trgm` 검색 | ES 장애 시 fallback 또는 성능 비교 기준 |
| API 스펙 | 가능하면 기존 검색 API 유지 |

## 3. 단일 노드 Elasticsearch 설정

초기 구현은 단일 노드 Elasticsearch를 기준으로 한다.

| 항목 | 설정 | 이유 |
| --- | --- | --- |
| Primary shard | `1` | 단일 노드에서는 shard를 여러 개로 나눠도 분산 이점이 없음 |
| Replica shard | `0` | replica는 다른 노드에 복제해야 의미가 있으므로 단일 노드에서는 불필요 |
| Index | `ssd-{profile}-documents` | dev/prod/test 환경별 index 분리 |

예상 index setting은 다음과 같다.

~~~json
{
  "settings": {
    "number_of_shards": 1,
    "number_of_replicas": 0
  }
}
~~~

단일 노드 구조는 다음과 같다.

~~~mermaid
flowchart LR
    A["Elasticsearch 단일 노드"] --> B["ssd-dev-documents"]
    B --> C["Primary Shard 0"]
~~~

단일 노드에서 shard를 여러 개로 늘리면 검색 요청이 여러 shard로 fan-out 된 뒤 다시 merge되어야 한다. 데이터 규모가 크지 않은 현재 단계에서는 shard 관리 비용이 더 커질 수 있으므로 `1 primary shard + 0 replica`로 시작한다.

## 4. Index Document 설계

문서 검색 응답에 필요한 값을 ES document에 포함한다.

| 필드 | 타입 | 용도 |
| --- | --- | --- |
| `documentId` | `long` | 문서 식별자 |
| `memberId` | `long` 또는 `keyword` | 본인 문서만 검색하기 위한 filter |
| `folderId` | `long` 또는 `keyword` | 폴더 단위 검색 확장 가능 |
| `title` | `text` | 제목 검색 대상 |
| `title.keyword` | `keyword` | 정확 일치, 정렬, 집계 |
| `keywords` | `text` | 관련 검색어/suggestion 대상 |
| `updatedAt` | `date` | 최신순 정렬 |
| `createdAt` | `date` | 생성순 정렬 |
| `isBookmarked` | `boolean` | 검색 응답 필드 유지 |
| `isDeleted` | `boolean` | soft delete 대응 |

## 5. Analyzer 전략

한글 검색 품질을 위해 Nori analyzer를 우선 검토한다.

| 필드 | Analyzer | 목적 |
| --- | --- | --- |
| `title` | `nori` | 한글 형태소 기반 일반 검색 |
| `title.autocomplete` | `edge_ngram` | prefix/autocomplete 검색 |
| `title.keyword` | `keyword` | 정확 일치와 정렬 |
| `keywords` | `nori` | 관련 키워드 검색 |

검색 쿼리는 다음 기준으로 설계한다.

| 기준 | 설명 |
| --- | --- |
| Filter | `memberId`, `isDeleted=false`는 score에 영향을 주지 않는 filter로 처리 |
| Match | `title`, `keywords`를 대상으로 관련도 계산 |
| Sort | 기본은 `_score desc`, 동일 점수에서는 `updatedAt desc` |
| Boost | 제목 일치 점수를 키워드 일치보다 높게 설정 |

## 6. 모듈별 구현 계획

| 모듈 | 구현 내용 |
| --- | --- |
| `ssd-domain` | `DocumentSearchRepository` port, 검색 조건/result 모델 정의 |
| `ssd-application` | 검색 facade 유지, 문서 변경 후 색인 이벤트 발행 |
| `ssd-infra` | PostgreSQL 검색 adapter와 Elasticsearch 검색 adapter 구현 |
| `ssd-external` | Elasticsearch client 설정, index 설정 관리 |
| `ssd-api` | 기존 검색 API 유지, 필요 시 ES 기반 응답 필드만 보강 |
| `docs/search` | ES 구조, mapping, 성능 비교 결과 문서화 |

검색 책임은 기존 `DocumentRepository`에서 분리하는 방향이 좋다.

| 기존 | 개선 |
| --- | --- |
| `DocumentRepository.searchDocuments()` | `DocumentSearchRepository.search()` |
| `DocumentRepository.findSearchSuggestions()` | `DocumentSearchRepository.suggest()` |
| 저장소와 검색 책임 혼합 | 원본 저장과 검색 Read Model 책임 분리 |

## 7. 색인 동기화 전략

외부 시스템인 Elasticsearch는 DB 트랜잭션에 직접 포함할 수 없다. 따라서 DB 저장 성공 이후 색인을 동기화한다.

~~~mermaid
sequenceDiagram
    participant API as API
    participant APP as Application
    participant DB as PostgreSQL
    participant ES as Elasticsearch

    API->>APP: 문서 생성/수정/삭제 요청
    APP->>DB: 원본 데이터 저장
    DB-->>APP: commit
    APP->>ES: 문서 색인/수정/삭제
    ES-->>APP: 색인 완료
~~~

초기 구현은 다음 순서로 진행한다.

| 단계 | 방식 | 설명 |
| --- | --- | --- |
| 1차 | After Commit Event | DB commit 이후 ES 색인 요청 |
| 2차 | 실패 로그 + 수동 재처리 | ES 장애 시 누락된 documentId 확인 가능하게 처리 |
| 3차 | Outbox Pattern | 검색 동기화 안정성이 더 중요해지면 outbox table 기반 재시도 도입 |

## 8. 장애 대응 방향

| 상황 | 대응 |
| --- | --- |
| ES 검색 실패 | PostgreSQL `pg_trgm` 검색으로 fallback |
| ES 색인 실패 | 실패 로그 기록 후 재처리 대상 관리 |
| ES index mapping 변경 | 새 index 생성 후 alias 전환 방식 검토 |
| ES와 DB 불일치 | documentId 기준 재색인 API 또는 batch job 제공 |

초기에는 ES 장애가 전체 서비스 장애로 번지지 않도록 검색 fallback을 유지한다.

## 9. 테스트 전략

| 테스트 | 목적 |
| --- | --- |
| Domain 단위 테스트 | 검색 조건 검증, 빈 검색어 예외 처리 |
| Infra 통합 테스트 | Testcontainers Elasticsearch로 색인/검색 검증 |
| API 통합 테스트 | 기존 검색 API 응답 스펙 유지 확인 |
| 성능 비교 테스트 | PostgreSQL `pg_trgm` 검색과 ES 검색 평균/p95/p99 비교 |

성능 비교 기준은 기존 검색 성능 테스트와 맞춘다.

| 비교 대상 | 측정 지표 |
| --- | --- |
| `LIKE '%keyword%'` | 평균, p95, p99 |
| B-Tree Prefix | 평균, p95, p99 |
| `pg_trgm + GIN` | 평균, p95, p99 |
| Elasticsearch | 평균, p95, p99, 결과 품질 |

## 10. 구현 순서

| 순서 | 작업 |
| --- | --- |
| 1 | Elasticsearch Docker/Testcontainers 설정 추가 |
| 2 | `DocumentSearchRepository` port 분리 |
| 3 | 기존 PostgreSQL 검색 adapter를 fallback 구현체로 정리 |
| 4 | Elasticsearch index mapping/settings 생성 |
| 5 | Elasticsearch 검색 adapter 구현 |
| 6 | 문서 생성/수정/삭제 후 색인 동기화 추가 |
| 7 | 검색 API 내부 구현을 ES 우선 검색으로 전환 |
| 8 | ES 실패 시 PostgreSQL fallback 적용 |
| 9 | 성능 테스트로 `pg_trgm` 대비 개선 수치 측정 |
| 10 | 결과를 `docs/search`에 문서화 |

## 11. 최종 방향

초기 ES 도입은 과하게 복잡하게 시작하지 않는다.

| 항목 | 결정 |
| --- | --- |
| 구성 | 단일 노드 Elasticsearch |
| Shard | `1 primary shard` |
| Replica | `0 replica` |
| 검색 대상 | 문서 제목, 키워드 |
| 색인 방식 | After Commit Event 기반 |
| fallback | 기존 PostgreSQL `pg_trgm` 검색 유지 |
| 확장 방향 | 본문 검색, 자동완성, outbox 재처리, index alias 전환 |

따라서 이번 이슈의 1차 목표는 “ES를 완성형 검색 시스템으로 한 번에 도입”하는 것이 아니라, 기존 PostgreSQL 검색 구조를 유지하면서 ES 기반 검색 Read Model을 추가하고 정량적으로 비교할 수 있는 구조를 만드는 것이다.
