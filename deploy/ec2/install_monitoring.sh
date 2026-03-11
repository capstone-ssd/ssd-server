#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
APP_NAME="${APP_NAME:-ssd}"
MONITORING_ROOT="${MONITORING_ROOT:-/opt/${APP_NAME}/monitoring}"
MONITORING_COMPOSE_FILE="${MONITORING_ROOT}/docker-compose.monitoring.yml"
GRAFANA_PORT="${GRAFANA_PORT:-3000}"
PROMETHEUS_PORT="${PROMETHEUS_PORT:-9090}"
K6_DASHBOARD_PORT="${K6_DASHBOARD_PORT:-5665}"

resolve_monitoring_source_dir() {
  local candidates=()

  if [[ -n "${MONITORING_SOURCE_DIR:-}" ]]; then
    candidates+=("${MONITORING_SOURCE_DIR}")
  fi

  candidates+=(
    "${SCRIPT_DIR}/../../k6/monitoring"
    "/tmp/k6/monitoring"
    "/tmp/monitoring"
  )

  for candidate in "${candidates[@]}"; do
    if [[ -d "${candidate}" ]]; then
      printf '%s\n' "${candidate}"
      return 0
    fi
  done

  return 1
}

MONITORING_SOURCE_DIR="$(resolve_monitoring_source_dir || true)"
if [[ -z "${MONITORING_SOURCE_DIR}" ]]; then
  echo "[ERROR] monitoring source dir not found" >&2
  exit 1
fi

install -d -m 755 \
  "${MONITORING_ROOT}" \
  "${MONITORING_ROOT}/prometheus" \
  "${MONITORING_ROOT}/grafana/provisioning/datasources" \
  "${MONITORING_ROOT}/grafana/provisioning/dashboards" \
  "${MONITORING_ROOT}/grafana/dashboards"

install -m 644 "${MONITORING_SOURCE_DIR}/docker-compose.monitoring.yml" "${MONITORING_COMPOSE_FILE}"
install -m 644 "${MONITORING_SOURCE_DIR}/prometheus/prometheus.yml" "${MONITORING_ROOT}/prometheus/prometheus.yml"
install -m 644 "${MONITORING_SOURCE_DIR}/grafana/provisioning/datasources/prometheus.yml" "${MONITORING_ROOT}/grafana/provisioning/datasources/prometheus.yml"
install -m 644 "${MONITORING_SOURCE_DIR}/grafana/provisioning/dashboards/dashboard.yml" "${MONITORING_ROOT}/grafana/provisioning/dashboards/dashboard.yml"
find "${MONITORING_SOURCE_DIR}/grafana/dashboards" -maxdepth 1 -type f -name '*.json' -print0 | \
  while IFS= read -r -d '' dashboard_file; do
    install -m 644 "${dashboard_file}" "${MONITORING_ROOT}/grafana/dashboards/$(basename "${dashboard_file}")"
  done

if command -v docker >/dev/null 2>&1; then
  docker compose -f "${MONITORING_COMPOSE_FILE}" up -d
else
  echo "[ERROR] docker is not installed" >&2
  exit 1
fi

if command -v ufw >/dev/null 2>&1; then
  if ufw status | grep -q "Status: active"; then
    ufw allow "${GRAFANA_PORT}/tcp" >/dev/null 2>&1 || true
    ufw allow "${PROMETHEUS_PORT}/tcp" >/dev/null 2>&1 || true
    ufw allow "${K6_DASHBOARD_PORT}/tcp" >/dev/null 2>&1 || true
  fi
fi

echo "[INFO] Monitoring install complete"
echo "[INFO] Grafana:    http://<server>:${GRAFANA_PORT}"
echo "[INFO] Prometheus: http://<server>:${PROMETHEUS_PORT}"
echo "[INFO] k6 Web UI:  http://<server>:${K6_DASHBOARD_PORT}"
