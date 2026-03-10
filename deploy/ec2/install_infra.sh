#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
APP_NAME="${APP_NAME:-ssd}"
DEPLOY_USER="${DEPLOY_USER:-${SUDO_USER:-ubuntu}}"
INFRA_DIR="${INFRA_DIR:-/home/${DEPLOY_USER}/infra}"
DEPLOY_ROOT="${DEPLOY_ROOT:-/opt/${APP_NAME}}"
RUNTIME_CONFIG_DIR="${RUNTIME_CONFIG_DIR:-${DEPLOY_ROOT}/config}"
APP_CONFIG_FILE="${APP_CONFIG_FILE:-${RUNTIME_CONFIG_DIR}/application-dev.yml}"
APP_CONFIG_SOURCE="${APP_CONFIG_SOURCE:-}"
NETWORK_NAME="${NETWORK_NAME:-ssd-net}"
NGINX_SERVER_NAME="${NGINX_SERVER_NAME:-dev-api.simsaimdang.shop}"
NGINX_TIMEOUT_FILE="${NGINX_TIMEOUT_FILE:-/etc/nginx/conf.d/zzz-ssd-timeout.conf}"
NGINX_UPSTREAM_FILE="${NGINX_UPSTREAM_FILE:-/etc/nginx/conf.d/ssd-upstream.conf}"
NGINX_SITE_AVAILABLE="${NGINX_SITE_AVAILABLE:-/etc/nginx/sites-available/${NGINX_SERVER_NAME}}"
NGINX_SITE_ENABLED="${NGINX_SITE_ENABLED:-/etc/nginx/sites-enabled/${NGINX_SERVER_NAME}}"

install -d -m 755 -o "${DEPLOY_USER}" -g "${DEPLOY_USER}" "${INFRA_DIR}"
install -d -m 755 "${DEPLOY_ROOT}" "${RUNTIME_CONFIG_DIR}"
install -m 644 "${SCRIPT_DIR}/docker-compose.yml" "${INFRA_DIR}/docker-compose.yml"
chown "${DEPLOY_USER}:${DEPLOY_USER}" "${INFRA_DIR}/docker-compose.yml"

if [[ -n "${APP_CONFIG_SOURCE}" ]]; then
  install -m 600 "${APP_CONFIG_SOURCE}" "${APP_CONFIG_FILE}"
elif [[ ! -f "${APP_CONFIG_FILE}" ]]; then
  echo "[ERROR] application-dev.yml is required at ${APP_CONFIG_FILE}" >&2
  exit 1
fi

if command -v docker >/dev/null 2>&1; then
  docker network inspect "${NETWORK_NAME}" >/dev/null 2>&1 || docker network create "${NETWORK_NAME}"
  docker compose -f "${INFRA_DIR}/docker-compose.yml" up -d --remove-orphans redis
else
  echo "[ERROR] docker is not installed" >&2
  exit 1
fi

if command -v nginx >/dev/null 2>&1; then
  install -d -m 755 /etc/nginx/conf.d /etc/nginx/sites-available /etc/nginx/sites-enabled
  install -m 644 "${SCRIPT_DIR}/nginx/zzz-ssd-timeout.conf" "${NGINX_TIMEOUT_FILE}"
  sed "s#__SERVER_NAME__#${NGINX_SERVER_NAME}#g" "${SCRIPT_DIR}/nginx/site.conf.template" > "${NGINX_SITE_AVAILABLE}"
  ln -sfn "${NGINX_SITE_AVAILABLE}" "${NGINX_SITE_ENABLED}"

  if [[ ! -f "${NGINX_UPSTREAM_FILE}" ]]; then
    cat > "${NGINX_UPSTREAM_FILE}" <<EOF_UPSTREAM
upstream ssd_backend {
    server 127.0.0.1:8080;
    keepalive 32;
}
EOF_UPSTREAM
  fi

  nginx -t
  systemctl reload nginx
else
  echo "[ERROR] nginx is not installed" >&2
  exit 1
fi

echo "[INFO] Infra install complete"
