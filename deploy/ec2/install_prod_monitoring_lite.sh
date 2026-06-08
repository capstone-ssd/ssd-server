#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
APP_NAME="${APP_NAME:-ssd}"
DEPLOY_USER="${DEPLOY_USER:-${SUDO_USER:-ubuntu}}"
MONITORING_SOURCE_DIR="${MONITORING_SOURCE_DIR:-${SCRIPT_DIR}/monitoring-lite}"
MONITORING_SHARED_SOURCE_DIR="${MONITORING_SHARED_SOURCE_DIR:-${SCRIPT_DIR}/monitoring}"
MONITORING_DIR="${MONITORING_DIR:-/opt/${APP_NAME}/monitoring-prod-lite}"
APP_CONFIG_FILE="${APP_CONFIG_FILE:-/opt/${APP_NAME}/config/application-dev.yml}"
DISCORD_WEBHOOK_FILE="${DISCORD_WEBHOOK_FILE:-${MONITORING_DIR}/alertmanager/secrets/discord_webhook_url}"
ENV_FILE="${ENV_FILE:-${MONITORING_DIR}/.env}"

install -d -m 755 -o "${DEPLOY_USER}" -g "${DEPLOY_USER}" "${MONITORING_DIR}"

tmp_dir="$(mktemp -d)"
trap 'rm -rf "${tmp_dir}"' EXIT
cp -a "${MONITORING_SOURCE_DIR}/." "${tmp_dir}/"
if [[ -d "${MONITORING_SHARED_SOURCE_DIR}" ]]; then
  cp -a "${MONITORING_SHARED_SOURCE_DIR}" "${tmp_dir}/../monitoring"
fi
rm -f "${tmp_dir}/.env" "${tmp_dir}/alertmanager/secrets/discord_webhook_url"
cp -a "${tmp_dir}/." "${MONITORING_DIR}/"
if [[ -d "${tmp_dir}/../monitoring" ]]; then
  shared_monitoring_dir="$(dirname "${MONITORING_DIR}")/monitoring"
  install -d -m 755 -o "${DEPLOY_USER}" -g "${DEPLOY_USER}" "${shared_monitoring_dir}"
  cp -a "${tmp_dir}/../monitoring/." "${shared_monitoring_dir}/"
fi
chown -R "${DEPLOY_USER}:${DEPLOY_USER}" "${MONITORING_DIR}"

install -d -m 750 -o "${DEPLOY_USER}" -g 65534 "$(dirname "${DISCORD_WEBHOOK_FILE}")"
if [[ ! -s "${DISCORD_WEBHOOK_FILE}" ]]; then
  webhook_url="$(awk '
    /^discord:/ {in_discord=1; next}
    in_discord && /^[^[:space:]]/ {in_discord=0}
    in_discord && /webhook-url:/ {sub(/^[[:space:]]*webhook-url:[[:space:]]*/, ""); print; exit}
  ' "${APP_CONFIG_FILE}" | tr -d '"' | tr -d "'")"
  if [[ -z "${webhook_url//[[:space:]]/}" ]]; then
    echo "[ERROR] Discord webhook URL을 찾지 못했습니다: ${APP_CONFIG_FILE}" >&2
    exit 1
  fi
  printf '%s' "${webhook_url}" > "${DISCORD_WEBHOOK_FILE}"
  chmod 600 "${DISCORD_WEBHOOK_FILE}"
fi

chown "${DEPLOY_USER}:65534" "$(dirname "${DISCORD_WEBHOOK_FILE}")"
chmod 750 "$(dirname "${DISCORD_WEBHOOK_FILE}")"
chown 65534:65534 "${DISCORD_WEBHOOK_FILE}"
chmod 600 "${DISCORD_WEBHOOK_FILE}"

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
docker compose -f docker-compose.monitoring-lite.yml config >/dev/null
docker compose -f docker-compose.monitoring-lite.yml up -d

echo "[INFO] SSD lightweight monitoring is running"
