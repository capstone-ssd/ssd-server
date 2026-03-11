import { runCreateDocumentIteration } from '../../lib/common.js';
import { buildSummary } from '../../lib/summary.js';

const SCENARIO_NAME = 'document-create-smoke';

export const options = {
  scenarios: {
    smoke: {
      executor: 'constant-vus',
      vus: Number(__ENV.SMOKE_VUS || 3),
      duration: __ENV.SMOKE_DURATION || '2m',
      gracefulStop: '10s',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<2000'],
    document_create_success_rate: ['rate>0.99'],
  },
};

export default function () {
  runCreateDocumentIteration({ size: __ENV.PAYLOAD_SIZE || 'large', scenarioName: SCENARIO_NAME });
}

export function handleSummary(data) {
  return buildSummary(SCENARIO_NAME, data);
}
