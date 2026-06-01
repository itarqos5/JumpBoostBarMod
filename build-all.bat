@echo off
setlocal

cd /d "%~dp0"

if exist "C:\Program Files\Java\jdk-21\bin\java.exe" (
    set "JAVA_HOME=C:\Program Files\Java\jdk-21"
    set "PATH=%JAVA_HOME%\bin;%PATH%"
)

echo Building Fabric, NeoForge, and Paper...
call gradlew.bat --no-daemon :fabric:build :neoforge:build :paper:build
if errorlevel 1 exit /b %errorlevel%

if exist dist rmdir /s /q dist
mkdir dist

for %%M in (fabric neoforge paper) do (
    for %%J in ("%%M\build\libs\*.jar") do (
        echo %%~nxJ | findstr /i /c:"-sources.jar" /c:"-dev.jar" /c:"-original.jar" >nul
        if errorlevel 1 copy /y "%%~fJ" "dist\" >nul
    )
)

echo Built jars:
dir /b dist\*.jar
