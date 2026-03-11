import { runCreateDocumentIteration } from '../../lib/common.js';
import { buildSummary } from '../../lib/summary.js';

const SCENARIO_NAME = 'document-create-payload-curve';

export const options = {
  scenarios: {
    payload_small: {
      executor: 'constant-vus',
      exec: 'payloadSmall',
      vus: Number(__ENV.CURVE_VUS || 10),
      duration: __ENV.CURVE_DURATION || '3m',
      startTime: '0s',
      gracefulStop: '10s',
    },
    payload_medium: {
      executor: 'constant-vus',
      exec: 'payloadMedium',
      vus: Number(__ENV.CURVE_VUS || 10),
      duration: __ENV.CURVE_DURATION || '3m',
      startTime: __ENV.CURVE_MEDIUM_START || '4m',
      gracefulStop: '10s',
    },
    payload_large: {
      executor: 'constant-vus',
      exec: 'payloadLarge',
      vus: Number(__ENV.CURVE_VUS || 10),
      duration: __ENV.CURVE_DURATION || '3m',
      startTime: __ENV.CURVE_LARGE_START || '8m',
      gracefulStop: '10s',
    },
    payload_xlarge: {
      executor: 'constant-vus',
      exec: 'payloadXLarge',
      vus: Number(__ENV.CURVE_VUS || 10),
      duration: __ENV.CURVE_DURATION || '3m',
      startTime: __ENV.CURVE_XLARGE_START || '12m',
      gracefulStop: '10s',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    'http_req_duration{scenario:document-create-curve-small}': ['p(95)<1200'],
    'http_req_duration{scenario:document-create-curve-medium}': ['p(95)<1800'],
    'http_req_duration{scenario:document-create-curve-large}': ['p(95)<2500'],
    'http_req_duration{scenario:document-create-curve-xlarge}': ['p(95)<4000'],
  },
};

export function payloadSmall() {
  runCreateDocumentIteration({ size: 'small', scenarioName: 'document-create-curve-small' });
}

export function payloadMedium() {
  runCreateDocumentIteration({ size: 'medium', scenarioName: 'document-create-curve-medium' });
}

export function payloadLarge() {
  runCreateDocumentIteration({ size: 'large', scenarioName: 'document-create-curve-large' });
}

export function payloadXLarge() {
  runCreateDocumentIteration({ size: 'xlarge', scenarioName: 'document-create-curve-xlarge' });
}

export function handleSummary(data) {
  return buildSummary(SCENARIO_NAME, data);
}
