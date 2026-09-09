@echo off
setlocal
title Irisen Query Explorer
cd /d "%~dp0"

if not exist "index.html" goto missing_files
if not exist "schema.js" goto missing_files

py -3 -c "import http.server, webbrowser" >nul 2>&1
if not errorlevel 1 (
    set "QUERY_PYTHON=py -3"
    goto start_server
)
python -c "import http.server, webbrowser" >nul 2>&1
if not errorlevel 1 (
    set "QUERY_PYTHON=python"
    goto start_server
)

echo [ERROR] Python 3 was not found. Install Python 3 and try again.
echo You can also open index.html directly in your browser.
pause
exit /b 1

:missing_files
echo [ERROR] index.html or schema.js is missing from this folder.
pause
exit /b 1

:start_server
echo Starting Irisen Query Explorer...
echo An available local port will be selected automatically.
echo Keep this window open. Press Ctrl+C or close it to stop the server.
echo.
%QUERY_PYTHON% -c "from functools import partial; from http.server import ThreadingHTTPServer, SimpleHTTPRequestHandler; from pathlib import Path; import webbrowser; root = Path.cwd().parent; server = ThreadingHTTPServer(('127.0.0.1', 0), partial(SimpleHTTPRequestHandler, directory=str(root))); url = 'http://127.0.0.1:' + str(server.server_port) + '/query/'; print(url, flush=True); webbrowser.open(url); server.serve_forever()"
set "QUERY_EXIT_CODE=%ERRORLEVEL%"
if not "%QUERY_EXIT_CODE%"=="0" pause
exit /b %QUERY_EXIT_CODE%
