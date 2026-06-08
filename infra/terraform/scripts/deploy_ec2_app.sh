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
DB_ENDPOINT="$(terraform output -raw db_endpoint)"
DB_NAME="$(terraform output -raw db_name)"
DB_USERNAME="$(terraform output -raw db_username)"
DB_PASSWORD="$(terraform output -raw db_password)"
S3_BUCKET_NAME="$(terraform output -raw s3_bucket_name)"
EXTERNAL_AI_BASE_URL="$(terraform output -raw external_ai_base_url 2>/dev/null || true)"

ACCOUNT_ID="$(aws sts get-caller-identity --query Account --output text)"
AWS_REGION="$(terraform output -json >/dev/null 2>&1 && terraform console <<< 'var.aws_region' 2>/dev/null | tr -d '\"' || true)"
AWS_REGION="${AWS_REGION:-ap-northeast-2}"

TMP_CONFIG="$(mktemp)"
trap 'rm -f "${TMP_CONFIG}"' EXIT

cat > "${TMP_CONFIG}" <<EOF
spring:
  application:
    name: ssd

  mvc:
    throw-exception-if-no-handler-found: true
  web:
    resources:
      add-mappings: false

  config:
    activate:
      on-profile: dev

  datasource:
    url: jdbc:postgresql://${DB_ENDPOINT}:5432/${DB_NAME}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        format_sql: false
        dialect: org.hibernate.dialect.PostgreSQLDialect
        show_sql: false
  data:
    redis:
      host: infra-redis-1
      port: 6379

  jwt:
    header: Authorization
    secret: sdvjnfdjnkdvsfjvnjvdksfdljnkvfljovnkf
    access-token-validity-in-seconds: 60000000
    refresh-token-validity-in-seconds: 2592000

  security:
    oauth2:
      client:
        registration:
          kakao:
            client-name: kakao
            client-id: b93385a5496e64dd00060400787950fb
            redirect-uri: http://${APP_IP}:80/oauth/kakao/server/callback
            authorization-grant-type: authorization_code
            scope: profile_nickname,profile_image,account_email
            client-authentication-method: client_secret_post
        provider:
          kakao:
            authorization-uri: https://kauth.kakao.com/oauth/authorize
            token-uri: https://kauth.kakao.com/oauth/token
            user-info-uri: https://kapi.kakao.com/v2/user/me
            user-name-attribute: id

feign:
  client:
    config:
      default:
        connectTimeout: 2000
        readTimeout: 3000

app:
  cookie:
    domain: ""
    same-site: None
    secure: false
  storage:
    s3:
      bucket: ${S3_BUCKET_NAME}
      region: ${AWS_REGION}
      access-key:
      secret-key:
  oauth:
    allowed-origins:
      - http://localhost:5173
      - http://localhost:8080
      - http://${APP_IP}
      - http://${APP_IP}:80
      - https://dev-api.simsaimdang.shop
      - https://dev.simsaimdang.shop
      - https://ssd-client-zfl2.vercel.app
  external-ai:
    base-url: http://166.104.223.33:8080
  search:
    pg-trgm:
      enabled: true

discord:
  webhook-url: https://discord.com/api/webhooks/1481239816069971988/qgcacvS-zm6gdbwCbK4aLpt_hsm34UEnL_pQZQrwa-Vd4Bez3JLpeg1dmFytO4MH5P51

sentry:
  dsn: https://46b8f558adc9be8be498848c3ca1cf7b@o4508596177076224.ingest.de.sentry.io/4511025301684304
  send-default-pii: true
  environment: \${SENTRY_ENVIRONMENT:\${spring.application.name}}

management:
  endpoints:
    web:
      exposure:
        include: health,prometheus
  endpoint:
    health:
      probes:
        enabled: true
      access: unrestricted
    prometheus:
      access: unrestricted
  metrics:
    tags:
      application: \${spring.application.name}
EOF

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
scp "${SSH_OPTS[@]}" "${TMP_CONFIG}" "${SSH_USER}@${APP_IP}:/home/${SSH_USER}/application-dev.yml"

ssh "${SSH_OPTS[@]}" "${SSH_USER}@${APP_IP}" bash <<EOF
set -euo pipefail
sudo APP_CONFIG_SOURCE="/home/${SSH_USER}/application-dev.yml" \
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
