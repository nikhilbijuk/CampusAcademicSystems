# Campus Academic Management System (KTU Mini Project Framework)

> 🚀 **Live Deployments**:
> * 💻 **Primary Full-Stack Deployment (Live Java Backend on Replit)**: [https://campus-academic-systems--devnikhilbiju.replit.app](https://campus-academic-systems--devnikhilbiju.replit.app)  
>   *(Powered by OpenJDK 17 on Replit Cloud, live REST APIs `/api/*`, pure OOP domain logic, and real-time state persistence)*
> * ⚡ **High-Availability Static Mirror (Cloudflare Edge)**: [https://campus-systems.dev-nikhilbiju.workers.dev](https://campus-systems.dev-nikhilbiju.workers.dev)  
>   *(Zero-downtime client-side mirror hosted on Cloudflare's global edge network with unlimited bandwidth)*

A modular, Object-Oriented Java framework for managing sports facility reservations, user quotas, leave tracking, and hostel mess billing. Designed to adhere to APJ Abdul Kalam Technological University (KTU) academic project standards.

##  Features

- **KTU Academic Performance & Attendance Module (New - Option A)**:
  - Subject-level attendance percentage tracking with mandatory **75% KTU minimum threshold** warnings (`LowAttendanceException`).
  - Three-tier status classification: *Eligible* ($\ge 75\%$), *Condonation Required* ($60\%-74\%$), and *Detained Shortage* ($< 60\%$).
  - Continuous Internal Evaluation (**CIE**) calculation out of 50 marks: Series Test 1 (max 20), Series Test 2 (max 20), and Assignments/Tutorials (max 10) with `InvalidMarkException` validation.
  - Official KTU 10-point credit grading system ($S=10.0, A^+=9.0, A=8.5, B^+=8.0, B=7.5, C^+=7.0, C=6.5, D=6.0, P=5.5, F/FE=0.0$).
  - Credit-weighted Semester Grade Point Average (**SGPA**) engine: $\text{SGPA} = \frac{\sum (C_i \times G_i)}{\sum C_i}$ with academic honours/classification detection.
- **Desktop Graphical User Interface (Java Swing + SQLite JDBC - Module 4 / CO5)**:
  - Clean desktop interface built with Java Swing (`JFrame`, `JTable`, `JTabbedPane`, `JOptionPane`).
  - Thread-safe **Singleton Design Pattern** [`DatabaseManager`](src/com/campus/db/DatabaseManager.java) with Double-Checked Locking.
  - Relational persistence with SQLite JDBC driver (`lib/sqlite-jdbc-3.45.1.0.jar`) across 10 normalized tables.
  - 1-click launcher scripts: `start-gui.bat` (Windows) and `start-gui.sh` (Linux/macOS).
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
- **Campus Library Module (Cross-Module Unified Fine Engine)**:
  - Textbook catalog management (`Book`, `LibraryLoan`) with role checkout quotas (`Student`: max 3 books, `Faculty`: max 10 books).
  - Overdue return calculation (`Rs. 2.00 / day`).
  - Cross-module fine propagation: overdue library fines automatically link to `User.getFineBalance()`, instantly freezing sports court bookings until cleared.
- **Persistence & Storage**:
  - SQLite Relational Database via JDBC (`data/campus.db`).
  - Native Java Object Serialization (`.ser`) fallback for cross-platform zero-dependency mode.
- **Interactive CLI & Unit Tests**:
  - Full-featured interactive command-line interface.
  - Automated 19-case unit test suite (`CampusTestHarness`) covering core OOP, exceptions, cross-module constraints, and KTU academic logic.

---

## 🏛️ Backend Architecture & Tech Stack

The backend is built with **100% Pure Core Java (JDK 8+)**, intentionally structured with **zero external third-party dependencies** to adhere strictly to KTU and academic evaluation standards:

| Layer | Technology | Key Classes | Responsibilities |
| :--- | :--- | :--- | :--- |
| **Desktop GUI (Module 4 / CO5)** | Java Swing (`JFrame`, `JTable`, `JTabbedPane`) | [`CampusSwingApp`](src/com/campus/gui/CampusSwingApp.java) | Native event-driven desktop GUI with tabs for Sports, Hostel, Library, and KTU Academics. |
| **Relational Database & DAO** | SQLite JDBC via `lib/sqlite-jdbc-3.45.1.0.jar` | [`DatabaseManager`](src/com/campus/db/DatabaseManager.java), [`CampusDao`](src/com/campus/db/CampusDao.java) | Thread-safe Singleton DB connection, 10 normalized SQL tables, CRUD queries, and object mapping. |
| **KTU Academic Performance & Attendance** | Pure Java OOP & KTU B.Tech Regulations | [`Course`](src/com/campus/academic/Course.java), [`AttendanceRecord`](src/com/campus/academic/AttendanceRecord.java), [`InternalAssessment`](src/com/campus/academic/InternalAssessment.java), [`KtuGrade`](src/com/campus/academic/KtuGrade.java), [`AcademicProfile`](src/com/campus/academic/AcademicProfile.java) | Computes subject attendance (75% threshold), CIE out of 50, and credit-weighted SGPA on KTU 10-point scale. |
| **HTTP Server & REST APIs** | `com.sun.net.httpserver.HttpServer` | [`CampusWebServer`](src/com/campus/web/CampusWebServer.java), [`JsonUtils`](src/com/campus/web/JsonUtils.java) | Exposes lightweight JSON REST endpoints (`/api/*`), serves static frontend files, binds dynamically to `$PORT`. |
| **Domain & Business Logic** | Pure Java OOP (Polymorphism, Inheritance) | [`Court`](src/com/campus/sports/Court.java), [`User`](src/com/campus/sports/User.java), [`HostelStudent`](src/com/campus/hostel/HostelStudent.java), [`Book`](src/com/campus/library/Book.java) | Enforces role quota ceilings, room tariffs, meal plans, and overdue library penalties. |
| **Custom Checked Exceptions** | Domain Exception Subclasses | `SlotAlreadyBookedException`, `OutstandingFineException`, `BookingQuotaExceededException`, `InvalidLeaveDaysException`, `BookNotAvailableException`, `LowAttendanceException`, `InvalidMarkException` | Enforces business rule validation and domain integrity. |
| **Serialization Fallback** | Native Java Object Serialization (`.ser`) | [`CampusStorageManager`](src/com/campus/storage/CampusStorageManager.java), [`CampusData`](src/com/campus/storage/CampusData.java) | Saves and loads complete object graphs to `data/campus_data.ser` via `ObjectOutputStream` and `ObjectInputStream`. |

---

## 🎯 SOLID Principles Mapping (KTU PBCST304)

| Principle | Meaning in Object-Oriented Design | How It Is Applied in this Project |
| :--- | :--- | :--- |
| **S - Single Responsibility Principle (SRP)** | Every class should have one, and only one, reason to change. | • [`AttendanceRecord`](src/com/campus/academic/AttendanceRecord.java) handles *only* class attendance counting and 75% threshold checks.<br>• [`InternalAssessment`](src/com/campus/academic/InternalAssessment.java) handles *only* CIE marks calculation.<br>• [`DatabaseManager`](src/com/campus/db/DatabaseManager.java) handles *only* DB connection pooling and schema initialization.<br>• [`CampusDao`](src/com/campus/db/CampusDao.java) handles *only* data persistence queries. |
| **O - Open/Closed Principle (OCP)** | Software entities should be open for extension, but closed for modification. | • [`BaseRoom`](src/com/campus/hostel/BaseRoom.java) is extended by [`SingleOccupancy`](src/com/campus/hostel/SingleOccupancy.java) and [`ACSuite`](src/com/campus/hostel/ACSuite.java). New room types (e.g., `DeluxeSuite`) can be added without modifying existing room logic.<br>• [`MealPlan`](src/com/campus/hostel/MealPlan.java) is extended by [`StandardPlan`](src/com/campus/hostel/StandardPlan.java) and [`SpecialDietPlan`](src/com/campus/hostel/SpecialDietPlan.java). |
| **L - Liskov Substitution Principle (LSP)** | Subclasses must be substitutable for their base classes without breaking correctness. | • Any subclass of [`User`](src/com/campus/sports/User.java) ([`Student`](src/com/campus/sports/Student.java), [`Faculty`](src/com/campus/sports/Faculty.java), [`Coach`](src/com/campus/sports/Coach.java)) can be passed polymorphically to `Court.reserve(slot, user, allCourts)`. Each respects `getBookingLimit()` without special-case branching. |
| **I - Interface Segregation Principle (ISP)** | Clients should not be forced to depend upon interfaces that they do not use. | • [`Reservable`](src/com/campus/sports/Reservable.java) interface defines only facility reservation methods (`checkAvailability`, `reserve`, `release`). It does not force mess or library methods onto sports facilities. |
| **D - Dependency Inversion Principle (DIP)** | High-level modules should depend on abstractions, not on concrete details. | • [`HostelStudent`](src/com/campus/hostel/HostelStudent.java) depends on the abstract classes `BaseRoom` and `MealPlan`, rather than hardcoded concrete types. |

---

## 📁 Project Structure

```
CampusAcademicSystems/
├── src/
│   └── com/
│       └── campus/
│           ├── academic/        # Course, AttendanceRecord, InternalAssessment, KtuGrade, AcademicProfile
│           ├── db/              # DatabaseManager (Singleton), CampusDao (JDBC CRUD)
│           ├── exceptions/      # Custom checked exceptions (LowAttendance, InvalidMark, etc.)
│           ├── gui/             # CampusSwingApp (Java Swing desktop GUI)
│           ├── hostel/          # BaseRoom, SingleOccupancy, ACSuite, MealPlan, HostelStudent
│           ├── library/         # Book, LibraryLoan
│           ├── sports/          # User, Student, Faculty, Coach, Reservable, Court
│           ├── storage/         # CampusData, CampusStorageManager (Serialization)
│           ├── web/             # CampusWebServer, JsonUtils
│           ├── test/            # CampusTestHarness (19 automated unit tests)
│           └── main/            # MainApp (CLI), WebLauncher (Web server)
├── lib/                         # sqlite-jdbc-3.45.1.0.jar
├── web/                         # Modern Web Dashboard (HTML, CSS, JS)
├── bin/                         # Compiled bytecode (.class)
├── data/                        # SQLite DB (campus.db) & Serialized storage (.ser)
├── start-gui.bat / .sh          # 1-Click Launchers for Java Swing Desktop GUI
├── start-web.bat / .sh          # 1-Click Launchers for Web Dashboard
└── README.md
```

---

## 🖥️ Running the Java Swing Desktop GUI (KTU PBCST304 Module 4 / CO5)

The project includes an event-driven desktop GUI built with **Java Swing** backed by a **SQLite JDBC database layer** using the **Singleton Design Pattern**:

### 🖱️ Option 1: 1-Click Launch
* **Windows File Explorer**: Double-click [`start-gui.bat`](start-gui.bat)
* **PowerShell Terminal**: Run `.\start-gui.bat` *(Note the `.\` prefix required by PowerShell)*
* **Command Prompt (CMD)**: Run `start-gui.bat`
* **Linux / macOS**: Run `./start-gui.sh` in terminal

### 💻 Option 2: Terminal Launch
```powershell
# Compile all sources with SQLite JDBC driver
javac -cp ".;lib/*" -d bin (Get-ChildItem -Recurse -Filter *.java src | Select-Object -ExpandProperty FullName)

# Launch Desktop GUI
java -cp "bin;lib/*" com.campus.gui.CampusSwingApp
```
*(On Linux/macOS, use `:` instead of `;` in the classpath: `java -cp "bin:lib/*" com.campus.gui.CampusSwingApp`)*

### 🌟 Desktop Tabs Overview:
1. **🏟️ Sports Facilities**: Live court reservations, slot collision detection, fine status indicator, and instant cancellation.
2. **🏨 Hostel & Mess Billing**: Register new students in Single Occupancy or AC Suite, apply leave days, and generate itemized monthly receipts.
3. **📚 Library Catalog**: Borrow and return books with role quotas and automatic overdue penalty fines.
4. **📊 KTU Academics & Attendance (New)**:
   - Subject-wise attendance percentage tracking with color-coded **75% KTU minimum threshold alerts**.
   - Continuous Internal Evaluation (**CIE / 50**) computation from Series Exams 1 & 2 and Assignments.
   - Live **KTU 10-Point SGPA calculation** with credit weighting and Degree Classification badge.

---

## ⚡ Quick Start: 3 Ways to Run the Web Version

### 🖱️ Option 1: 1-Click Launch (Easiest for Windows)
Simply double-click the **`start-web.bat`** file in your project folder!  
It will automatically compile the code, start the server, and pop open your browser to **[http://localhost:8080](http://localhost:8080)**.

*(On Linux / macOS, run `./start-web.sh`)*

---

### 💻 Option 2: Terminal Command
1. Open PowerShell or Terminal in this folder:
   ```powershell
   cd CampusAcademicSystems
   ```
2. Start the web server:
   ```powershell
   java -cp bin com.campus.main.WebLauncher
   ```
3. Open your browser: **[http://localhost:8080](http://localhost:8080)**  
   *(To stop the server, press `q` and `Enter`, or press `Ctrl + C`)*

---

### 🌐 Option 3: Zero-Setup In-Browser Web Demo
You can also open the dashboard **without running any commands or even installing Java**:
* Simply double-click [`web/index.html`](file:///c:/Users/Nikhil%20Biju/.gemini/antigravity-ide/scratch/CampusAcademicSystems/web/index.html) in your file manager to open it in Chrome, Edge, or Firefox!
* Or host it as a free live website via **GitHub Pages** (Settings ➔ Pages ➔ Source: `main` branch / root).

> [!NOTE]
> When the Java backend is active (`WebLauncher`), the web app connects to the Java server and `.ser` database. When running standalone, it automatically uses client-side storage so it remains 100% interactive anywhere!

---

## 🚀 Detailed Execution & Compilation Guide

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

#### Option A: Modern Web Dashboard (Recommended for Demonstrations)
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
Executes the comprehensive 15-test verification harness covering OOP inheritance, slot collisions, quota caps, fine holds, polymorphism tariffs, library loans, overdue penalties, and serialization.

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

---

## 🌐 Internet Deployment Guide

You can deploy the Campus Academic Management System to the public internet using three different methods:

### Method 1: Instant Client-Side Deployment (GitHub Pages — 100% Free)
The frontend (`index.html` + `web/app.js`) features built-in fallback mock-state persistence. Anyone browsing the GitHub Pages site can test court bookings, hostel billing calculators, tab switching, and interactive features with zero server setup required.

1. Navigate to your repository on GitHub: [`https://github.com/nikhilbijuk/CampusAcademicSystems`](https://github.com/nikhilbijuk/CampusAcademicSystems)
2. Go to **Settings** > **Pages** (in the left sidebar).
3. Under **Build and deployment** > **Branch**:
   * Select **`main`** branch.
   * Select **`/ (root)`** folder.
   * Click **Save**.
4. Within 1–2 minutes, your web application will be live at:
   ```
   https://nikhilbijuk.github.io/CampusAcademicSystems/
   ```

### ⚖️ Live Deployments Comparison: Which URL to Use?

| Feature | 💻 **Replit URL** (Primary Full-Stack) | ⚡ **Cloudflare URL** (High-Availability Mirror) |
| :--- | :--- | :--- |
| **Live Address** | [https://campus-academic-systems--devnikhilbiju.replit.app](https://campus-academic-systems--devnikhilbiju.replit.app) | [https://campus-systems.dev-nikhilbiju.workers.dev](https://campus-systems.dev-nikhilbiju.workers.dev) |
| **Runtime Environment** | **Real Cloud JVM (OpenJDK 17 on Replit)** | Cloudflare Global Edge Network |
| **Backend Engine** | Pure Java HTTP Server (`com.sun.net.httpserver.HttpServer`) | Static Edge Engine |
| **OOP Domain Logic** | Real Java OOP classes (`Court`, `HostelStudent`, `Book`) | Client-side JavaScript fallback |
| **REST APIs** | Live endpoints (`/api/data`, `/api/reserve`, `/api/save`) | N/A |
| **Data Persistence** | Native Java Object Serialization (`data/campus_data.ser`) | Browser `localStorage` |
| **Recommended For** | **Official evaluation, teacher review, full-stack demonstration** | **Fast portfolio preview, backup mirror with zero downtime** |

---

### Method 2: Replit Cloud Deployment (Primary Java Backend)
The project is pre-configured with [`.replit`](.replit) and [`replit.nix`](replit.nix) to compile and run natively on OpenJDK 17.

* **Live URL**: [https://campus-academic-systems--devnikhilbiju.replit.app](https://campus-academic-systems--devnikhilbiju.replit.app)
* **How It Runs**:
  1. Cloned directly from GitHub into Replit.
  2. Provisions OpenJDK 17 via Nix environment.
  3. Executes `start-web.sh`, compiling all `.java` sources into `bin/`.
  4. Binds dynamically to `$PORT` and exposes live REST endpoints.

---

### Method 3: Cloudflare Edge Deployment (High-Availability Mirror)
Hosted on Cloudflare's global edge network across 300+ cities:

* **Live URL**: [https://campus-systems.dev-nikhilbiju.workers.dev](https://campus-systems.dev-nikhilbiju.workers.dev)
* **Key Benefits**:
  * 100% Free with unlimited bandwidth.
  * Zero cold-start latency.
  * Works as a reliable backup during presentations.

---

### Method 4: Instant Live Tunneling (Localtunnel / Ngrok)
To demonstrate your locally running Java server to an external evaluator or mobile phone over public Wi-Fi without cloud dependencies:

1. Start your local Java Web Launcher:
   ```powershell
   java -cp bin com.campus.main.WebLauncher
   ```
2. Open a second terminal window and run:
   ```bash
   npx localtunnel --port 8080
   ```
   *(Or using ngrok: `ngrok http 8080`)*
3. Share the generated public HTTPS URL (e.g., `https://clean-campus-app.loca.lt`) to access your local machine live from anywhere!



