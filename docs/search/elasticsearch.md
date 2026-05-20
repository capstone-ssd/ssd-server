# Elasticsearch 기반 문서 검색 구현

## 1. 구현 범위

이번 구현은 기존 PostgreSQL 검색을 제거하지 않고, Elasticsearch를 검색 전용 Read Model로 추가하는 방식으로 진행하였다.

| 항목 | 구현 내용 |
| --- | --- |
| 검색 우선순위 | Elasticsearch 우선 검색 |
| fallback | Elasticsearch 장애 시 PostgreSQL `pg_trgm` 검색 사용 |
| 원본 저장소 | PostgreSQL 유지 |
| 색인 동기화 | DB commit 이후 이벤트 기반 색인 |
| 기본 설정 | `app.search.elasticsearch.enabled=false` |

## 2. 검색 흐름

~~~mermaid
flowchart LR
    A["DocumentQueryService"] --> B["DocumentSearchRepository"]
    B --> C["DelegatingDocumentSearchRepository"]
    C --> D{"ES enabled?"}
    D -->|Yes| E["Elasticsearch 검색"]
    D -->|No| F["PostgreSQL pg_trgm 검색"]
    E -->|실패| F
~~~

`DocumentQueryService`는 더 이상 `DocumentRepository`의 검색 메서드를 직접 호출하지 않는다. 검색 책임은 `DocumentSearchRepository`로 분리하였다.

| Port | 역할 |
| --- | --- |
| `DocumentRepository` | 원본 문서 저장/조회/삭제 |
| `DocumentSearchRepository` | 문서 검색, 검색어 추천, ES 색인/삭제 |

## 3. Index 설정

초기 Elasticsearch는 단일 노드 기준으로 구성한다.

| 설정 | 값 | 이유 |
| --- | --- | --- |
| Primary shard | `1` | 단일 노드에서는 shard 분산 이점이 없음 |
| Replica shard | `0` | 복제할 다른 노드가 없음 |
| Index name | `app.search.elasticsearch.index-name` | 환경별 index 분리 |

~~~json
{
  "settings": {
    "number_of_shards": 1,
    "number_of_replicas": 0
  }
}
~~~

## 4. Document 구조

Elasticsearch에는 검색 응답에 필요한 최소 필드를 저장한다.

| 필드 | 용도 |
| --- | --- |
| `documentId` | 문서 식별자 |
| `memberId` | 본인 문서만 검색하기 위한 filter |
| `folderId` | 폴더 정보 응답 |
| `title` | 제목 검색 |
| `title.keyword` | 제목 정렬 |
| `title.autocomplete` | prefix 검색 |
| `keywords` | 관련 검색어 추천 |
| `purpose` | 문서 목적 응답 |
| `bookmark` | 즐겨찾기 여부 응답 |
| `deleted` | 삭제 문서 제외 |
| `createdAt`, `updatedAt` | 정렬 기준 |

## 5. 색인 동기화

DB 트랜잭션 내부에서 Elasticsearch를 직접 호출하지 않는다. 문서 변경 작업이 commit된 이후 이벤트를 발행하고, 이벤트 핸들러에서 ES 색인을 처리한다.

~~~mermaid
sequenceDiagram
    participant API as API
    participant APP as Application Facade
    participant DB as PostgreSQL
    participant EVENT as SearchIndexEvent
    participant ES as Elasticsearch

    API->>APP: 문서 생성/수정/삭제
    APP->>DB: 원본 데이터 변경
    DB-->>APP: commit
    APP->>EVENT: afterCommit 이벤트 발행
    EVENT->>ES: index/delete 요청
~~~

| 문서 작업 | ES 처리 |
| --- | --- |
| 생성 | index |
| 수정 | index 갱신 |
| 폴더 이동 | index 갱신 |
| 즐겨찾기 변경 | index 갱신 |
| 삭제 | index 삭제 |

## 6. 장애 대응

| 장애 상황 | 처리 방식 |
| --- | --- |
| ES 검색 실패 | PostgreSQL 검색 fallback |
| ES prefix 검색 실패 | PostgreSQL prefix 검색 fallback |
| ES 추천 검색 실패 | PostgreSQL `pg_trgm` 추천 fallback |
| ES 색인 실패 | 경고 로그 기록 후 API 요청은 성공 유지 |
| ES index 초기화 실패 | 앱 기동은 유지하고 fallback 검색 사용 |

## 7. 설정

기본 설정은 Elasticsearch를 사용하지 않는다.

~~~yaml
app:
  search:
    elasticsearch:
      enabled: false
      base-url: ${ELASTICSEARCH_BASE_URL:http://localhost:9200}
      index-name: ${ELASTICSEARCH_INDEX_NAME:ssd-local-documents}
      request-timeout: 3s
~~~

Elasticsearch를 사용할 환경에서만 `enabled=true`로 전환한다.

## 8. 검증

| 검증 | 결과 |
| --- | --- |
| `:ssd-domain:test` | 검색 port 분리 후 통과 |
| `:ssd-infra:test` | ES fallback 단위 테스트 통과 |
| `:ssd-infra:compileJava` | ES adapter 컴파일 통과 |
| `promtool` 등 외부 검증 | 대상 아님 |

현재 구현은 ES가 없어도 기존 검색 API가 동작하는 구조다. 다음 단계에서는 Testcontainers 기반으로 실제 ES index/search 통합 테스트와 PostgreSQL 대비 성능 비교를 추가한다.
