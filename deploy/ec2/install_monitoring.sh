#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
APP_NAME="${APP_NAME:-ssd}"
MONITORING_ROOT="${MONITORING_ROOT:-/opt/${APP_NAME}/monitoring}"
MONITORING_COMPOSE_FILE="${MONITORING_ROOT}/docker-compose.monitoring.yml"
GRAFANA_PORT="${GRAFANA_PORT:-3000}"
PROMETHEUS_PORT="${PROMETHEUS_PORT:-9090}"
K6_DASHBOARD_PORT="${K6_DASHBOARD_PORT:-5665}"
LOADTEST_MONITORING_ENABLED="${LOADTEST_MONITORING_ENABLED:-false}"

copy_if_changed() {
  local source_file="$1"
  local target_file="$2"
  local file_mode="$3"

  if [[ -f "${target_file}" ]] && cmp -s "${source_file}" "${target_file}"; then
    return 1
  fi

  install -m "${file_mode}" "${source_file}" "${target_file}"
  return 0
}

container_running() {
  local container_name="$1"
  [[ "$(docker inspect -f '{{.State.Running}}' "${container_name}" 2>/dev/null || true)" == "true" ]]
}

is_enabled() {
  case "${1,,}" in
    true|1|yes|y|on)
      return 0
      ;;
    false|0|no|n|off)
      return 1
      ;;
    *)
      echo "[ERROR] LOADTEST_MONITORING_ENABLED must be true or false: ${1}" >&2
      exit 1
      ;;
  esac
}

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

monitoring_changed=0

if copy_if_changed "${MONITORING_SOURCE_DIR}/docker-compose.monitoring.yml" "${MONITORING_COMPOSE_FILE}" 644; then
  monitoring_changed=1
fi
if copy_if_changed "${MONITORING_SOURCE_DIR}/prometheus/prometheus.yml" "${MONITORING_ROOT}/prometheus/prometheus.yml" 644; then
  monitoring_changed=1
fi
if copy_if_changed "${MONITORING_SOURCE_DIR}/grafana/provisioning/datasources/prometheus.yml" "${MONITORING_ROOT}/grafana/provisioning/datasources/prometheus.yml" 644; then
  monitoring_changed=1
fi
if copy_if_changed "${MONITORING_SOURCE_DIR}/grafana/provisioning/dashboards/dashboard.yml" "${MONITORING_ROOT}/grafana/provisioning/dashboards/dashboard.yml" 644; then
  monitoring_changed=1
fi
while IFS= read -r -d '' dashboard_file; do
  target_file="${MONITORING_ROOT}/grafana/dashboards/$(basename "${dashboard_file}")"
  if copy_if_changed "${dashboard_file}" "${target_file}" 644; then
    monitoring_changed=1
  fi
done < <(find "${MONITORING_SOURCE_DIR}/grafana/dashboards" -maxdepth 1 -type f -name '*.json' -print0)

if command -v docker >/dev/null 2>&1; then
  if is_enabled "${LOADTEST_MONITORING_ENABLED}"; then
    if (( monitoring_changed == 1 )) \
      || ! container_running "ssd-loadtest-prometheus" \
      || ! container_running "ssd-loadtest-grafana" \
      || ! container_running "ssd-loadtest-node-exporter" \
      || ! container_running "ssd-loadtest-cadvisor"; then
      docker compose -f "${MONITORING_COMPOSE_FILE}" up -d
    else
      echo "[INFO] Loadtest monitoring unchanged, skip compose up"
    fi
  else
    echo "[INFO] Loadtest monitoring disabled, stop containers if they exist"
    docker compose -f "${MONITORING_COMPOSE_FILE}" down --remove-orphans >/dev/null 2>&1 || true
  fi
else
  echo "[ERROR] docker is not installed" >&2
  exit 1
fi

if is_enabled "${LOADTEST_MONITORING_ENABLED}" && command -v ufw >/dev/null 2>&1; then
  if ufw status | grep -q "Status: active"; then
    ufw allow "${GRAFANA_PORT}/tcp" >/dev/null 2>&1 || true
    ufw allow "${PROMETHEUS_PORT}/tcp" >/dev/null 2>&1 || true
    ufw allow "${K6_DASHBOARD_PORT}/tcp" >/dev/null 2>&1 || true
  fi
fi

if is_enabled "${LOADTEST_MONITORING_ENABLED}"; then
  echo "[INFO] Loadtest monitoring install complete"
  echo "[INFO] Grafana:    http://<server>:${GRAFANA_PORT}"
  echo "[INFO] Prometheus: http://<server>:${PROMETHEUS_PORT}"
  echo "[INFO] k6 Web UI:  http://<server>:${K6_DASHBOARD_PORT}"
else
  echo "[INFO] Loadtest monitoring is disabled"
  echo "[INFO] Set LOADTEST_MONITORING_ENABLED=true to start it"
fi
