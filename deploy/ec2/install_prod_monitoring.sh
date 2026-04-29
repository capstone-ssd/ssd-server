#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
APP_NAME="${APP_NAME:-ssd}"
DEPLOY_USER="${DEPLOY_USER:-${SUDO_USER:-ubuntu}}"
MONITORING_SOURCE_DIR="${MONITORING_SOURCE_DIR:-${SCRIPT_DIR}/monitoring}"
MONITORING_DIR="${MONITORING_DIR:-/opt/${APP_NAME}/monitoring-prod}"
APP_CONFIG_FILE="${APP_CONFIG_FILE:-/opt/${APP_NAME}/config/application-dev.yml}"
DISCORD_WEBHOOK_FILE="${DISCORD_WEBHOOK_FILE:-${MONITORING_DIR}/alertmanager/secrets/discord_webhook_url}"
ENV_FILE="${ENV_FILE:-${MONITORING_DIR}/.env}"

if [[ ! -d "${MONITORING_SOURCE_DIR}" ]]; then
  echo "[ERROR] monitoring source directory not found: ${MONITORING_SOURCE_DIR}" >&2
  exit 1
fi

install -d -m 755 -o "${DEPLOY_USER}" -g "${DEPLOY_USER}" "${MONITORING_DIR}"

# Copy versioned monitoring files while preserving runtime-only secrets and Grafana credentials.
tmp_dir="$(mktemp -d)"
trap 'rm -rf "${tmp_dir}"' EXIT
cp -a "${MONITORING_SOURCE_DIR}/." "${tmp_dir}/"
rm -f "${tmp_dir}/.env" "${tmp_dir}/alertmanager/secrets/discord_webhook_url"
cp -a "${tmp_dir}/." "${MONITORING_DIR}/"
chown -R "${DEPLOY_USER}:${DEPLOY_USER}" "${MONITORING_DIR}"

install -d -m 700 -o "${DEPLOY_USER}" -g "${DEPLOY_USER}" "$(dirname "${DISCORD_WEBHOOK_FILE}")"
if [[ ! -s "${DISCORD_WEBHOOK_FILE}" ]]; then
  if [[ ! -f "${APP_CONFIG_FILE}" ]]; then
    echo "[ERROR] application config not found: ${APP_CONFIG_FILE}" >&2
    exit 1
  fi

  webhook_url="$(awk '
    /^discord:/ {in_discord=1; next}
    in_discord && /^[^[:space:]]/ {in_discord=0}
    in_discord && /webhook-url:/ {sub(/^[[:space:]]*webhook-url:[[:space:]]*/, ""); print; exit}
  ' "${APP_CONFIG_FILE}" | tr -d '"' | tr -d "'")"

  if [[ -z "${webhook_url}" ]]; then
    echo "[ERROR] discord.webhook-url not found in ${APP_CONFIG_FILE}" >&2
    exit 1
  fi

  printf '%s' "${webhook_url}" > "${DISCORD_WEBHOOK_FILE}"
  chmod 600 "${DISCORD_WEBHOOK_FILE}"
  chown "${DEPLOY_USER}:${DEPLOY_USER}" "${DISCORD_WEBHOOK_FILE}"
fi

if [[ ! -f "${ENV_FILE}" ]]; then
  grafana_password="$(openssl rand -base64 24 | tr -d '\n')"
  {
    echo 'GRAFANA_ADMIN_USER=admin'
    echo "GRAFANA_ADMIN_PASSWORD=${grafana_password}"
  } > "${ENV_FILE}"
  chmod 600 "${ENV_FILE}"
  chown "${DEPLOY_USER}:${DEPLOY_USER}" "${ENV_FILE}"
fi

cd "${MONITORING_DIR}"
docker compose -f docker-compose.monitoring.yml config >/tmp/ssd-prod-monitoring-compose.config
docker compose -f docker-compose.monitoring.yml up -d

echo "[INFO] SSD production monitoring is running"
echo "[INFO] Grafana: ssh -L 3001:127.0.0.1:3001 <server> then open http://localhost:3001"
echo "[INFO] Prometheus: ssh -L 9091:127.0.0.1:9091 <server> then open http://localhost:9091"
echo "[INFO] Alertmanager: ssh -L 9093:127.0.0.1:9093 <server> then open http://localhost:9093"
