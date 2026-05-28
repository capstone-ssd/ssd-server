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
LEGACY_MOCK_CONTAINER_NAME="${LEGACY_MOCK_CONTAINER_NAME:-external-ai-mock}"

copy_if_changed() {
  local source_file="$1"
  local target_file="$2"
  local file_mode="$3"
  local owner="${4:-}"

  if [[ -f "${target_file}" ]] && cmp -s "${source_file}" "${target_file}"; then
    if [[ -n "${owner}" ]]; then
      chown "${owner}" "${target_file}"
    fi
    return 1
  fi

  install -m "${file_mode}" "${source_file}" "${target_file}"
  if [[ -n "${owner}" ]]; then
    chown "${owner}" "${target_file}"
  fi
  return 0
}

install_rendered_if_changed() {
  local rendered_file="$1"
  local target_file="$2"
  local file_mode="$3"

  if [[ -f "${target_file}" ]] && cmp -s "${rendered_file}" "${target_file}"; then
    return 1
  fi

  install -m "${file_mode}" "${rendered_file}" "${target_file}"
  return 0
}

redis_running() {
  local container_id
  container_id="$(docker compose -f "${INFRA_DIR}/docker-compose.yml" ps -q redis 2>/dev/null || true)"
  [[ -n "${container_id}" ]] && [[ "$(docker inspect -f '{{.State.Running}}' "${container_id}" 2>/dev/null || true)" == "true" ]]
}

cleanup_legacy_mock_container() {
  if docker ps -a --format '{{.Names}}' | grep -qx "${LEGACY_MOCK_CONTAINER_NAME}"; then
    echo "[INFO] Stop legacy mock container: ${LEGACY_MOCK_CONTAINER_NAME}"
    docker rm -f "${LEGACY_MOCK_CONTAINER_NAME}" >/dev/null 2>&1 || true
  fi
}

install -d -m 755 -o "${DEPLOY_USER}" -g "${DEPLOY_USER}" "${INFRA_DIR}"
install -d -m 755 "${DEPLOY_ROOT}" "${RUNTIME_CONFIG_DIR}"

infra_changed=0
nginx_changed=0
network_created=0

if copy_if_changed "${SCRIPT_DIR}/docker-compose.yml" "${INFRA_DIR}/docker-compose.yml" 644 "${DEPLOY_USER}:${DEPLOY_USER}"; then
  infra_changed=1
fi

if [[ -n "${APP_CONFIG_SOURCE}" ]]; then
  copy_if_changed "${APP_CONFIG_SOURCE}" "${APP_CONFIG_FILE}" 600 >/dev/null || true
elif [[ ! -f "${APP_CONFIG_FILE}" ]]; then
  echo "[ERROR] application-dev.yml is required at ${APP_CONFIG_FILE}" >&2
  exit 1
fi

if command -v docker >/dev/null 2>&1; then
  if ! docker network inspect "${NETWORK_NAME}" >/dev/null 2>&1; then
    docker network create "${NETWORK_NAME}"
    network_created=1
  fi

  if (( infra_changed == 1 || network_created == 1 )) || ! redis_running; then
    docker compose -f "${INFRA_DIR}/docker-compose.yml" up -d redis
  else
    echo "[INFO] Redis infra unchanged, skip compose up"
  fi

  cleanup_legacy_mock_container
else
  echo "[ERROR] docker is not installed" >&2
  exit 1
fi

if command -v nginx >/dev/null 2>&1; then
  install -d -m 755 /etc/nginx/conf.d /etc/nginx/sites-available /etc/nginx/sites-enabled

  if copy_if_changed "${SCRIPT_DIR}/nginx/zzz-ssd-timeout.conf" "${NGINX_TIMEOUT_FILE}" 644; then
    nginx_changed=1
  fi

  rendered_site_file="$(mktemp)"
  trap 'rm -f "${rendered_site_file}"' EXIT
  sed "s#__SERVER_NAME__#${NGINX_SERVER_NAME}#g" "${SCRIPT_DIR}/nginx/site.conf.template" > "${rendered_site_file}"
  if install_rendered_if_changed "${rendered_site_file}" "${NGINX_SITE_AVAILABLE}" 644; then
    nginx_changed=1
  fi
  ln -sfn "${NGINX_SITE_AVAILABLE}" "${NGINX_SITE_ENABLED}"

  if [[ ! -f "${NGINX_UPSTREAM_FILE}" ]]; then
    cat > "${NGINX_UPSTREAM_FILE}" <<EOF_UPSTREAM
upstream ssd_backend {
    server 127.0.0.1:8080;
    keepalive 32;
}
EOF_UPSTREAM
    nginx_changed=1
  fi

  if (( nginx_changed == 1 )); then
    nginx -t
    systemctl reload nginx
  else
    echo "[INFO] Nginx infra unchanged, skip reload"
  fi
else
  echo "[ERROR] nginx is not installed" >&2
  exit 1
fi

echo "[INFO] Infra install complete"
