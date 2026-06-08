#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TERRAFORM_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
REPO_ROOT="$(cd "${TERRAFORM_DIR}/../.." && pwd)"

AWS_PROFILE="${AWS_PROFILE:-capstone-ssd}"
SSH_PRIVATE_KEY_PATH="${SSH_PRIVATE_KEY_PATH:-$HOME/.ssh/id_rsa}"
SSH_USER="${SSH_USER:-ubuntu}"
IMAGE_TAG="${IMAGE_TAG:-$(date +%Y%m%d%H%M%S)}"
SPRING_PROFILE="${SPRING_PROFILE:-dev}"
APP_NAME="${APP_NAME:-ssd}"
NGINX_SERVER_NAME="${NGINX_SERVER_NAME:-_}"
APP_CONFIG_SOURCE="${APP_CONFIG_SOURCE:-}"

export AWS_PROFILE

require_cmd() {
  command -v "$1" >/dev/null 2>&1 || {
    echo "[ERROR] missing command: $1" >&2
    exit 1
  }
}

for cmd in aws docker terraform ssh scp; do
  require_cmd "${cmd}"
done

cd "${TERRAFORM_DIR}"

APP_IP="$(terraform output -raw app_public_ip)"
ECR_URL="$(terraform output -raw ecr_repository_url)"

ACCOUNT_ID="$(aws sts get-caller-identity --query Account --output text)"
AWS_REGION="$(terraform output -json >/dev/null 2>&1 && terraform console <<< 'var.aws_region' 2>/dev/null | tr -d '\"' || true)"
AWS_REGION="${AWS_REGION:-ap-northeast-2}"

if [[ -z "${APP_CONFIG_SOURCE}" ]]; then
  echo "[ERROR] APP_CONFIG_SOURCE is required. Provide a local application-${SPRING_PROFILE}.yml path." >&2
  exit 1
fi

if [[ ! -f "${APP_CONFIG_SOURCE}" ]]; then
  echo "[ERROR] APP_CONFIG_SOURCE not found: ${APP_CONFIG_SOURCE}" >&2
  exit 1
fi

echo "[INFO] Build amd64 image ${ECR_URL}:${IMAGE_TAG}"
aws ecr get-login-password --region "${AWS_REGION}" | docker login --username AWS --password-stdin "${ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
docker buildx build \
  --platform linux/amd64 \
  --push \
  -t "${ECR_URL}:${IMAGE_TAG}" \
  "${REPO_ROOT}"

SSH_OPTS=(-i "${SSH_PRIVATE_KEY_PATH}" -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null)

echo "[INFO] Wait for EC2 SSH: ${APP_IP}"
for _ in $(seq 1 60); do
  if ssh "${SSH_OPTS[@]}" "${SSH_USER}@${APP_IP}" "echo ok" >/dev/null 2>&1; then
    break
  fi
  sleep 5
done

ssh "${SSH_OPTS[@]}" "${SSH_USER}@${APP_IP}" "echo connected" >/dev/null

scp "${SSH_OPTS[@]}" -r "${REPO_ROOT}/deploy/ec2" "${SSH_USER}@${APP_IP}:/home/${SSH_USER}/"
scp "${SSH_OPTS[@]}" "${APP_CONFIG_SOURCE}" "${SSH_USER}@${APP_IP}:/home/${SSH_USER}/application-${SPRING_PROFILE}.yml"

ssh "${SSH_OPTS[@]}" "${SSH_USER}@${APP_IP}" bash <<EOF
set -euo pipefail
sudo APP_CONFIG_SOURCE="/home/${SSH_USER}/application-${SPRING_PROFILE}.yml" \
  NGINX_ENABLE_SSL=false \
  NGINX_SERVER_NAME="${NGINX_SERVER_NAME}" \
  /home/${SSH_USER}/ec2/install_infra.sh

aws ecr get-login-password --region "${AWS_REGION}" | sudo docker login --username AWS --password-stdin "${ECR_URL%/*}"

sudo DOCKER_REPO="${ECR_URL}" \
  IMAGE_TAG="${IMAGE_TAG}" \
  SPRING_PROFILES_ACTIVE="${SPRING_PROFILE}" \
  APP_GRAFANA_BASE_URL="http://${APP_IP}/grafana" \
  HEALTH_PATH="/actuator/health" \
  /home/${SSH_USER}/ec2/blue_green_deploy.sh
EOF

echo "[INFO] Deployment finished"
echo "[INFO] Swagger: http://${APP_IP}/swagger-ui/index.html"
echo "[INFO] Health:  http://${APP_IP}/actuator/health"
