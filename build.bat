@echo off
echo "Choose what to build:"
echo "1. Paper"
echo "2. Fabric"
echo "3. NeoForge"
set /p choice="Enter your choice (1-3): "

if "%choice%"=="1" (
    call gradlew.bat :paper:build
) else if "%choice%"=="2" (
    call gradlew.bat :fabric:build
) else if "%choice%"=="3" (
    call gradlew.bat :neoforge:build
) else (
    echo "Invalid choice."
)

