#!/usr/bin/env bash
set -euo pipefail

DEPLOY_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
cd "$DEPLOY_DIR"

JAVA_BIN="${JAVA_BIN:-/home/united/jdk-25.0.2/bin/java}"
SERVER_ADDRESS="${SERVER_ADDRESS:-0.0.0.0}"
JAR_FILE="${1:-irisen-26.3.jar}"
ENV_FILE="${MYSQL_RUNTIME_ENV_FILE:-$DEPLOY_DIR/.mysql-runtime.env}"

if [[ ! -x "$JAVA_BIN" ]]; then
  echo "Java executable not found or not executable: $JAVA_BIN" >&2
  exit 1
fi
if [[ ! -f "$JAR_FILE" ]]; then
  echo "JAR file not found: $DEPLOY_DIR/$JAR_FILE" >&2
  echo "Place the application JAR here as irisen-26.3.jar, or pass its filename: $0 actual.jar" >&2
  exit 1
fi

if [[ ! -f "$ENV_FILE" ]]; then
  echo "First MySQL run: enter the connection settings to save in $ENV_FILE"
  read -r -p "Server port [80]: " APP_SERVER_PORT
  APP_SERVER_PORT="${APP_SERVER_PORT:-80}"
  read -r -p "DB IP address: " APP_DB_HOST
  read -r -p "DB user: " APP_DB_USER
  read -r -s -p "DB password: " APP_DB_PASSWORD
  echo
  read -r -p "SMTP email: " APP_SMTP_EMAIL
  read -r -s -p "SMTP password: " APP_SMTP_PASSWORD
  echo

  if [[ -z "$APP_DB_HOST" || -z "$APP_DB_USER" || -z "$APP_DB_PASSWORD" || -z "$APP_SMTP_EMAIL" || -z "$APP_SMTP_PASSWORD" ]]; then
    echo "All MySQL and SMTP fields are required." >&2
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
  } > "$ENV_FILE"
  chmod 600 "$ENV_FILE"
fi

# shellcheck disable=SC1090
source "$ENV_FILE"
export APP_SERVER_PORT APP_DB_HOST APP_DB_USER APP_DB_PASSWORD APP_SMTP_EMAIL APP_SMTP_PASSWORD

exec "$JAVA_BIN" -Dserver.address="$SERVER_ADDRESS" -jar "$JAR_FILE"
