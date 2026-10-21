# Adi Shankara Institute of Engineering & Technology
### Department of Computer Science & Engineering
## APJ Abdul Kalam Technological University (KTU) Mini Project / B.Tech Project Report Compendium

---

# Comprehensive Project Report Compendium: Campus Academic Systems (Enterprise Multi-Tier Architecture)

---

## Abstract

Modern educational institutions require robust, coherent, and integrated systems to manage non-academic facilities alongside strict academic compliance regulations. Traditional disparate campus management systems suffer from data fragmentation, manual reconciliation overheads, and inconsistent policy enforcement across facility booking, hostel residency, library circulation, and academic evaluations. 

This project presents the **Campus Academic Management System**, an enterprise-grade, multi-tier Object-Oriented software architecture engineered in pure Java (JDK 17) adhering strictly to KTU B.Tech curriculum standards (Course Code: PBCST304 / CST205). The system integrates five core operational pillars:
1. **Sports & Facilities Reservation Engine** with real-time slot collision detection, role-based quota ceilings (Student: 2, Faculty: 5, Coach: 10), and multi-day isolation.
2. **Hostel Residency & Dynamic Mess Billing Subsystem** featuring polymorphic room tariffs (`SingleOccupancy`, `ACSuite`) and automated leave rebate deductions.
3. **Central Library Management Subsystem** with checkout privileges, overdue penalty tracking (unified at Rs. 5.00/day), and an automated cross-module fine enforcement policy where any unsettled liability ($> \text{Rs. } 0$) freezes sports reservations.
4. **KTU Academic Performance & Attendance Tracker** implementing the mandatory 75% KTU attendance threshold classification, 50-mark Continuous Internal Evaluation (CIE), and credit-weighted Semester Grade Point Average (SGPA) calculation on the 10-point KTU scale.
5. **Dual Persistence & Interface Architecture** comprising a thread-safe Singleton `DatabaseManager` with 10 normalized SQLite tables for a desktop Java Swing GUI, a lightweight embedded HTTP server exposing 12 RESTful API endpoints, and a responsive web dashboard.

System correctness is validated through an exhaustive 28-case automated unit test harness (`CampusTestHarness`) achieving 100% pass rates across boundary limits, custom checked exceptions, concurrency isolation, and polymorphic calculations.

---

# Table of Contents (40-Page Report Structure)

- **Chapter 1: Introduction**
  - 1.1 Background & Motivation
  - 1.2 Problem Statement & Existing Deficiencies
  - 1.3 Project Objectives
  - 1.4 Scope and Applicability
  - 1.5 Organization of the Report
- **Chapter 2: Literature Review & Academic Framework**
  - 2.1 Comparative Review of Campus ERPs
  - 2.2 APJ Abdul Kalam Technological University (KTU) Regulations
  - 2.3 Architectural Paradigms: Monolith vs. Micro-services vs. Multi-Tier
  - 2.4 Rationale for Core Java and Zero-Dependency Tooling
- **Chapter 3: Software Requirements Specification (SRS)**
  - 3.1 Functional Requirements by Module
  - 3.2 Non-Functional Requirements (Security, Integrity, Performance, Portability)
  - 3.3 Hardware, Software, and Network Interfaces
  - 3.4 Custom Checked Domain Exceptions Catalog
- **Chapter 4: System Architecture & SOLID Design Principles**
  - 4.1 High-Level Multi-Tier Architecture
  - 4.2 Comprehensive SOLID Principles Mapping (SRP, OCP, LSP, ISP, DIP)
  - 4.3 Design Patterns Applied (Singleton, DAO, Model-View-Controller)
  - 4.4 Relational Data Model & Normalized SQLite Schema (10 Tables)
- **Chapter 5: Detailed Module Design & Implementation**
  - 5.1 Sports Facility & Dynamic Slot Reservation Engine
  - 5.2 Hostel Accommodation & Automated Leave Rebate Mess Billing
  - 5.3 Central Library Circulation & Cross-Module Fine Enforcement
  - 5.4 KTU Academic Attendance, CIE & SGPA Computation Engine
  - 5.5 Dual-Interface Layer: Java Swing Desktop GUI & Responsive Web Portal
  - 5.6 Persistence Strategies: SQLite Relational JDBC vs. Native Serialization
  - 5.7 RESTful API Subsystem (12 Endpoints)
- **Chapter 6: Software Verification, Testing & Quality Assurance**
  - 6.1 Verification Methodology & Test Strategy
  - 6.2 Complete Test Suite Matrix (Tests 1 to 28 Detailed)
  - 6.3 Cross-Module Integration & Security Boundary Verification
  - 6.4 Exception Handling & Fault Tolerance Analysis
- **Chapter 7: Results, Evaluation & Visual Analysis**
  - 7.1 Visual Interface Analysis (Walkthrough of Figures 8.1 to 8.6)
  - 7.2 Web Application vs. Desktop Application Feature Matrix
  - 7.3 Performance Metrics & Memory Footprint Analysis
- **Chapter 8: Deployment & Reproducibility Guide**
  - 8.1 Offline Deployment via Standalone Archive
  - 8.2 Cloud Deployment on Replit Platform
  - 8.3 Live Mobile Viewport & Multi-Device Operation
- **Chapter 9: Conclusion & Future Scope**
  - 9.1 Summary of Contributions
  - 9.2 Limitations of the Current Implementation
  - 9.3 Directions for Future Enhancements
- **References & Appendices**
  - Appendix A: SQLite Database DDL Scripts
  - Appendix B: REST API Specification Table
  - Appendix C: Complete 28-Test Execution Output

---

# Chapter 1: Introduction

### 1.1 Background & Motivation
In modern collegiate environments such as Adi Shankara Institute of Engineering & Technology, operational efficiency relies heavily on managing both physical infrastructure and student records. Campus facilities—such as badminton courts, tennis courts, synthetic football turfs, cricket grounds, hostel residence rooms, dining halls, and central libraries—are typically administered by independent clerical offices using manual logbooks or isolated spreadsheets. This operational fragmentation leads to scheduling collisions, uncollected overdue library penalties, inconsistent mess rebate credits, and an inability to enforce institutional disciplinary holds across departments.

Concurrently, under the APJ Abdul Kalam Technological University (KTU) B.Tech curriculum, academic monitoring requires strict compliance with specific credit frameworks, continuous internal evaluation criteria, and attendance minimums. Integrating these operational workflows into a unified, extensible Object-Oriented software ecosystem provides an ideal platform to demonstrate software engineering rigor.

### 1.2 Problem Statement & Existing Deficiencies
Existing campus tools exhibit several critical weaknesses:
1. **Siloed Databases**: A student with significant outstanding library fines can continue reserving sports facilities without restriction because the sports department has no visibility into library loan ledgers.
2. **Arbitrary Manual Billing**: Calculating hostel mess charges by manually tallying leave registers leads to clerical errors, delayed fee notices, and student disputes.
3. **Lack of Concurrency Control**: Slot bookings made simultaneously by multiple users result in double bookings, unauthorized cancellations, or quota overruns.
4. **Disconnected Academic Tracking**: Attendance percentages and continuous evaluation (CIE) marks are rarely linked with institutional student clearance statuses.

### 1.3 Project Objectives
The primary objectives of this project are:
- To design a clean, modular, and extensible Object-Oriented domain model in Java (JDK 17) adhering strictly to SOLID design principles.
- To implement dynamic resource reservation with role-based quotas (Student: 2, Faculty: 5, Coach: 10), collision prevention, and date-aware slot scheduling across 5 facilities.
- To automate hostel accommodation management and itemized mess billing with mathematical leave rebate adjustments ($R_m = \max(0, (D_m - L) \times r_{\text{daily}})$).
- To engineer a cross-module penalty enforcement bridge whereby any unpaid library fine ($> \text{Rs. } 0$) immediately locks campus clearance and blocks sports bookings.
- To implement the KTU academic grading framework: 75% attendance threshold classification, CIE computation out of 50 marks, and credit-weighted SGPA calculation on a 10-point scale.
- To provide dual presentation interfaces: a desktop Java Swing application backed by SQLite JDBC and an embedded RESTful web server serving a responsive web dashboard.
- To validate system integrity through a comprehensive 28-case automated test suite.

---

# Chapter 2: Literature Review & Academic Framework

### 2.1 Comparative Analysis
Enterprise ERP systems (e.g., SAP Campus Management, Ellucian Banner) provide exhaustive functionality but introduce high licensing costs, proprietary dependencies, and excessive complexity for medium-sized technical colleges. Lightweight open-source tools often focus narrowly on a single task (such as a library catalog) while neglecting cross-module policy enforcement. The architecture presented here bridges this gap by implementing lightweight, pure Java multi-tier software with zero external dependencies beyond standard JDBC drivers.

### 2.2 KTU Academic Regulations Framework
The system implements the academic evaluation rules specified by KTU:
- **Attendance Mandate**: Minimum 75% attendance required in each registered course to be eligible for the End Semester Examination (ESE). Attendance between 60% and 74% requires institutional condonation on medical/approved grounds. Shortages below 60% result in course detention (`FE` grade).
- **Continuous Internal Evaluation (CIE)**: 50 total marks divided into Series Test 1 (max 20 marks), Series Test 2 (max 20 marks), and Assignments/Tutorials (max 10 marks).
- **Letter Grading & Grade Points**:
  $$\text{Scale: } S = 10.0, A^+ = 9.0, A = 8.5, B^+ = 8.0, B = 7.5, C^+ = 7.0, C = 6.5, D = 6.0, P = 5.5, F/FE = 0.0$$
- **SGPA Calculation Formula**:
  $$\text{SGPA} = \frac{\sum_{i=1}^{n} (C_i \times G_i)}{\sum_{i=1}^{n} C_i}$$
  Where $C_i$ denotes course credit weight and $G_i$ represents the earned grade point.

---

# Chapter 3: Software Requirements Specification (SRS)

### 3.1 Functional Requirements
- **FR-1 [Sports Facility Reservation]**: Users must browse availability across 5 sports facilities (Badminton, Tennis, Basketball, Football Turf, Cricket Ground) and book hour slots. The system must prevent duplicate bookings (`SlotAlreadyBookedException`) and enforce role quotas (`BookingQuotaExceededException`).
- **FR-2 [Hostel & Mess Billing]**: The system must calculate monthly hostel rent based on room type (`SingleOccupancy` at 20% surcharge or `ACSuite` at flat +Rs. 1500 add-on). Approved leave days must deduct Rs. 150.00/day from the standard mess tariff. Leaves exceeding monthly days must be rejected (`InvalidLeaveDaysException`).
- **FR-3 [Central Library & Circulation]**: Users can checkout available catalog titles up to their quota ceiling. Returning overdue titles must levy a penalty of Rs. 5.00 per day overdue.
- **FR-4 [Cross-Module Clearance Enforcement]**: The system must inspect `User.getFineBalance()`. If balance $> 0$, facility reservations must be blocked with an `OutstandingFineException`.
- **FR-5 [KTU Academic Performance Engine]**: Must calculate course attendance %, trigger `LowAttendanceException` if $< 75\%$, validate internal marks within legal limits (`InvalidMarkException`), and calculate credit-weighted SGPA.
- **FR-6 [Administrative Overrides]**: Administrative users (`ADM-014` / `Faculty`) must have authorization to release any reserved slot and adjust disciplinary records.

### 3.2 Non-Functional Requirements
- **Performance**: Sub-50ms local API response times on an embedded lightweight HTTP server.
- **Data Integrity**: Thread-safe database transactions and synchronized data structures to prevent race conditions during concurrent slot reservations.
- **Platform Portability**: 100% portable Java bytecode executing identically on Windows, Linux, and macOS.
- **Zero Third-Party Runtime Burden**: Zero complex enterprise container dependencies; all REST handling is managed via native `com.sun.net.httpserver`.

---

# Chapter 4: System Architecture & SOLID Design Principles

### 4.1 Multi-Tier Architecture
The software is organized into four clean layers:
1. **Presentation Layer**: 
   - Native Java Swing Desktop Application (`com.campus.gui.CampusSwingApp`).
   - HTML5/CSS3/Vanilla JS Responsive Web Portal (`web/index.html`, `login.html`, `student-dashboard.html`, `admin-dashboard.html`).
2. **Controller & Communication Layer**:
   - Lightweight Java HTTP REST Server (`CampusWebServer`) exposing 12 REST API endpoints.
   - Command-Line Interface (`MainApp`).
3. **Domain Business Logic Layer**:
   - Pure OOP domain classes (`Court`, `User`, `Student`, `Faculty`, `Coach`, `HostelStudent`, `BaseRoom`, `Book`, `LibraryLoan`, `Course`, `AttendanceRecord`, `InternalAssessment`).
4. **Data Persistence Layer**:
   - Data Access Object (`CampusDao`) interacting via JDBC with SQLite database (`data/campus.db`).
   - Serialization Manager (`CampusStorageManager`) serializing object graphs to `data/campus_data.ser`.

### 4.2 Comprehensive SOLID Principles Mapping

| SOLID Principle | Meaning in OOD | Exact Application in Campus Academic Systems |
| :--- | :--- | :--- |
| **S - Single Responsibility Principle** | A class should have only one reason to change. | • `AttendanceRecord` manages *only* attendance counting and percentage checks.<br>• `InternalAssessment` computes *only* CIE scores.<br>• `DatabaseManager` handles *only* connection lifecycle and table DDL.<br>• `CampusDao` handles *only* SQL CRUD execution. |
| **O - Open/Closed Principle** | Classes open for extension, closed for modification. | • `BaseRoom` is extended by `SingleOccupancy` and `ACSuite`. Adding a new room type (e.g. `DeluxeTriple`) requires zero changes to existing room calculation code.<br>• `MealPlan` is extended by `StandardPlan` and `SpecialDietPlan`. |
| **L - Liskov Substitution Principle** | Derived classes must be substitutable for base types. | • Subclasses of `User` (`Student`, `Faculty`, `Coach`) can be passed interchangeably to `Court.reserve(slot, user, allCourts)`. Quotas are resolved polymorphically via `user.getBookingLimit()`. |
| **I - Interface Segregation Principle** | Clients should not depend on unused interfaces. | • The `Reservable` interface defines *only* booking operations (`checkAvailability`, `reserve`, `release`). It does not pollute sports facilities with hostel billing or library loan contracts. |
| **D - Dependency Inversion Principle** | Depend upon abstractions, not concrete classes. | • `HostelStudent` holds references to abstract classes `BaseRoom` and `MealPlan`, decoupling student residency from concrete room pricing implementations. |

### 4.3 Database Schema (10 Normalized Relational Tables)

```sql
-- 1. Users Table
CREATE TABLE users (
    user_id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    role TEXT NOT NULL,
    booking_limit INTEGER NOT NULL,
    fine_balance REAL DEFAULT 0.0
);

-- 2. Courts Table
CREATE TABLE courts (
    court_id TEXT PRIMARY KEY,
    court_type TEXT NOT NULL
);

-- 3. Court Reservations Table
CREATE TABLE court_reservations (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    court_id TEXT NOT NULL,
    slot TEXT NOT NULL,
    user_id TEXT NOT NULL,
    reserved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (court_id) REFERENCES courts(court_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- 4. Hostel Students Table
CREATE TABLE hostel_students (
    roll_no TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    room_number INTEGER NOT NULL,
    room_type TEXT NOT NULL,
    room_tariff REAL NOT NULL,
    meal_plan TEXT NOT NULL,
    leaves_this_month INTEGER DEFAULT 0
);

-- 5. Library Books Table
CREATE TABLE library_books (
    isbn TEXT PRIMARY KEY,
    title TEXT NOT NULL,
    author TEXT NOT NULL,
    category TEXT NOT NULL,
    is_available INTEGER DEFAULT 1,
    borrower_id TEXT,
    borrower_name TEXT
);

-- 6. Library Loans Table
CREATE TABLE library_loans (
    loan_id INTEGER PRIMARY KEY AUTOINCREMENT,
    isbn TEXT NOT NULL,
    user_id TEXT NOT NULL,
    borrow_date DATE NOT NULL,
    due_date DATE NOT NULL,
    return_date DATE,
    fine_charged REAL DEFAULT 0.0,
    is_returned INTEGER DEFAULT 0
);

-- 7. Academic Courses Table
CREATE TABLE academic_courses (
    course_code TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    credits INTEGER NOT NULL,
    semester INTEGER NOT NULL
);

-- 8. Student Attendance Table
CREATE TABLE student_attendance (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    student_id TEXT NOT NULL,
    course_code TEXT NOT NULL,
    classes_attended INTEGER NOT NULL,
    total_classes INTEGER NOT NULL
);

-- 9. Student Internals (CIE) Table
CREATE TABLE student_internals (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    student_id TEXT NOT NULL,
    course_code TEXT NOT NULL,
    series_1 REAL NOT NULL,
    series_2 REAL NOT NULL,
    assignments REAL NOT NULL,
    total_cie REAL NOT NULL
);

-- 10. Student Course Grades Table
CREATE TABLE student_course_grades (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    student_id TEXT NOT NULL,
    course_code TEXT NOT NULL,
    grade TEXT NOT NULL,
    grade_point REAL NOT NULL
);
```

---

# Chapter 5: Detailed Module Design & Implementation

### 5.1 Sports Facility & Dynamic Slot Reservation Engine
- **Facilities Model**: 5 facilities seeded (`CRT1`: Badminton, `CRT2`: Tennis, `CRT3`: Basketball, `CRT4`: Football Turf, `CRT5`: Cricket Ground).
- **Time Slots**: 7 discrete daily operational slots (`06:00-07:00`, `07:00-08:00`, `08:00-09:00`, `16:00-17:00`, `17:00-18:00`, `18:00-19:00`, `19:00-20:00`).
- **Quota Model**: Enforced polymorphically via `user.getBookingLimit()`:
  - `Student`: 2 concurrent slots.
  - `Faculty`: 5 concurrent slots.
  - `Coach`: 10 concurrent slots.
- **Collision & Release Logic**: Synchronized locking on court objects prevents race conditions. Normal users can only release slots they personally booked; administrative users (`ADM-014`, `Faculty`) possess system override privileges.

### 5.2 Hostel & Automated Mess Billing Module
- **Room Tariff Computation**:
  - `SingleOccupancy`: Surcharge of 20% on base tariff: $T_{\text{single}} = T_{\text{base}} \times 1.20$.
  - `ACSuite`: Fixed luxury surcharge of Rs. 1,500.00: $T_{\text{ac}} = T_{\text{base}} + 1500.00$.
- **Mess Rebate Formula**:
  $$\text{Mess Charge} = \max\left(0, (D_{\text{month}} - L_{\text{approved}}) \times 150.00\right)$$
  Where $D_{\text{month}} = 30$ days and $L_{\text{approved}}$ represents approved leave days.

### 5.3 Central Library Circulation & Unified Fine Policy
- **Borrowing Limit**: Students up to 3 books, Faculty up to 10 books.
- **Unified Overdue Fine**: Standardized across web and desktop platforms to **Rs. 5.00 / day overdue**.
- **Automated Fine Propagation**: Overdue returns execute:
  ```java
  borrower.addFine(overdueDays * DAILY_OVERDUE_FINE);
  ```
  Any outstanding balance ($> \text{Rs. } 0$) immediately trips `user.getFineBalance() > 0`, causing subsequent `Court.reserve()` invocations to fail with `OutstandingFineException`.

### 5.4 KTU Academic Performance & Attendance Tracker
- **Attendance Verification**:
  $$\text{Attendance } \% = \left(\frac{\text{Classes Attended}}{\text{Total Classes}}\right) \times 100$$
  - $\ge 75\%$: Eligible (`STATUS_ELIGIBLE`).
  - $60\% - 74.9\%$: Condonation Required (`STATUS_CONDONATION`).
  - $< 60\%$: Detained / Attendance Shortage (`LowAttendanceException`).
- **CIE Marks Formulation**:
  $$\text{CIE (Max 50)} = \text{Series 1 (Max 20)} + \text{Series 2 (Max 20)} + \text{Assignments (Max 10)}$$
  Any mark exceeding category ceilings throws `InvalidMarkException`.

### 5.5 RESTful Web Server Endpoints (12 Endpoints)

| HTTP Method | Endpoint URI | Description |
| :--- | :--- | :--- |
| `GET` | `/api/data` | Returns complete system state as structured JSON. |
| `POST` | `/api/reserve` | Books a court slot with collision and quota checks. |
| `POST` | `/api/release` | Cancels/releases a booked slot with authorization validation. |
| `POST` | `/api/modify` | Reschedules an existing booking atomically to a new slot. |
| `POST` | `/api/fine` | Settles or records student fine account balances. |
| `POST` | `/api/hostel/register` | Registers a student into hostel residency. |
| `POST` | `/api/hostel/leave` | Records approved mess leaves and calculates rebate. |
| `GET` | `/api/hostel/bill` | Generates itemized monthly rent and meal fee statement. |
| `POST` | `/api/library/borrow` | Checks out a book to a user. |
| `POST` | `/api/library/return` | Processes book return, calculates overdue fines. |
| `GET` | `/api/academic` | Retrieves KTU attendance, CIE, and SGPA calculations. |
| `POST` | `/api/save` | Persists live state to disk (`campus_data.ser`). |

---

# Chapter 6: Software Verification, Testing & Quality Assurance

### 6.1 Testing Strategy
The project implements a comprehensive automated test harness ([`CampusTestHarness.java`](src/com/campus/test/CampusTestHarness.java)) covering unit functionality, mathematical models, boundary limits, and cross-module transactions.

### 6.2 The Complete 28-Case Unit Test Suite Matrix

| Test ID | Test Name | Target Class & Method | Expected Behavior & Verification Rule | Status |
| :---: | :--- | :--- | :--- | :---: |
| **01** | Successful Court Booking | `Court.reserve()` | Normal reservation by Student on empty slot succeeds. | **PASS** |
| **02** | Slot Collision Detection | `Court.reserve()` | Booking an already reserved slot throws `SlotAlreadyBookedException`. | **PASS** |
| **03** | Role Quota Cap Enforcement | `Court.reserve()` | Attempting to exceed Student limit (2) throws `BookingQuotaExceededException`. | **PASS** |
| **04** | Outstanding Fine Hold | `Court.reserve()` | User with fine balance $> 0$ throws `OutstandingFineException`. | **PASS** |
| **05** | Negative Leave Validation | `HostelStudent.recordLeave()` | Supplying negative leave days throws `InvalidLeaveDaysException`. | **PASS** |
| **06** | State Serialization & Load | `CampusStorageManager` | Serializes complete state to disk and restores identical object graph. | **PASS** |
| **07** | ACSuite Monthly Tariff | `ACSuite.calculateTariff()` | Base 5000 + 1500 surcharge correctly evaluates to Rs. 6500.0. | **PASS** |
| **08** | SingleOccupancy Tariff | `SingleOccupancy.calculateTariff()` | Base 4000 * 1.20 surcharge correctly evaluates to Rs. 4800.0. | **PASS** |
| **09** | Cancellation Authorization | `Court.release()` | Intruder cannot release another's slot; owner can release successfully. | **PASS** |
| **10** | Excessive Leave Validation | `HostelStudent.recordLeave()` | Leave days $> 30$ in a 30-day month throws `InvalidLeaveDaysException`. | **PASS** |
| **11** | Itemized Hostel Bill & Rebate | `HostelStudent.calculateMonthlyBill()` | Monthly bill correctly subtracts leave days * Rs. 150 from mess total. | **PASS** |
| **12** | Book Checkout & Loan Status | `Book.borrow()` | Available book is loaned, marked unavailable, borrower recorded. | **PASS** |
| **13** | Unavailable Book Rejection | `Book.borrow()` | Borrowing already loaned book throws `BookNotAvailableException`. | **PASS** |
| **14** | Overdue Book Penalty Math | `LibraryLoan.completeReturn()` | 4 days overdue * Rs. 5/day adds exactly Rs. 20.0 fine to borrower. | **PASS** |
| **15** | Cross-Module Integration | `LibraryLoan` → `Court` | Unsettled overdue library fine immediately blocks sports court reservation. | **PASS** |
| **16** | KTU 75% Attendance Check | `AttendanceRecord.isEligible()` | 36/40 attended (90%) evaluates to `Eligible` with zero exception. | **PASS** |
| **17** | KTU Shortage Detainment | `AttendanceRecord.verifyEligibility()` | 22/40 attended (55%) triggers `LowAttendanceException`. | **PASS** |
| **18** | CIE Calculation & Mark Limits | `InternalAssessment` | Series 18+19 + Assignment 9.5 = 46.5/50. Marks $> 20$ throw `InvalidMarkException`. | **PASS** |
| **19** | Weighted SGPA Calculation | `AcademicProfile.calculateSgpa()` | Credit-weighted grade points produce exact SGPA (9.30 - Distinction). | **PASS** |
| **20** | Robust JSON Parser Engine | `JsonUtils.parseJsonObject()` | Correctly parses nested strings, unicode, escaped quotes, primitives. | **PASS** |
| **21** | JSON String Escaping Utility | `JsonUtils.escapeJson()` | Escapes control characters, tabs, and newlines safely for JSON output. | **PASS** |
| **22** | User Hierarchy Quotas | `User`, `Student`, `Faculty`, `Coach` | Verifies dynamic polymorphic limits (Student: 2, Faculty: 5, Coach: 10). | **PASS** |
| **23** | Squad Size Validation | `Court.reserve(squad)` | Football 11v11 match with $< 22$ players throws `InsufficientPlayersException`. | **PASS** |
| **24** | Full Team Roster Reservation | `Court.reserve(squad)` | 22-player roster validated and booked successfully for turf pitch. | **PASS** |
| **25** | Atomic Slot Rescheduling | `Court.reschedule()` | Atomically frees old slot and books new slot without race conditions. | **PASS** |
| **26** | Slot Deletion Verification | `Court.release()` | Slot released, booking cleared, availability returns true. | **PASS** |
| **27** | Fuzzy Slot Matching & Override | `Court.release()` | Prefix slot format ("06:00") and Admin override release verified. | **PASS** |
| **28** | Multi-Day Date-Aware Isolation | `Court.reserve(dateSlot)` | Booking on `2026-10-14` leaves same slot on `2026-10-15` available. Admin `ADM-014` override verified. | **PASS** |

---

# Chapter 7: Results, Evaluation & Discussion

### 7.1 Report Figures Reference Matrix

| Figure | Title / Caption | Source File / Window | Key Visual Details |
| :---: | :--- | :--- | :--- |
| **Figure 8.1** | Portal Landing Page | `screenshots/01_portal_landing_page.png` | Live Replit deployment URL (`campus-academic-systems--devnikhilbiju.replit.app`), hero section, 5 facilities metric. |
| **Figure 8.2** | Student & Admin Login Screen | `screenshots/02_login_screen.png` | Role tab selector, institutional demo credentials, clean responsive typography. |
| **Figure 8.3** | Student Dashboard Overview | `screenshots/03_student_dashboard_overview.png` | Attendance 90%, CGPA 9.30, active bookings, hostel room allocation (Room 301), library loan cards. |
| **Figure 8.4** | Administrator Dashboard | `screenshots/07_admin_dashboard.png` | 5 Sports Facilities, campus account fine clearances, resident room rosters, inventory controls. |
| **Figure 8.5** | Java Swing Desktop Application | `screenshots/08_swing_desktop_gui.png` | Live desktop JFrame, green status: *"Connected to SQLite via Singleton DatabaseManager"*, acting user selector, sports facility table with CRT1 booked by S101. |
| **Figure 8.6** | DB Browser for SQLite View | `data/campus.db` in DB Browser | Table `court_reservations` displaying row 1: `id: 6, court_id: CRT1, slot: 06:00-07:00, user_id: S101`, status bar showing `campus.db (SQLite v3.45.1)`. |

### 7.2 Web Application vs. Desktop Swing Application Comparison

| Feature / Dimension | Web Application (`CampusWebServer`) | Desktop Application (`CampusSwingApp`) |
| :--- | :--- | :--- |
| **Presentation Technology** | HTML5, Vanilla CSS3, Modern JavaScript | Java Swing (`JFrame`, `JTable`, `JTabbedPane`) |
| **Persistence Mechanism** | Native Java Object Serialization (`data/campus_data.ser`) | SQLite Relational Database via JDBC (`data/campus.db`) |
| **Network Architecture** | Client-Server over HTTP REST (`localhost:8080` / Cloud) | Local Standalone MVC Desktop Process |
| **Concurrent Access** | Multi-client web browser access | Local single-workstation administrative console |
| **Overdue Fine Rate** | Unified at **Rs. 5.00 / day** | Unified at **Rs. 5.00 / day** |
| **Quota Enforcement** | Student: 2, Faculty: 5, Coach: 10 | Student: 2, Faculty: 5, Coach: 10 |
| **Fine Blocking Threshold** | Any balance $> \text{Rs. } 0$ blocks bookings | Any balance $> \text{Rs. } 0$ blocks bookings |

---

# Chapter 8: Conclusion & Future Scope

### 8.1 Summary of Contributions
The Campus Academic Management System demonstrates that enterprise campus operations and strict university academic compliance can be harmoniously integrated into a pure, clean, zero-dependency Java architecture. By applying SOLID principles, design patterns (Singleton, DAO, MVC), custom domain exceptions, and dual persistence (SQLite JDBC and Object Serialization), the system provides an educational benchmark for KTU B.Tech project evaluations.

### 8.2 Future Scope
1. **Biometric Integration**: Automated hardware fingerprint/RFID scanners at dining hall entry gates to automatically log mess leaves and meal attendance.
2. **Payment Gateway Integration**: Direct Razorpay/UPI gateway integration to allow students to settle outstanding library penalties online in real time.
3. **Automated Timetable Collision Engine**: Cross-referencing university academic lecture timetables with sports bookings to prevent students from reserving athletic facilities during scheduled class hours.
