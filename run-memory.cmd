@echo off
setlocal
cd /d "%~dp0"
set "APP_DB_MODE=sqlite-memory"
set "APP_CONSOLE_ENABLED=true"
if not "%~1"=="" set "APP_SQLITE_EXPORT_PATH=%~f1"
call "%~dp0mvnw.cmd" -DskipTests -Dirisen.build.name=irisen-memory package
if errorlevel 1 exit /b 1
echo Starting at http://127.0.0.1:8080. Type exit to save and stop.
java -Dapp.db.mode=sqlite-memory -Dserver.address=127.0.0.1 -Dserver.port=8080 -Dapp.console.enabled=true -jar "%~dp0target\irisen-memory.jar"
exit /b %ERRORLEVEL%
