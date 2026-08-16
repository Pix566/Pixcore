@echo off
rem Build all Pixcore modules. Requires JDK 21 and Gradle 9.2.1+.
where gradle >nul 2>nul
if %errorlevel%==0 (
  gradle clean build
) else (
  echo Gradle not found. Install Gradle 9.2.1+ or use an IDE with Gradle support.
  exit /b 1
)
