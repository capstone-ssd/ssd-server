import { runCreateDocumentIteration } from '../../lib/common.js';
import { TARGET_ERROR_RATE, TARGET_P95_MS, TARGET_P99_MS } from '../../lib/config.js';
import { buildSummary } from '../../lib/summary.js';

const SCENARIO_NAME = 'document-create-target';

export const options = {
  scenarios: {
    target: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: __ENV.TARGET_STAGE_1_DURATION || '2m', target: Number(__ENV.TARGET_STAGE_1_VUS || 10) },
        { duration: __ENV.TARGET_STAGE_2_DURATION || '3m', target: Number(__ENV.TARGET_STAGE_2_VUS || 50) },
        { duration: __ENV.TARGET_STAGE_3_DURATION || '3m', target: Number(__ENV.TARGET_STAGE_3_VUS || 100) },
        { duration: __ENV.TARGET_STAGE_4_DURATION || '10m', target: Number(__ENV.TARGET_STAGE_4_VUS || 150) },
        { duration: __ENV.TARGET_STAGE_5_DURATION || '2m', target: 0 },
      ],
      gracefulRampDown: '30s',
    },
  },
  thresholds: {
    http_req_failed: [`rate<${TARGET_ERROR_RATE}`],
    http_req_duration: [`p(95)<${TARGET_P95_MS}`, `p(99)<${TARGET_P99_MS}`],
    document_create_success_rate: ['rate>0.99'],
  },
};

export default function () {
  runCreateDocumentIteration({ size: __ENV.PAYLOAD_SIZE || 'large', scenarioName: SCENARIO_NAME });
}

export function handleSummary(data) {
  return buildSummary(SCENARIO_NAME, data);
}
