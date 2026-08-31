#!/usr/bin/env bash
set -euo pipefail

DEPLOY_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
cd "$DEPLOY_DIR"

JAVA_BIN="${JAVA_BIN:-$(command -v java || true)}"
SERVER_ADDRESS="${SERVER_ADDRESS:-127.0.0.1}"
ENV_FILE="${MYSQL_RUNTIME_ENV_FILE:-/etc/irisen/runtime.env}"
FORCE_CONFIGURE=false
CONFIGURE_ONLY=false

case "${1:-}" in
  --configure)
    FORCE_CONFIGURE=true
    shift
    ;;
  --configure-only)
    FORCE_CONFIGURE=true
    CONFIGURE_ONLY=true
    shift
    ;;
esac
JAR_FILE="${1:-irisen-26.4.jar}"

if [[ -f "$ENV_FILE" ]]; then
  if [[ -L "$ENV_FILE" ]]; then
    echo "Refusing to load a symbolic-link runtime environment file: $ENV_FILE" >&2
    exit 1
  fi
  if [[ -n "$(find "$ENV_FILE" -prune -perm /077 -print -quit)" ]]; then
    echo "Runtime environment file must not be accessible by group or other users: $ENV_FILE" >&2
    exit 1
  fi
  # shellcheck disable=SC1090
  source "$ENV_FILE"
fi

if [[ "$FORCE_CONFIGURE" == true || -z "${APP_SERVER_PORT:-}" || -z "${APP_DB_HOST:-}" || -z "${APP_DB_USER:-}" || -z "${APP_DB_PASSWORD:-}" || -z "${APP_SMTP_EMAIL:-}" || -z "${APP_SMTP_PASSWORD:-}" || -z "${APP_ADMIN_LOGIN_CODE:-}" || -z "${APP_ADMIN_ALLOWED_ADDRESSES:-}" ]]; then
  ENV_DIR="$(dirname "$ENV_FILE")"
  if [[ ! -d "$ENV_DIR" ]]; then
    install -d -m 700 "$ENV_DIR"
  fi
  if [[ -f "$ENV_FILE" ]]; then
    cp -p -- "$ENV_FILE" "$ENV_FILE.bak"
    chmod 600 "$ENV_FILE.bak"
    echo "Previous settings were backed up to $ENV_FILE.bak"
  fi
  echo "Enter the complete MySQL runtime settings to save in $ENV_FILE"
  read -r -p "Internal server port [8080]: " APP_SERVER_PORT
  APP_SERVER_PORT="${APP_SERVER_PORT:-8080}"
  read -r -p "DB IP address: " APP_DB_HOST
  read -r -p "DB user: " APP_DB_USER
  read -r -s -p "DB password: " APP_DB_PASSWORD
  echo
  read -r -p "SMTP email: " APP_SMTP_EMAIL
  read -r -s -p "SMTP password: " APP_SMTP_PASSWORD
  echo
  read -r -s -p "Admin login code (use a generated high-entropy secret): " APP_ADMIN_LOGIN_CODE
  echo
  read -r -p "Admin allowed IP addresses (comma-separated): " APP_ADMIN_ALLOWED_ADDRESSES

  if [[ -z "$APP_DB_HOST" || -z "$APP_DB_USER" || -z "$APP_DB_PASSWORD" || -z "$APP_SMTP_EMAIL" || -z "$APP_SMTP_PASSWORD" || -z "$APP_ADMIN_LOGIN_CODE" || -z "$APP_ADMIN_ALLOWED_ADDRESSES" ]]; then
    echo "All database, SMTP, and admin security fields are required." >&2
    exit 1
  fi
  if (( ${#APP_ADMIN_LOGIN_CODE} < 32 )); then
    echo "Admin login code must contain at least 32 characters." >&2
    exit 1
  fi

  umask 077
  {
    printf 'APP_SERVER_PORT=%q\n' "$APP_SERVER_PORT"
    printf 'APP_DB_HOST=%q\n' "$APP_DB_HOST"
    printf 'APP_DB_USER=%q\n' "$APP_DB_USER"
    printf 'APP_DB_PASSWORD=%q\n' "$APP_DB_PASSWORD"
    printf 'APP_SMTP_EMAIL=%q\n' "$APP_SMTP_EMAIL"
    printf 'APP_SMTP_PASSWORD=%q\n' "$APP_SMTP_PASSWORD"
    printf 'APP_ADMIN_LOGIN_CODE=%q\n' "$APP_ADMIN_LOGIN_CODE"
    printf 'APP_ADMIN_ALLOWED_ADDRESSES=%q\n' "$APP_ADMIN_ALLOWED_ADDRESSES"
  } > "$ENV_FILE"
  chmod 600 "$ENV_FILE"
fi

if (( ${#APP_ADMIN_LOGIN_CODE} < 32 )); then
  echo "APP_ADMIN_LOGIN_CODE must contain at least 32 characters." >&2
  exit 1
fi

export APP_SERVER_PORT APP_DB_HOST APP_DB_USER APP_DB_PASSWORD APP_SMTP_EMAIL APP_SMTP_PASSWORD APP_ADMIN_LOGIN_CODE APP_ADMIN_ALLOWED_ADDRESSES

if [[ "$CONFIGURE_ONLY" == true ]]; then
  echo "Runtime environment settings updated: $ENV_FILE"
  exit 0
fi

if [[ -z "$JAVA_BIN" || ! -x "$JAVA_BIN" ]]; then
  echo "Java executable was not found. Set JAVA_BIN or add java to PATH." >&2
  exit 1
fi
if [[ ! -f "$JAR_FILE" ]]; then
  echo "JAR file not found: $DEPLOY_DIR/$JAR_FILE" >&2
  echo "Place the application JAR here as irisen-26.4.jar, or pass its filename: $0 actual.jar" >&2
  exit 1
fi

exec "$JAVA_BIN" -Dserver.address="$SERVER_ADDRESS" -jar "$JAR_FILE"
