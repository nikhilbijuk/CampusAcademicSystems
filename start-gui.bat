@echo off
title Campus Academic Systems - Desktop Swing GUI
echo ========================================================
echo   Campus Academic Systems (Java Swing + SQLite JDBC)
echo   KTU PBL Course Project (PBCST304 Module 4)
echo ========================================================
echo.

if not exist bin mkdir bin
if not exist data mkdir data
if not exist lib mkdir lib

if not exist lib\sqlite-jdbc-3.45.1.0.jar (
    echo Downloading SQLite JDBC Driver...
    powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; (New-Object System.Net.WebClient).DownloadFile('https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.45.1.0/sqlite-jdbc-3.45.1.0.jar', 'lib\sqlite-jdbc-3.45.1.0.jar')"
)

echo Compiling Java source files with SQLite JDBC driver...
dir /s /b src\*.java > sources.txt
javac -cp ".;lib/*" -d bin @sources.txt
del sources.txt

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed!
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo Launching CampusSwingApp Desktop GUI...
java -cp "bin;lib/*" com.campus.gui.CampusSwingApp
pause
