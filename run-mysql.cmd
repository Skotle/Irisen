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

if not defined APP_SERVER_PORT set "APP_SERVER_PORT=8080"
if not defined SERVER_ADDRESS set "SERVER_ADDRESS=127.0.0.1"

for %%V in (APP_DB_HOST APP_DB_USER APP_DB_PASSWORD APP_SMTP_EMAIL APP_SMTP_PASSWORD APP_ADMIN_LOGIN_CODE APP_ADMIN_ALLOWED_ADDRESSES) do (
    if not defined %%V (
        echo [ERROR] Required environment variable %%V is not set.
        echo Configure the runtime secrets outside this project before starting MySQL mode.
        exit /b 1
    )
)

echo Starting Irisen MySQL mode at %SERVER_ADDRESS%:%APP_SERVER_PORT%
call "%~dp0mvnw.cmd" spring-boot:run -Dspring-boot.run.jvmArguments="-Dserver.address=%SERVER_ADDRESS%"
set "EXIT_CODE=%ERRORLEVEL%"
endlocal & exit /b %EXIT_CODE%
