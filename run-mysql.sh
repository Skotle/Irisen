#!/usr/bin/env bash
set -euo pipefail

DEPLOY_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
cd "$DEPLOY_DIR"

JAVA_BIN="${JAVA_BIN:-/home/united/jdk-25.0.2/bin/java}"
SERVER_ADDRESS="${SERVER_ADDRESS:-0.0.0.0}"
JAR_FILE="${1:-irisen-26.3.jar}"

if [[ ! -x "$JAVA_BIN" ]]; then
  echo "Java executable not found or not executable: $JAVA_BIN" >&2
  exit 1
fi
if [[ ! -f "$JAR_FILE" ]]; then
  echo "JAR file not found: $DEPLOY_DIR/$JAR_FILE" >&2
  echo "Place the application JAR here as irisen-26.3.jar, or pass its filename: $0 actual.jar" >&2
  exit 1
fi

exec "$JAVA_BIN" -Dserver.address="$SERVER_ADDRESS" -jar "$JAR_FILE"
