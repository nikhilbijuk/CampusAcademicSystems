#!/bin/bash
cd "$(dirname "$0")"

echo "========================================================"
echo "  Campus Academic Systems (Java Swing + SQLite JDBC)"
echo "  KTU PBL Course Project (PBCST304 Module 4)"
echo "========================================================"
echo ""

mkdir -p bin data lib

if [ ! -f lib/sqlite-jdbc-3.45.1.0.jar ]; then
    echo "Downloading SQLite JDBC Driver..."
    curl -L -o lib/sqlite-jdbc-3.45.1.0.jar https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.45.1.0/sqlite-jdbc-3.45.1.0.jar
fi

echo "Compiling Java source files with SQLite JDBC driver..."
find src -name "*.java" > sources.txt
javac -cp ".:lib/*" -d bin @sources.txt
rm sources.txt

if [ $? -ne 0 ]; then
    echo "[ERROR] Compilation failed!"
    exit 1
fi

echo "Launching CampusSwingApp Desktop GUI..."
java -cp "bin:lib/*" com.campus.gui.CampusSwingApp
