#!/bin/bash
cd "$(dirname "$0")"

echo "========================================================"
echo "  Campus Academic Management System (1-Click Web Runner)"
echo "========================================================"
echo ""

mkdir -p bin
echo "Compiling Java source files..."
find src -name "*.java" > sources.txt
javac -d bin @sources.txt
rm sources.txt

echo "Starting Web Server at http://localhost:8080 ..."
if which xdg-open > /dev/null 2>&1; then
  xdg-open http://localhost:8080 &
elif which open > /dev/null 2>&1; then
  open http://localhost:8080 &
fi

java -cp bin com.campus.main.WebLauncher
