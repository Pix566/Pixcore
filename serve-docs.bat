@echo off
rem Serve Pixcore MkDocs wiki locally at http://127.0.0.1:8000
set PYTHON=D:\p\python.exe
if exist "%PYTHON%" (
  "%PYTHON%" -m mkdocs serve
) else (
  where mkdocs >nul 2>nul
  if %errorlevel%==0 (
    mkdocs serve
  ) else (
    echo MkDocs not found. Install with: D:\p\python.exe -m pip install -r requirements-docs.txt
    exit /b 1
  )
)
