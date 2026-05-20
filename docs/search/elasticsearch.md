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
spring:
  elasticsearch:
    uris: ${ELASTICSEARCH_BASE_URL:http://localhost:9200}

app:
  search:
    elasticsearch:
      enabled: false
      index-name: ${ELASTICSEARCH_INDEX_NAME:ssd-local-documents}
~~~

Elasticsearch를 사용할 환경에서만 `enabled=true`로 전환한다.

dev 서버에서는 `develop-cd`가 아래 환경변수를 주입한다.

| 환경변수 | 값 |
| --- | --- |
| `ELASTICSEARCH_ENABLED` | `true` |
| `APP_SEARCH_ELASTICSEARCH_ENABLED` | `true` |
| `SPRING_ELASTICSEARCH_URIS` | `http://ssd-elasticsearch:9200` |
| `APP_SEARCH_ELASTICSEARCH_INDEX_NAME` | `ssd-dev-documents` |

Elasticsearch 컨테이너는 `deploy/ec2/docker-compose.yml`에서 단일 노드로 실행한다.

| 설정 | 값 |
| --- | --- |
| image | `docker.elastic.co/elasticsearch/elasticsearch:9.2.1` |
| network | `ssd-net` |
| host port | `127.0.0.1:9200` |
| internal uri | `http://ssd-elasticsearch:9200` |
| heap | `-Xms256m -Xmx256m` |
| memory limit | `512m` |
| host sysctl | `vm.max_map_count=262144` |

## 8. Spring Data Elasticsearch 적용

이번 구현은 직접 HTTP 요청을 조립하지 않고 Spring Data Elasticsearch를 사용한다.

| 구성 | 역할 |
| --- | --- |
| `ElasticsearchDocument` | `@Document` 기반 ES index document |
| `ElasticsearchDocumentRepository` | `ElasticsearchRepository` 기반 색인 저장/삭제 |
| `ElasticsearchOperations` | `NativeQuery` 기반 검색 query 실행 |
| `ElasticsearchIndexInitializer` | `IndexOperations.createWithMapping()`으로 index 생성 |
| `ElasticsearchRepositoryConfig` | ES 활성화 시 repository scan 범위 지정 |

검색 query는 문자열 JSON을 직접 조립하지 않고 `co.elastic.clients.elasticsearch._types.query_dsl.Query`와 `NativeQuery`로 구성한다.

Spring Boot 4.0.0에서 사용하는 Elasticsearch Java Client는 `9.2.1`이다. 따라서 로컬/Testcontainers/배포 compose의 Elasticsearch image도 `9.2.1`로 맞춘다. 버전이 맞지 않으면 ES media type 호환 문제로 검색 호출이 실패할 수 있다.

## 9. 검증

| 검증 | 결과 |
| --- | --- |
| `:ssd-domain:test` | 검색 port 분리 후 통과 |
| `:ssd-infra:test` | ES fallback 단위 테스트 통과 |
| `:ssd-infra:compileJava` | Spring Data Elasticsearch adapter 컴파일 통과 |
| `:ssd-api:performanceIntegrationTest` | PostgreSQL/ES 검색 성능 비교 통과 |
| `promtool` 등 외부 검증 | 대상 아님 |

현재 구현은 ES가 없어도 기존 검색 API가 동작하는 구조다.

## 10. 로컬 성능 비교 결과

Testcontainers 기반으로 PostgreSQL과 Elasticsearch를 함께 띄우고, 동일한 5만 건 데이터셋을 양쪽에 적재한 뒤 검색 성능을 비교하였다.

| 항목 | 값 |
| --- | --- |
| 실행 일시 | 2026-05-20 |
| 테스트 명령 | `./gradlew :ssd-api:performanceIntegrationTest -DsearchPerfDocs=50000 -DsearchPerfWarmups=10 -DsearchPerfIterations=50 --no-daemon` |
| 문서 수 | 50,000건 |
| 매칭 문서 수 | 50건 |
| 검색어 | `사업계획서` |
| warmup | 10회 |
| 측정 반복 | 50회 |

| 검색 방식 | 결과 수 | 평균 | p95 | p99 |
| --- | ---: | ---: | ---: | ---: |
| PostgreSQL contains | 50 | 13ms | 14ms | 16ms |
| PostgreSQL prefix | 50 | 11ms | 11ms | 14ms |
| Elasticsearch contains | 50 | 6ms | 7ms | 11ms |
| Elasticsearch prefix | 50 | 4ms | 5ms | 6ms |

| 비교 | 평균 개선율 | p95 개선율 | p99 개선율 |
| --- | ---: | ---: | ---: |
| PostgreSQL contains -> PostgreSQL prefix | 15.38% | 21.43% | 12.50% |
| PostgreSQL contains -> Elasticsearch contains | 53.85% | 50.00% | 31.25% |
| PostgreSQL prefix -> Elasticsearch prefix | 63.64% | 54.55% | 57.14% |

~~~mermaid
flowchart LR
    A["동일 데이터셋 50,000건"] --> B["PostgreSQL contains"]
    A --> C["PostgreSQL prefix"]
    A --> D["Elasticsearch contains"]
    A --> E["Elasticsearch prefix"]
    B --> F["avg 13ms"]
    C --> G["avg 11ms"]
    D --> H["avg 6ms"]
    E --> I["avg 4ms"]
~~~

이번 측정에서는 Elasticsearch가 PostgreSQL 검색보다 평균 기준으로 더 빠르게 동작하였다. 다만 이 결과는 로컬 Testcontainers 환경 기준이며, 실제 운영에서는 네트워크 지연, ES heap, index refresh 정책, 데이터 증가량에 따라 결과가 달라질 수 있다.
