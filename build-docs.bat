@echo off
rem Build Pixcore MkDocs wiki into site/
set PYTHON=D:\p\python.exe
if exist "%PYTHON%" (
  "%PYTHON%" -m mkdocs build
) else (
  where mkdocs >nul 2>nul
  if %errorlevel%==0 (
    mkdocs build
  ) else (
    echo MkDocs not found. Install with: D:\p\python.exe -m pip install -r requirements-docs.txt
    exit /b 1
  )
)
