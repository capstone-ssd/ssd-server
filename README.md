# SSD Server

SSD Server는 사업계획서 문서를 생성, 수정, 분석하고 AI 기반 요약, 평가, 체크리스트 피드백을 제공하는 백엔드 서버입니다.


<br>

## 목차

1. [프로젝트 소개](#프로젝트-소개)
2. [기술 스택](#기술-스택)
3. [시스템 아키텍처](#시스템-아키텍처)
4. [멀티모듈 구조](#멀티모듈-구조)
5. [ERD](#erd)
6. [로컬 실행 방법](#로컬-실행-방법)

<br>

## 프로젝트 소개

SSD는 사업계획서 작성 과정에서 사용자가 문서를 관리하고, AI를 통해 문서의 요약, 키워드 추출, 평가, 체크리스트 기반 피드백을 받을 수 있도록 지원하는 서비스입니다.

백엔드 서버는 문서, 폴더, 회원, 인증, AI 분석, 파일 저장소, 알림 연동을 담당합니다. 외부 AI 서버, PostgreSQL, Redis, S3 등 변경 가능성이 높은 기술 요소가 도메인 로직에 직접 침투하지 않도록 모듈 경계를 분리했습니다.

<br>

## 기술 스택

| 구분 | 기술 |
| --- | --- |
| Language | Java 21 |
| Build | Gradle |
| Framework | Spring Boot 4.0.0 |
| Web | Spring MVC |
| Security | Spring Security, JWT, OAuth |
| Persistence | Spring Data JPA, PostgreSQL |
| Cache / Token Store | Redis |
| External Client | OpenFeign, Spring AI |
| API Docs | springdoc-openapi, Swagger UI |
| Infra | Docker, GitHub Actions |
| Monitoring | Actuator, Prometheus, Grafana |

<br>

## 시스템 아키텍처

~~~mermaid
flowchart LR
    Client["Client"] --> API["ssd-api\nREST API / Security"]
    API --> APP["ssd-application\nFacade / Transaction Boundary"]
    APP --> DOMAIN["ssd-domain\nUsecase / Domain Logic"]
    DOMAIN --> REPO_PORT["Repository Port"]
    REPO_PORT --> INFRA["ssd-infra\nJPA Adapter / Entity"]
    INFRA --> DB[("PostgreSQL")]
    DOMAIN --> EXT_PORT["External Port"]
    EXT_PORT --> EXTERNAL["ssd-external\nAI / S3 / Discord Client"]
    EXTERNAL --> AI["AI Server"]
    EXTERNAL --> S3["S3"]
    EXTERNAL --> DISCORD["Discord"]
    API --> AUTH["ssd-auth\nJWT / OAuth / Principal"]
    AUTH --> REDIS[("Redis")]
~~~

요청은 `ssd-api`에서 HTTP 요청/응답으로 처리되고, 실제 유스케이스 실행은 `ssd-application`의 facade를 통해 시작됩니다. `ssd-application`은 트랜잭션 경계를 관리하고, `ssd-domain`은 비즈니스 규칙과 유스케이스 로직을 담당합니다.

JPA Entity와 Spring Data JPA 구현체는 `ssd-infra`에 위치합니다. 외부 AI 서버, S3, Discord 등 외부 시스템 연동은 `ssd-external`에서 담당합니다.

<br>

## 멀티모듈 구조

~~~text
ssd-server
├── ssd-api
├── ssd-application
├── ssd-domain
├── ssd-infra
├── ssd-auth
├── ssd-external
└── ssd-common
~~~

| 모듈 | 책임 |
| --- | --- |
| `ssd-api` | Spring Boot 애플리케이션 엔트리포인트, REST Controller, 요청/응답 DTO, Security 설정 |
| `ssd-application` | 유스케이스 facade, 트랜잭션 경계 관리 |
| `ssd-domain` | 도메인 모델, 비즈니스 로직, 도메인 Repository 인터페이스, 외부 연동 Port |
| `ssd-infra` | JPA Entity, Spring Data JPA Repository, 도메인 Repository 구현체 |
| `ssd-auth` | JWT, OAuth, 인증 principal, 인증 상태 저장소 |
| `ssd-external` | 외부 AI 서버, S3, Discord 등 외부 시스템 연동 |
| `ssd-common` | 공통 API 응답, 공통 예외, 공통 유틸리티 |

### 의존성 방향

~~~mermaid
flowchart LR
    API["ssd-api"] --> APPLICATION["ssd-application"]
    API --> DOMAIN["ssd-domain"]
    API --> AUTH["ssd-auth"]
    API --> EXTERNAL["ssd-external"]
    API --> INFRA["ssd-infra"]
    API --> COMMON["ssd-common"]

    APPLICATION --> DOMAIN
    APPLICATION --> COMMON

    DOMAIN --> COMMON
    AUTH --> DOMAIN
    AUTH --> COMMON
    EXTERNAL --> COMMON
    INFRA --> DOMAIN
    INFRA --> COMMON
    INFRA --> EXTERNAL
~~~

`ssd-domain`은 구현체가 아니라 인터페이스와 도메인 모델을 중심으로 동작합니다. 
트랜잭션은 `ssd-application`에서 관리하고, JPA 구현체는 `ssd-infra`에 위치시켜 도메인 로직이 영속성 구현 방식에 직접 의존하지 않도록 구성했습니다.

<br>

## ERD

~~~mermaid
erDiagram
    MEMBERS ||--o{ DOCUMENTS : owns
    MEMBERS ||--o{ FOLDERS : owns
    MEMBERS ||--o{ EVALUATOR_REVIEWS : writes

    FOLDERS ||--o{ FOLDERS : contains
    FOLDERS ||--o{ DOCUMENTS : contains

    DOCUMENTS ||--o{ DOCUMENT_PARAGRAPHS : has
    DOCUMENTS ||--o{ DOCUMENT_COMMENTS : has
    DOCUMENTS ||--o{ DOCUMENT_LOGS : has
    DOCUMENTS ||--o{ EVALUATOR_REVIEWS : receives
    DOCUMENTS ||--o{ CHECK_LISTS : has
    DOCUMENTS ||--o{ EVALUATOR_CHECK_LISTS : has
    DOCUMENTS ||--o{ DOCUMENT_AI_CHECK_SNAPSHOTS : has

    MEMBERS {
        bigint id PK
        varchar email
        varchar nickname
        varchar profile_image_url
        varchar role
        datetime created_at
        datetime updated_at
    }

    FOLDERS {
        bigint id PK
        bigint member_id FK
        bigint parent_id FK
        varchar name
        datetime created_at
        datetime updated_at
    }

    DOCUMENTS {
        bigint id PK
        bigint member_id FK
        bigint folder_id FK
        varchar title
        text content
        boolean bookmark
        varchar purpose
        int version
        datetime created_at
        datetime updated_at
    }

    DOCUMENT_PARAGRAPHS {
        bigint id PK
        bigint document_id FK
        int block_id
        varchar block_type
        text content
        datetime created_at
        datetime updated_at
    }

    DOCUMENT_COMMENTS {
        bigint id PK
        bigint document_id FK
        bigint member_id FK
        int block_id
        text content
        datetime created_at
        datetime updated_at
    }

    DOCUMENT_LOGS {
        bigint id PK
        bigint document_id FK
        bigint member_id FK
        varchar action_type
        int deleted_block_count
        int created_block_count
        datetime created_at
        datetime updated_at
    }

    EVALUATOR_REVIEWS {
        bigint id PK
        bigint document_id FK
        bigint member_id FK
        int score
        text content
        datetime created_at
        datetime updated_at
    }

    CHECK_LISTS {
        bigint id PK
        bigint document_id FK
        text content
        boolean checked
        datetime created_at
        datetime updated_at
    }

    EVALUATOR_CHECK_LISTS {
        bigint id PK
        bigint document_id FK
        text content
        boolean checked
        datetime created_at
        datetime updated_at
    }

    DOCUMENT_AI_CHECK_SNAPSHOTS {
        bigint id PK
        bigint document_id FK
        int block_id
        text content
        datetime created_at
        datetime updated_at
    }
~~~


