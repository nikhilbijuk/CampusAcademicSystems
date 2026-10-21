# 🖥️ Guide: Running on Another PC or College Lab System

This guide explains how to run the **Campus Academic Management System** on another computer (such as a lab machine, evaluator's PC, or laptop) during your viva or project presentation.

---

## 📋 Prerequisites
The target PC only needs:
* **Java Development Kit (JDK 8 or newer)** installed.
  *(Recommended: OpenJDK 17 or 21)*.
  * Verify in terminal: `java -version` and `javac -version`.

---

## 🚀 Option 1: Running from the Project ZIP (100% Offline — Recommended)

This is the **best and safest method** for college presentations because all pre-seeded databases (`data/campus.db`) and JDBC drivers (`lib/*.jar`) are included. No internet connection is required.

### Steps:
1. Copy `CampusAcademicSystems_Latest.zip` to a USB drive and paste it onto the target PC.
2. Right-click the `.zip` file → **Extract All...**
3. Open the extracted folder:

### A. Run the Web Application
* **Windows**: Double-click `start-web.bat`
* **Linux / macOS**: Open terminal and run:
  ```bash
  chmod +x start-web.sh
  ./start-web.sh
  ```
* Open your browser and navigate to: **[http://localhost:8080](http://localhost:8080)**
* **Login Credentials**:
  * **Student**: ID `s101`, Password `student123`
  * **Admin**: ID `admin`, Password `admin123`

### B. Run the Java Swing Desktop GUI (Module 4 / CO5)
* **Windows**: Double-click `start-gui.bat`
* **Linux / macOS**: Open terminal and run:
  ```bash
  chmod +x start-gui.sh
  ./start-gui.sh
  ```
* The native Swing window will launch immediately with SQLite connected (`data/campus.db`).

### C. Run the Unit Test Suite (28 Tests)
* Open Command Prompt or PowerShell in the folder and run:
  ```powershell
  java -cp "bin;lib/*" com.campus.test.CampusTestHarness
  ```
* Output will verify: **`TEST SUMMARY: Passed 28 / 28 tests.`**

---

## 🌐 Option 2: Running from GitHub Clone (Online)

If you don't have the zip file but have an internet connection on the target PC:

1. Open PowerShell, Terminal, or Command Prompt:
   ```bash
   git clone https://github.com/nikhilbijuk/CampusAcademicSystems.git
   cd CampusAcademicSystems
   ```
2. **Start the Web App**:
   ```powershell
   start-web.bat
   ```
   *(Or on Linux/macOS: `./start-web.sh`)*
3. **Start the Swing Desktop App**:
   ```powershell
   start-gui.bat
   ```
   *(The `start-gui.bat` script will automatically download the SQLite JDBC driver and SLF4J jars into `lib/` if missing).*

---

## 🗄️ Option 3: Inspecting the Database (`data/campus.db`)

To show the database during viva:
1. Open **DB Browser for SQLite** on the target PC.
2. Click **File → Open Database** and choose `data/campus.db`.
3. Go to the **Browse Data** tab and select table **`court_reservations`** to show the live bookings.

Alternatively, inspect via terminal without installing any tools:
```powershell
java -cp "bin;lib/*" com.campus.db.DumpDb
```
