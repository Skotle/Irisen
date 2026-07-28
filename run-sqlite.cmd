@echo off
setlocal
set "APP_DB_MODE=sqlite"
set "APP_SQLITE_DB_PATH=%~dp0mydb.db"
call mvnw.cmd spring-boot:run
endlocal
