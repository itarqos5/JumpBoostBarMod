@echo off
setlocal

cd /d "%~dp0"

if exist "C:\Program Files\Java\jdk-21\bin\java.exe" (
    set "JAVA_HOME=C:\Program Files\Java\jdk-21"
    set "PATH=%JAVA_HOME%\bin;%PATH%"
)

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\run-client.ps1"
