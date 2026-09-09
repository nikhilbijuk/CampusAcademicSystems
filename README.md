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

## 🚀 Getting Started

### Prerequisites
- Java Development Kit (JDK 8 or higher)
- Git

### Build & Compilation
From the project root folder:
```powershell
# Compile all Java source files into bin/
javac -d bin (Get-ChildItem -Recurse -Filter *.java | Select-Object -ExpandProperty FullName)
```

### Option A: Run Modern Web Dashboard (Recommended for Demonstrations)
```powershell
# Launch the Web Server & Dashboard at http://localhost:8080
java -cp bin com.campus.main.WebLauncher
```
Open your browser and navigate to: **`http://localhost:8080`**

### Option B: Run Interactive Terminal CLI
```powershell
# Launch the Interactive Console Runner
java -cp bin com.campus.main.MainApp
```

### Run Unit Tests
```powershell
# Execute the Unit Test Suite (11 test cases)
java -cp bin com.campus.test.CampusTestHarness
```
