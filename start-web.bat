@echo off
title Campus Academic Systems - Web Dashboard
cd /d "%~dp0"

echo ========================================================
echo   Campus Academic Management System (1-Click Web Runner)
echo ========================================================
echo.

if not exist bin mkdir bin

echo Compiling Java source files...
dir /s /b src\*.java > sources.txt
javac -d bin @sources.txt
del sources.txt

echo.
echo Starting Web Server on http://localhost:8080 ...
echo Opening your web browser...
start http://localhost:8080

java -cp bin com.campus.main.WebLauncher
pause
