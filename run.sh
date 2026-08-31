#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
MODE="${1:-sqlite}"

case "$MODE" in
  sqlite)
    shift || true
    exec "$SCRIPT_DIR/run-sqlite.sh" "$@"
    ;;
  mysql)
    shift
    exec "$SCRIPT_DIR/run-mysql.sh" "$@"
    ;;
  *)
    echo "Usage: $0 [sqlite [jar-file] | mysql [--configure|--configure-only] [jar-file]]" >&2
    exit 2
    ;;
esac
