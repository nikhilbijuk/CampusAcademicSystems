# Campus Academic Management System (KTU Mini Project Framework)

A modular, Object-Oriented Java framework for managing sports facility reservations, user quotas, leave tracking, and hostel mess billing. Designed to adhere to APJ Abdul Kalam Technological University (KTU) academic project standards.

##  Features

- **Sports Facility Reservation**:
  - Court slot reservation with collision detection.
  - Quota enforcement per role (`Student`: max 2, `Faculty`: max 5, `Coach`: max 10).
  - Unpaid fine check (`OutstandingFineException`) blocking reservations until cleared.
  - Slot cancellation with user authorization checks and admin override.
- **Hostel & Mess Billing Module**:
  - Flexible room models (`SingleOccupancy`, `ACSuite`).
  - Customizable meal plans (`StandardPlan`, `SpecialDietPlan`).
  - Leave tracking with input validation (`InvalidLeaveDaysException`).
  - Itemized monthly bill generator with leave deductions.
- **Persistence & Storage**:
  - Native Java Object Serialization (`.ser`) for saving/loading system state automatically.
  - Built-in data seed routines for fresh initializations.
- **Interactive CLI & Unit Tests**:
  - Full-featured 9-option interactive command-line interface.
  - Automated 11-case unit test suite (`CampusTestHarness`).

---

## 📁 Project Structure

```
CampusAcademicSystems/
├── src/
│   └── com/
│       └── campus/
│           ├── exceptions/      # Custom Exception classes
│           ├── hostel/          # BaseRoom, SingleOccupancy, ACSuite, MealPlan, HostelStudent
│           ├── sports/          # User, Student, Faculty, Coach, Reservable, Court
│           ├── storage/         # CampusData, CampusStorageManager
│           ├── web/             # CampusWebServer, JsonUtils
│           ├── test/            # CampusTestHarness
│           └── main/            # MainApp (CLI), WebLauncher (Web UI)
├── web/                         # Modern Web Dashboard (HTML, CSS, JS)
├── bin/                         # Compiled bytecode (.class)
├── data/                        # Serialized storage (.ser)
└── README.md
```

---

## 🚀 Getting Started & Execution Guide

### 📋 Prerequisites
Ensure you have the following installed on your machine:
* **Java Development Kit (JDK 8 or higher)**: Verify by running:
  ```bash
  javac -version
  java -version
  ```
* **Git** (optional, for version control)

---

### 1️⃣ Compilation

Before running the system, compile all Java source files from the project root into the `bin/` directory:

#### 🪟 Windows (PowerShell)
```powershell
# Create bin directory if not present
if (!(Test-Path "bin")) { New-Item -ItemType Directory -Path "bin" }

# Compile all source files recursively
javac -d bin (Get-ChildItem -Recurse -Filter *.java | Select-Object -ExpandProperty FullName)
```

#### 🪟 Windows (Command Prompt / CMD)
```cmd
:: Create bin directory if not present
if not exist bin mkdir bin

:: Find and compile all Java files
dir /s /b src\*.java > sources.txt
javac -d bin @sources.txt
del sources.txt
```

#### 🐧 Linux & 🍎 macOS (Bash / Zsh)
```bash
# Create bin directory if not present
mkdir -p bin

# Find and compile all Java files
find src -name "*.java" > sources.txt
javac -d bin @sources.txt
rm sources.txt

# Alternatively in one line:
javac -d bin $(find src -name "*.java")
```

---

### 2️⃣ Running the Application

You can run the system in two distinct modes:

#### Option A: Modern Web Dashboard (Recommended for Demonstrations & Viva)
Launches the embedded HTTP server and serves the responsive single-page web dashboard.

* **Windows (PowerShell or CMD)**:
  ```powershell
  java -cp bin com.campus.main.WebLauncher
  ```
* **Linux / macOS**:
  ```bash
  java -cp bin com.campus.main.WebLauncher
  ```

🌐 **Accessing the Dashboard**:
* Open your browser and navigate to: **[http://localhost:8080](http://localhost:8080)**
* On desktop environments, your default browser will attempt to launch automatically.
* On headless Linux servers or remote machines, forward port `8080` over SSH:
  ```bash
  ssh -L 8080:localhost:8080 username@server-ip
  ```
* **Stopping the Web Server**: Type `q` followed by `Enter` in the terminal, or press `Ctrl + C`.

---

#### Option B: Interactive Terminal CLI
Launches the traditional console runner with the 9-option interactive menu.

* **Windows (PowerShell or CMD)**:
  ```powershell
  java -cp bin com.campus.main.MainApp
  ```
* **Linux / macOS**:
  ```bash
  java -cp bin com.campus.main.MainApp
  ```

---

### 3️⃣ Running the Automated Unit Test Suite
Executes the comprehensive 11-test verification harness covering OOP inheritance, slot collisions, quota caps, fine holds, polymorphism tariffs, and serialization.

* **Windows (PowerShell or CMD)**:
  ```powershell
  java -cp bin com.campus.test.CampusTestHarness
  ```
* **Linux / macOS**:
  ```bash
  java -cp bin com.campus.test.CampusTestHarness
  ```

---

### ❓ Troubleshooting & FAQs

* **Error: `Address already in use: bind` (Port 8080 Busy)**:
  * Another application is using port 8080.
  * **Windows**: Find and terminate the process:
    ```powershell
    Get-Process -Id (Get-NetTCPConnection -LocalPort 8080).OwningProcess | Stop-Process -Force
    ```
  * **Linux / macOS**: Free the port:
    ```bash
    fuser -k 8080/tcp
    # Or:
    kill -9 $(lsof -t -i:8080)
    ```
* **Data Persistence**:
  * All active court reservations, registered hostel students, and fine balances are saved to `data/campus_data.ser`.
  * To reset the application back to default seed data at any time, simply delete the file:
    ```bash
    # Windows:
    Remove-Item -Path "data/campus_data.ser" -Force
    # Linux/macOS:
    rm -f data/campus_data.ser
    ```
