@echo off
setlocal
cd /d "%~dp0"

where java >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java was not found in PATH.
    exit /b 1
)
if not exist "%~dp0mvnw.cmd" (
    echo [ERROR] mvnw.cmd was not found.
    exit /b 1
)

set "APP_DB_MODE=sqlite"
set "APP_SQLITE_DB_PATH=%~dp0mydb.db"
echo Starting Irisen locally at http://127.0.0.1:8080
call "%~dp0mvnw.cmd" spring-boot:run -Dspring-boot.run.jvmArguments="-Dserver.address=127.0.0.1 -Dserver.port=8080"
set "EXIT_CODE=%ERRORLEVEL%"
endlocal & exit /b %EXIT_CODE%
