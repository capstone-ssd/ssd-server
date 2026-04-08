# SSD Naming Wiki

## 목적
- 이 문서는 SSD 저장소에서 사용하는 네이밍 기준의 정본이다.
- GitHub Wiki를 별도로 운영하더라도, 실제 리뷰와 변경 기준은 저장소 내부 문서를 우선한다.
- 도메인 용어, 클래스/메서드 명명 규칙, 금지 단어, 변경 결정 이력을 한 곳에서 관리한다.

## 문서 구성
- `/Users/jeonjaeyeon/Desktop/capstone/ssd/docs/naming/glossary.md`
  - 도메인 용어 사전
  - 같은 개념을 어떤 단어로 고정할지 정의한다.
- `/Users/jeonjaeyeon/Desktop/capstone/ssd/docs/naming/patterns.md`
  - 클래스, 메서드, DTO, boolean, port/adapter 이름 패턴을 정의한다.
- `/Users/jeonjaeyeon/Desktop/capstone/ssd/docs/naming/banned-words.md`
  - 의미가 흐린 단어와 지양 표현을 관리한다.
- `/Users/jeonjaeyeon/Desktop/capstone/ssd/docs/naming/decision-log.md`
  - 용어 충돌이나 naming 결정의 근거를 남긴다.
- `/Users/jeonjaeyeon/Desktop/capstone/ssd/docs/naming/rename-mapping.md`
  - 현재 이름과 목표 이름, 후속 이슈를 연결한다.

## 운영 원칙
1. 새 도메인 용어를 도입하면 `glossary.md`를 먼저 확인하고, 없으면 같은 PR에서 추가한다.
2. 같은 개념에는 같은 단어를 쓴다. 예외가 필요하면 `decision-log.md`에 남긴다.
3. 대규모 rename은 별도 구조 이슈와 함께 진행하고, 사전 작업은 `rename-mapping.md`에 기록한다.
4. 이름이 길어지는 것보다 의미가 흐려지는 것이 더 큰 문제다. 축약은 검색성과 문맥이 유지될 때만 허용한다.
5. 저장소 내부 문서와 실제 코드가 충돌하면 코드를 우선 확인하고, 문서를 즉시 갱신한다.

## 변경 절차
1. 변경하려는 이름이 도메인 용어인지, 구현 세부 이름인지 구분한다.
2. 도메인 용어면 `glossary.md`와 `decision-log.md`를 먼저 갱신한다.
3. 실제 rename이 필요한 경우 `rename-mapping.md`에 후속 이슈와 함께 기록한다.
4. 코드 변경 PR에는 PR 템플릿 체크리스트를 통해 문서 반영 여부를 함께 남긴다.

## 현재 상태
- 이 문서 구조는 `#136`에서 생성되었다.
- 핵심 용어 정의와 rename 매핑의 실질 내용은 `#137` 이후 이슈에서 순차적으로 채운다.
