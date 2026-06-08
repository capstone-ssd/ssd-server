#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SERVER_NAME="${SERVER_NAME:?SERVER_NAME is required}"
DEPLOY_USER="${DEPLOY_USER:-${SUDO_USER:-ubuntu}}"
NGINX_INTERNAL_ONLY="${NGINX_INTERNAL_ONLY:-true}"

if ! command -v caddy >/dev/null 2>&1; then
  sudo apt-get update
  sudo apt-get install -y debian-keyring debian-archive-keyring apt-transport-https curl
  curl -1sLf 'https://dl.cloudsmith.io/public/caddy/stable/gpg.key' | sudo gpg --dearmor -o /usr/share/keyrings/caddy-stable-archive-keyring.gpg
  curl -1sLf 'https://dl.cloudsmith.io/public/caddy/stable/debian.deb.txt' | sudo tee /etc/apt/sources.list.d/caddy-stable.list >/dev/null
  sudo apt-get update
  sudo apt-get install -y caddy
fi

if [[ "${NGINX_INTERNAL_ONLY}" == "true" ]]; then
  config_file="/opt/ssd/config/application-dev.yml"
  if [[ ! -f "${config_file}" ]]; then
    echo "[ERROR] Config file not found: ${config_file}" >&2
    echo "[ERROR] Please run the main deployment flow or provide APP_CONFIG_SOURCE manually." >&2
    exit 1
  fi
  sudo APP_CONFIG_SOURCE="/opt/ssd/config/application-dev.yml" \
    NGINX_ENABLE_SSL=false \
    NGINX_INTERNAL_ONLY=true \
    NGINX_SERVER_NAME="_" \
    "${SCRIPT_DIR}/install_infra.sh"
fi

rendered_caddyfile="$(mktemp)"
trap 'rm -f "${rendered_caddyfile}"' EXIT

sed \
  -e "s#__SERVER_NAME__#${SERVER_NAME}#g" \
  "${SCRIPT_DIR}/caddy/Caddyfile.template" > "${rendered_caddyfile}"

sudo install -d -m 755 /etc/caddy
sudo install -m 644 "${rendered_caddyfile}" /etc/caddy/Caddyfile
sudo systemctl enable --now caddy
sudo systemctl restart caddy
sudo systemctl status caddy --no-pager
