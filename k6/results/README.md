# k6 결과 구조

## 목적
- 시나리오별 원본 산출물과 사람이 읽는 보고서를 같은 루트 아래에서 관리합니다.
- 결과 파일을 `document-create`, `external-ai`처럼 테스트 주제별로 나눕니다.

## 구조
- `k6/results/document-create`
  - 문서 생성 API 부하테스트 결과
- `k6/results/external-ai`
  - 외부 AI 연동 API 부하테스트 결과

## 현재 배치
- `k6/results/document-create/1st-test/report.md`
  - 문서 생성 API 1차 테스트 요약 보고서
- `k6/results/document-create/1st-test/smoke`
  - smoke 시나리오 원본 결과
- `k6/results/document-create/1st-test/payload-curve`
  - payload curve 시나리오 원본 결과
- `k6/results/document-create/1st-test/target`
  - target 시나리오 원본 결과
- `k6/results/external-ai/canary`
  - 외부 AI canary 시나리오 원본 결과

## 원칙
- 보고서는 `report.md`
- k6 요약 산출물은 `result.json`, `result.txt`
- 수동 실행 메모나 전후 기록은 `start.txt`, `end.txt`
