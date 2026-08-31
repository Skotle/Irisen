#!/usr/bin/env bash
set -euo pipefail

DEPLOY_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
cd "$DEPLOY_DIR"

export APP_DB_MODE=sqlite
export APP_SQLITE_DB_PATH="$DEPLOY_DIR/mydb.db"
SERVER_PORT="${SERVER_PORT:-8080}"
SERVER_ADDRESS="${SERVER_ADDRESS:-127.0.0.1}"
JAVA_BIN="${JAVA_BIN:-$(command -v java || true)}"

if [[ -z "$JAVA_BIN" || ! -x "$JAVA_BIN" ]]; then
  echo "Java executable was not found. Set JAVA_BIN or add java to PATH." >&2
  exit 1
fi

echo "Starting Irisen locally at http://$SERVER_ADDRESS:$SERVER_PORT"
if [[ $# -gt 0 ]]; then
  JAR_FILE="$1"
  if [[ ! -f "$JAR_FILE" ]]; then
    echo "JAR file not found: $JAR_FILE" >&2
    exit 1
  fi
  exec "$JAVA_BIN" -Dserver.address="$SERVER_ADDRESS" -Dserver.port="$SERVER_PORT" -jar "$JAR_FILE"
fi

if [[ ! -f "$DEPLOY_DIR/mvnw" ]]; then
  echo "Maven wrapper was not found: $DEPLOY_DIR/mvnw" >&2
  exit 1
fi

exec bash "$DEPLOY_DIR/mvnw" spring-boot:run \
  "-Dspring-boot.run.jvmArguments=-Dserver.address=$SERVER_ADDRESS -Dserver.port=$SERVER_PORT"
