#!/usr/bin/env bash
set -euo pipefail
cd -- "$(dirname -- "${BASH_SOURCE[0]}")"
export APP_DB_MODE=sqlite-memory APP_CONSOLE_ENABLED=true
if [[ $# -gt 0 ]]; then export APP_SQLITE_EXPORT_PATH="$1"; fi
bash ./mvnw -DskipTests -Dirisen.build.name=irisen-memory package
echo 'Starting at http://127.0.0.1:8080. Type exit to save and stop.'
exec java -Dapp.db.mode=sqlite-memory -Dserver.address=127.0.0.1 -Dserver.port=8080 -Dapp.console.enabled=true -jar target/irisen-memory.jar
