@echo off
setlocal
call mvnw.cmd -Dapp.db.mode=sqlite -Dapp.sqlite.path=mydb.db spring-boot:run
endlocal
