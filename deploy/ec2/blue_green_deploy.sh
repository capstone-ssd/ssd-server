#!/usr/bin/env bash
set -euo pipefail

APP_NAME="${APP_NAME:-ssd}"
DOCKER_REPO="${DOCKER_REPO:?DOCKER_REPO is required}"
IMAGE_TAG="${IMAGE_TAG:?IMAGE_TAG is required}"
IMAGE="${DOCKER_REPO}:${IMAGE_TAG}"

LOCK_FILE="${LOCK_FILE:-/var/lock/${APP_NAME}-deploy.lock}"
mkdir -p "$(dirname "${LOCK_FILE}")"
exec 200>"${LOCK_FILE}"
flock -n 200 || { echo "[ERROR] Another deployment is running"; exit 1; }

SPRING_PROFILE="${SPRING_PROFILES_ACTIVE:-dev}"
STATE_FILE="${STATE_FILE:-/opt/${APP_NAME}/active_color}"
NETWORK_NAME="${NETWORK_NAME:-ssd-net}"
APP_CONFIG_FILE="${APP_CONFIG_FILE:-/opt/${APP_NAME}/config/application-dev.yml}"
SPRING_CONFIG_ADDITIONAL_LOCATION="${SPRING_CONFIG_ADDITIONAL_LOCATION:-optional:file:/config/}"
TIME_ZONE="${APP_TIME_ZONE:-Asia/Seoul}"
APP_LOG_DIR="${APP_LOG_DIR:-/opt/${APP_NAME}/logs}"
APP_JAVA_TOOL_OPTIONS="${APP_JAVA_TOOL_OPTIONS:--Xms256m -Xmx256m -Xlog:gc*,safepoint:file=/logs/gc.log:time,uptime,level,tags:filecount=5,filesize=10M}"
APP_SEARCH_ELASTICSEARCH_ENABLED="${APP_SEARCH_ELASTICSEARCH_ENABLED:-false}"
SPRING_ELASTICSEARCH_URIS="${SPRING_ELASTICSEARCH_URIS:-http://ssd-elasticsearch:9200}"
APP_SEARCH_ELASTICSEARCH_INDEX_NAME="${APP_SEARCH_ELASTICSEARCH_INDEX_NAME:-ssd-dev-documents}"

BLUE_PORT="${BLUE_PORT:-8080}"
GREEN_PORT="${GREEN_PORT:-8081}"
APP_PORT="${APP_PORT:-8080}"
HEALTH_PATH="${HEALTH_PATH:-/swagger-ui/index.html}"
HEALTH_TIMEOUT_SECONDS="${HEALTH_TIMEOUT_SECONDS:-120}"
HEALTH_REQUEST_TIMEOUT_SECONDS="${HEALTH_REQUEST_TIMEOUT_SECONDS:-5}"
HEALTH_CONNECT_TIMEOUT_SECONDS="${HEALTH_CONNECT_TIMEOUT_SECONDS:-3}"
HEALTH_RETRY_SLEEP_SECONDS="${HEALTH_RETRY_SLEEP_SECONDS:-2}"

NGINX_UPSTREAM_FILE="${NGINX_UPSTREAM_FILE:-/etc/nginx/conf.d/ssd-upstream.conf}"

mkdir -p "$(dirname "${STATE_FILE}")" "${APP_LOG_DIR}"

if [[ ! -f "${APP_CONFIG_FILE}" ]]; then
  echo "[ERROR] application config not found: ${APP_CONFIG_FILE}" >&2
  exit 1
fi

if [[ ! -f "${STATE_FILE}" ]]; then
  echo "blue" > "${STATE_FILE}"
fi

CURRENT_COLOR="$(cat "${STATE_FILE}")"
if [[ "${CURRENT_COLOR}" == "blue" ]]; then
  NEXT_COLOR="green"
  NEXT_PORT="${GREEN_PORT}"
  CURRENT_PORT="${BLUE_PORT}"
else
  NEXT_COLOR="blue"
  NEXT_PORT="${BLUE_PORT}"
  CURRENT_PORT="${GREEN_PORT}"
fi

NEXT_CONTAINER="${APP_NAME}-${NEXT_COLOR}"
CURRENT_CONTAINER="${APP_NAME}-${CURRENT_COLOR}"
HEALTH_URL="http://127.0.0.1:${NEXT_PORT}${HEALTH_PATH}"
LEGACY_APP_CONTAINER_NAME="${LEGACY_APP_CONTAINER_NAME:-infra-app-1}"

echo "[INFO] Current=${CURRENT_COLOR}(${CURRENT_PORT}), Next=${NEXT_COLOR}(${NEXT_PORT})"
echo "[INFO] Pull image: ${IMAGE}"
echo "[INFO] JAVA_TOOL_OPTIONS=${APP_JAVA_TOOL_OPTIONS}"
docker pull "${IMAGE}"

docker network inspect "${NETWORK_NAME}" >/dev/null 2>&1 || docker network create "${NETWORK_NAME}"

docker rm -f "${NEXT_CONTAINER}" >/dev/null 2>&1 || true

echo "[INFO] Start ${NEXT_CONTAINER}"
docker run -d \
  --name "${NEXT_CONTAINER}" \
  --restart unless-stopped \
  --network "${NETWORK_NAME}" \
  -e SPRING_PROFILES_ACTIVE="${SPRING_PROFILE}" \
  -e SPRING_CONFIG_ADDITIONAL_LOCATION="${SPRING_CONFIG_ADDITIONAL_LOCATION}" \
  -e JAVA_TOOL_OPTIONS="${APP_JAVA_TOOL_OPTIONS}" \
  -e APP_SEARCH_ELASTICSEARCH_ENABLED="${APP_SEARCH_ELASTICSEARCH_ENABLED}" \
  -e SPRING_ELASTICSEARCH_URIS="${SPRING_ELASTICSEARCH_URIS}" \
  -e APP_SEARCH_ELASTICSEARCH_INDEX_NAME="${APP_SEARCH_ELASTICSEARCH_INDEX_NAME}" \
  -e TZ="${TIME_ZONE}" \
  -v "${APP_CONFIG_FILE}:/config/application-dev.yml:ro" \
  -v "${APP_LOG_DIR}:/logs" \
  -p "127.0.0.1:${NEXT_PORT}:${APP_PORT}" \
  "${IMAGE}"

echo "[INFO] Health check: ${HEALTH_URL}"
STARTED_AT=$(date +%s)
ATTEMPT=0
while true; do
  ATTEMPT=$((ATTEMPT + 1))

  if curl -fsS \
    --connect-timeout "${HEALTH_CONNECT_TIMEOUT_SECONDS}" \
    --max-time "${HEALTH_REQUEST_TIMEOUT_SECONDS}" \
    "${HEALTH_URL}" >/dev/null 2>&1; then
    break
  fi

  sleep "${HEALTH_RETRY_SLEEP_SECONDS}"
  ELAPSED=$(( $(date +%s) - STARTED_AT ))

  if (( ELAPSED >= HEALTH_TIMEOUT_SECONDS )); then
    echo "[ERROR] Health check timeout (${HEALTH_TIMEOUT_SECONDS}s, attempts=${ATTEMPT})"
    docker logs "${NEXT_CONTAINER}" --tail 200 || true
    docker rm -f "${NEXT_CONTAINER}" >/dev/null 2>&1 || true
    exit 1
  fi
done

cat > "${NGINX_UPSTREAM_FILE}" <<EOF_UPSTREAM
upstream ssd_backend {
    server 127.0.0.1:${NEXT_PORT};
    keepalive 32;
}
EOF_UPSTREAM

nginx -t
systemctl reload nginx

echo "${NEXT_COLOR}" > "${STATE_FILE}"

docker rm -f "${CURRENT_CONTAINER}" >/dev/null 2>&1 || true
if [[ "${LEGACY_APP_CONTAINER_NAME}" != "${NEXT_CONTAINER}" && "${LEGACY_APP_CONTAINER_NAME}" != "${CURRENT_CONTAINER}" ]]; then
  docker rm -f "${LEGACY_APP_CONTAINER_NAME}" >/dev/null 2>&1 || true
fi
docker image prune -f >/dev/null 2>&1 || true

echo "[INFO] Deploy success: active=${NEXT_COLOR}"
