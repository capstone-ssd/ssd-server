import http from 'k6/http';
import { check, fail, sleep } from 'k6';
import { Counter, Rate, Trend } from 'k6/metrics';

import {
  BASE_URL,
  TARGET_PATH,
  REQUEST_TIMEOUT,
  TOKENS,
  THINK_TIME_SECONDS,
  FOLDER_ID,
} from './config.js';
import { buildCreateDocumentPayload, resolvePayloadSize } from './document_payloads.js';

export const documentCreateDuration = new Trend('document_create_duration', true);
export const documentCreateSuccessRate = new Rate('document_create_success_rate');
export const documentCreateCount = new Counter('document_create_count');

function pickToken() {
  if (TOKENS.length === 0) {
    fail('ACCESS_TOKEN, ACCESS_TOKENS 또는 ACCESS_TOKENS_FILE 환경변수는 필수입니다.');
  }

  return TOKENS[(__VU - 1) % TOKENS.length];
}

function requestParams(tags = {}) {
  return {
    headers: {
      Authorization: `Bearer ${pickToken()}`,
      'Content-Type': 'application/json',
    },
    tags,
    timeout: REQUEST_TIMEOUT,
  };
}

export function requestCreateDocument({ size = 'large', extraTags = {} } = {}) {
  const payloadSize = resolvePayloadSize(size);
  const payload = buildCreateDocumentPayload({
    size: payloadSize,
    folderId: FOLDER_ID,
    titlePrefix: `부하테스트 문서 vu${__VU}`,
  });

  const response = http.post(
    `${BASE_URL}${TARGET_PATH}`,
    JSON.stringify(payload),
    requestParams({ endpoint: TARGET_PATH, payload_size: payloadSize, ...extraTags }),
  );

  documentCreateDuration.add(response.timings.duration, { payload_size: payloadSize, ...extraTags });
  documentCreateCount.add(1, { payload_size: payloadSize, ...extraTags });
  return response;
}

export function checkCreateDocumentResponse(response) {
  const ok = check(response, {
    'status is 200': (r) => r.status === 200,
    'response code is 200': (r) => {
      if (!r.body) {
        return false;
      }

      try {
        return JSON.parse(r.body).code === '200';
      } catch (error) {
        return false;
      }
    },
    'response has document id': (r) => {
      if (!r.body) {
        return false;
      }

      try {
        const body = JSON.parse(r.body);
        return Number.isInteger(body?.data?.id) || typeof body?.data?.id === 'number';
      } catch (error) {
        return false;
      }
    },
  });

  documentCreateSuccessRate.add(ok ? 1 : 0);
  return ok;
}

export function runCreateDocumentIteration({ size = 'large', scenarioName }) {
  const response = requestCreateDocument({
    size,
    extraTags: { scenario: scenarioName },
  });
  checkCreateDocumentResponse(response);
  sleep(THINK_TIME_SECONDS);
}
