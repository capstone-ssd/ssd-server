import { runCreateDocumentIteration } from '../../lib/common.js';
import { buildSummary } from '../../lib/summary.js';

const SCENARIO_NAME = 'document-create-stress';

export const options = {
  scenarios: {
    stress: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: __ENV.STRESS_STAGE_1_DURATION || '2m', target: Number(__ENV.STRESS_STAGE_1_VUS || 150) },
        { duration: __ENV.STRESS_STAGE_2_DURATION || '3m', target: Number(__ENV.STRESS_STAGE_2_VUS || 200) },
        { duration: __ENV.STRESS_STAGE_3_DURATION || '3m', target: Number(__ENV.STRESS_STAGE_3_VUS || 250) },
        { duration: __ENV.STRESS_STAGE_4_DURATION || '3m', target: Number(__ENV.STRESS_STAGE_4_VUS || 300) },
        { duration: __ENV.STRESS_STAGE_5_DURATION || '2m', target: 0 },
      ],
      gracefulRampDown: '30s',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(95)<5000'],
  },
};

export default function () {
  runCreateDocumentIteration({ size: __ENV.PAYLOAD_SIZE || 'large', scenarioName: SCENARIO_NAME });
}

export function handleSummary(data) {
  return buildSummary(SCENARIO_NAME, data);
}
