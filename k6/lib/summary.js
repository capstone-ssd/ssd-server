import { RESULTS_DIR, RUN_LABEL } from './config.js';

function runId() {
  return new Date().toISOString().replace(/[:.]/g, '-');
}

export function buildSummary(scenarioName, data) {
  const id = runId();
  const prefix = `${RESULTS_DIR}/${scenarioName}-${RUN_LABEL}-${id}`;
  const summary = {
    scenarioName,
    runLabel: RUN_LABEL,
    metrics: data.metrics,
    rootGroup: data.root_group,
    state: data.state,
  };

  const lines = [
    `scenario=${scenarioName}`,
    `runLabel=${RUN_LABEL}`,
    `iterations=${data.metrics.iterations?.count ?? 0}`,
    `http_req_failed=${data.metrics.http_req_failed?.rate ?? 'n/a'}`,
    `http_req_duration_p95=${data.metrics.http_req_duration?.['p(95)'] ?? 'n/a'}`,
    `http_req_duration_p99=${data.metrics.http_req_duration?.['p(99)'] ?? 'n/a'}`,
    `vus_max=${data.metrics.vus_max?.value ?? 'n/a'}`,
  ].join('\n');

  return {
    [`${prefix}.json`]: JSON.stringify(summary, null, 2),
    [`${prefix}.txt`]: `${lines}\n`,
    stdout: `${lines}\n`,
  };
}
