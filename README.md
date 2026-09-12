# Campus Academic Management System (KTU Mini Project Framework)

> 🚀 **Live Deployments**:
> * 📦 **Primary Full-Stack Deployment (Real Java Backend in Docker)**: [https://campusacademicsystems-ccjf7nuu.b4a.run/](https://campusacademicsystems-ccjf7nuu.b4a.run/)  
>   *(Powered by Back4App Containers running Eclipse Adoptium OpenJDK 17, live REST APIs `/api/*`, pure OOP domain logic, and native `.ser` serialization)*
> * ⚡ **High-Availability Static Mirror (Cloudflare Edge)**: [https://campus-systems.dev-nikhilbiju.workers.dev](https://campus-systems.dev-nikhilbiju.workers.dev)  
>   *(Zero-downtime client-side mirror hosted on Cloudflare's global edge network)*

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
- **Campus Library Module (Cross-Module Unified Fine Engine)**:
  - Textbook catalog management (`Book`, `LibraryLoan`) with role checkout quotas (`Student`: max 3 books, `Faculty`: max 10 books).
  - Overdue return calculation (`Rs. 5.00 / day`).
  - Cross-module fine propagation: overdue library fines automatically link to `User.getFineBalance()`, instantly freezing sports court bookings until cleared.
- **Persistence & Storage**:
  - Native Java Object Serialization (`.ser`) for saving/loading system state automatically.
  - Built-in data seed routines for fresh initializations.
- **Interactive CLI & Unit Tests**:
  - Full-featured 9-option interactive command-line interface.
  - Automated 15-case unit test suite (`CampusTestHarness`).

---

## 🏛️ Backend Architecture & Tech Stack

The backend is built with **100% Pure Core Java (JDK 8+)**, intentionally structured with **zero external third-party dependencies** to adhere strictly to KTU and academic evaluation standards:

| Layer | Technology | Key Classes | Responsibilities |
| :--- | :--- | :--- | :--- |
| **HTTP Server & REST APIs** | `com.sun.net.httpserver.HttpServer` | [`CampusWebServer`](src/com/campus/web/CampusWebServer.java), [`JsonUtils`](src/com/campus/web/JsonUtils.java) | Exposes lightweight JSON REST endpoints (`/api/*`), serves static frontend files, binds dynamically to `$PORT`. |
| **Domain & Business Logic** | Pure Java OOP (Polymorphism, Inheritance) | [`Court`](src/com/campus/sports/Court.java), [`User`](src/com/campus/sports/User.java), [`HostelStudent`](src/com/campus/hostel/HostelStudent.java), [`Book`](src/com/campus/library/Book.java) | Enforces role quota ceilings, room tariffs, meal plans, and overdue library penalties. |
| **Custom Checked Exceptions** | Domain Exception Subclasses | `SlotAlreadyBookedException`, `OutstandingFineException`, `BookingQuotaExceededException`, `InvalidLeaveDaysException`, `BookNotAvailableException` | Enforces business rule validation and domain integrity. |
| **Persistence & Database** | Native Java Object Serialization (`.ser`) | [`CampusStorageManager`](src/com/campus/storage/CampusStorageManager.java), [`CampusData`](src/com/campus/storage/CampusData.java) | Saves and loads complete object graphs to `data/campus_data.ser` via `ObjectOutputStream` and `ObjectInputStream`. |

---

## 📁 Project Structure

```
CampusAcademicSystems/
├── src/
│   └── com/
│       └── campus/
│           ├── exceptions/      # Custom Exception classes
│           ├── hostel/          # BaseRoom, SingleOccupancy, ACSuite, MealPlan, HostelStudent
│           ├── library/         # Book, LibraryLoan
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
The frontend (`index.html` + `web/app.js`) features built-in fallback mock-state persistence. Anyone browsing the GitHub Pages site can test court bookings, hostel billing calculators, tab switching, and viva notes with zero server setup required.

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

| Feature | 📦 **Back4App URL** (Official Submission) | ⚡ **Cloudflare URL** (High-Availability Mirror) |
| :--- | :--- | :--- |
| **Live Address** | [https://campusacademicsystems-ccjf7nuu.b4a.run/](https://campusacademicsystems-ccjf7nuu.b4a.run/) | [https://campus-systems.dev-nikhilbiju.workers.dev](https://campus-systems.dev-nikhilbiju.workers.dev) |
| **Runtime Environment** | **Real Linux Docker Container (OpenJDK 17)** | Cloudflare Global Edge CDN |
| **Backend Engine** | Pure Java HTTP Server (`com.sun.net.httpserver.HttpServer`) | Static Asset Engine |
| **OOP Domain Logic** | Real Java OOP classes (`Court`, `HostelStudent`, `Book`) | Client-side JavaScript fallback |
| **REST APIs** | Live endpoints (`/api/data`, `/api/reserve`, `/api/save`) | N/A |
| **Data Persistence** | Native Java Object Serialization (`data/campus_data.ser`) | Browser `localStorage` |
| **Recommended For** | **Official KTU viva evaluation, teacher review, full-stack defense** | **Fast portfolio preview, backup mirror with zero downtime** |

---

### Method 2: Full-Stack Cloud Deployment (Back4App Containers, Railway & Render)
Because the project contains a standardized [`Dockerfile`](Dockerfile) running Eclipse Adoptium OpenJDK 17 and `WebLauncher` dynamically binds to any cloud `$PORT`, it can be deployed to any container platform:

#### 1. Back4App Containers (Verified Live Deployment)
* **Status**: 🟢 **Live**: [https://campusacademicsystems-ccjf7nuu.b4a.run/](https://campusacademicsystems-ccjf7nuu.b4a.run/)
* **Free Tier**: 100 free container execution hours/month, 256 MB RAM, 0.25 vCPU, zero credit card required.
* **Steps to Replicate**:
  1. Sign in to [back4app.com/containers](https://www.back4app.com/containers) using GitHub.
  2. Click **Create New App** > **Container as a Service (CaaS)**.
  3. Select **A Dockerfile** stack and connect `nikhilbijuk/CampusAcademicSystems`.
  4. Set **Port**: `8080` (HTTP).
  5. Back4App automatically detects `java -cp bin com.campus.main.WebLauncher` as start command.
  6. Click **Save and Deploy**.

#### 2. Replit (Permanent Free Java Hosting — Zero Setup)
* **Website**: [replit.com](https://replit.com)
* **Free Tier**: Native Java execution environment, permanent web URL (`*.replit.app`), no credit card required.
* **Pre-configured**: The repository includes a [`.replit`](.replit) config file for 1-click compilation and execution.
* **Steps to Deploy**:
  1. Go to [replit.com](https://replit.com) and sign in with GitHub.
  2. Click **+ Create Repl** > Select **Import from GitHub**.
  3. Paste your repository URL: `https://github.com/nikhilbijuk/CampusAcademicSystems`.
  4. Click **Import from GitHub**.
  5. Once imported, click the big green **▶ Run** button.
  6. Replit automatically runs `start-web.sh`, launches `WebLauncher` on port `8080`, and opens your permanent live web application in the Webview panel!

#### 3. Railway (Fastest Cloud Trial & 8 GB RAM)
* **Website**: [railway.app](https://railway.app)
* **Free Tier**: $5 starter trial credit (~500 container hours), no credit card required.
* **Steps**:
  1. Log in to Railway using GitHub.
  2. Click **New Project** > **Deploy from GitHub repo** > Select `CampusAcademicSystems`.
  3. Railway automatically detects [`Dockerfile`](Dockerfile) and deploys on a free public domain.

#### 4. Render (Standard Free PaaS)
* **Website**: [render.com](https://render.com)
* **Free Tier**: 512 MB RAM web service, sleeps after 15 mins of inactivity (~45s cold start).
* **Steps**:
  1. Sign up on Render.com > Click **New +** > **Web Service**.
  2. Connect `CampusAcademicSystems` repository.
  3. Render auto-selects `Docker` runtime and [`Dockerfile`](Dockerfile).
  4. Select **Free** instance type and click **Create Web Service**.

---

### Method 3: Cloudflare Deployment Options

Cloudflare offers two ways to deploy this system:

#### A. Cloudflare Pages (Frontend Web Hosting — 100% Free & Unlimited Bandwidth)
1. Sign up on [dash.cloudflare.com](https://dash.cloudflare.com).
2. Navigate to **Compute (Workers & Pages)** > **Pages** > **Connect to Git**.
3. Select your repository: `CampusAcademicSystems`.
4. Build Settings:
   * **Framework preset**: `None`
   * **Build command**: *(leave blank)*
   * **Build output directory**: `/`
5. Click **Save and Deploy**. Cloudflare gives you an instant `*.pages.dev` URL backed by their global 300+ city edge network.

#### B. Cloudflare Tunnel (Live Secure Tunnel from Your Laptop)
Expose your local Java server to a public HTTPS Cloudflare link without port forwarding:
1. Download `cloudflared` from Cloudflare or via winget:
   ```powershell
   winget install Cloudflare.cloudflared
   ```
2. Start the local Java Web Launcher:
   ```powershell
   java -cp bin com.campus.main.WebLauncher
   ```
3. In another terminal, run:
   ```powershell
   cloudflared tunnel --url http://localhost:8080
   ```
4. Cloudflare will output a free, encrypted public HTTPS link (e.g., `https://random-words.trycloudflare.com`) routing directly to your running Java server!

---

### Method 4: Instant Live Viva Tunneling (Localtunnel / Ngrok)
To demonstrate your locally running Java server to an external evaluator, viva examiner, or mobile phone over public Wi-Fi without deploying to the cloud:

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


