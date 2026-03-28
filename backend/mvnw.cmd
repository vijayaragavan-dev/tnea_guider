@echo off
setlocal

set "MVN_VERSION=3.9.9"
set "BASE_DIR=%~dp0"
set "TOOLS_DIR=%BASE_DIR%.tools"
set "MVN_DIR=%TOOLS_DIR%\apache-maven-%MVN_VERSION%"
set "MVN_CMD=%MVN_DIR%\bin\mvn.cmd"

if not exist "%MVN_CMD%" (
  if not exist "%TOOLS_DIR%" mkdir "%TOOLS_DIR%"
  set "ZIP_FILE=%TOOLS_DIR%\apache-maven-%MVN_VERSION%-bin.zip"
  set "URL=https://archive.apache.org/dist/maven/maven-3/%MVN_VERSION%/binaries/apache-maven-%MVN_VERSION%-bin.zip"
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -Uri '%URL%' -OutFile '%ZIP_FILE%'"
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -Path '%ZIP_FILE%' -DestinationPath '%TOOLS_DIR%' -Force"
)

if "%~1"=="spring-boot" (
  shift
  call "%MVN_CMD%" spring-boot:run %*
) else (
  call "%MVN_CMD%" %*
)
