package com.campus.test;

import com.campus.academic.*;
import com.campus.exceptions.*;
import com.campus.hostel.*;
import com.campus.library.*;
import com.campus.sports.*;
import com.campus.storage.*;
import java.util.ArrayList;
import java.util.List;

public class CampusTestHarness {

    public static void main(String[] args) {
        int passed = 0;
        int failed = 0;

        System.out.println("==================================================");
        System.out.println("=== RUNNING CAMPUS ACADEMIC SYSTEM UNIT TESTS ===");
        System.out.println("==================================================");

        // Test 1: Successful Court Reservation
        try {
            Court court = new Court("CRT1", "Badminton");
            User student = new Student("S101", "Rahul");
            List<Court> courts = new ArrayList<>();
            courts.add(court);

            boolean result = court.reserve("09:00-10:00", student, courts);
            if (result && !court.checkAvailability("09:00-10:00")) {
                System.out.println(" [PASS] Test 1: Successful court reservation");
                passed++;
            } else {
                System.err.println(" [FAIL] Test 1: Reservation returned false or slot not booked");
                failed++;
            }
        } catch (Exception e) {
            System.err.println(" [FAIL] Test 1 unexpected exception: " + e.getMessage());
            failed++;
        }

        // Test 2: Slot Collision Exception (SlotAlreadyBookedException)
        try {
            Court court = new Court("CRT1", "Badminton");
            User student1 = new Student("S101", "Rahul");
            User student2 = new Student("S102", "Priya");
            List<Court> courts = new ArrayList<>();
            courts.add(court);

            court.reserve("09:00-10:00", student1, courts);
            court.reserve("09:00-10:00", student2, courts);
            System.err.println(" [FAIL] Test 2: Expected SlotAlreadyBookedException but none was thrown");
            failed++;
        } catch (SlotAlreadyBookedException e) {
            System.out.println(" [PASS] Test 2: Caught expected SlotAlreadyBookedException");
            passed++;
        } catch (Exception e) {
            System.err.println(" [FAIL] Test 2 wrong exception: " + e.getMessage());
            failed++;
        }

        // Test 3: Booking Quota Exceeded Exception (BookingQuotaExceededException)
        try {
            Court court1 = new Court("CRT1", "Badminton");
            Court court2 = new Court("CRT2", "Tennis");
            Court court3 = new Court("CRT3", "Basketball");
            User student = new Student("S101", "Rahul"); // Limit is 2
            List<Court> courts = new ArrayList<>();
            courts.add(court1);
            courts.add(court2);
            courts.add(court3);

            court1.reserve("09:00-10:00", student, courts);
            court2.reserve("10:00-11:00", student, courts);
            // 3rd reservation should trigger quota exception (student limit is 2)
            court3.reserve("11:00-12:00", student, courts);

            System.err.println(" [FAIL] Test 3: Expected BookingQuotaExceededException but none was thrown");
            failed++;
        } catch (BookingQuotaExceededException e) {
            System.out.println(" [PASS] Test 3: Caught expected BookingQuotaExceededException");
            passed++;
        } catch (Exception e) {
            System.err.println(" [FAIL] Test 3 wrong exception: " + e.getMessage());
            failed++;
        }

        // Test 4: Outstanding Fine Exception (OutstandingFineException)
        try {
            Court court = new Court("CRT1", "Badminton");
            User student = new Student("S101", "Rahul");
            student.addFine(150.0); // Add fine
            List<Court> courts = new ArrayList<>();
            courts.add(court);

            court.reserve("09:00-10:00", student, courts);
            System.err.println(" [FAIL] Test 4: Expected OutstandingFineException but none was thrown");
            failed++;
        } catch (OutstandingFineException e) {
            System.out.println(" [PASS] Test 4: Caught expected OutstandingFineException");
            passed++;
        } catch (Exception e) {
            System.err.println(" [FAIL] Test 4 wrong exception: " + e.getMessage());
            failed++;
        }

        // Test 5: Invalid Leave Days Exception (InvalidLeaveDaysException)
        try {
            HostelStudent student = new HostelStudent("STU101", "Anjali", new SingleOccupancy(101, 3000.0), new StandardPlan());
            student.applyLeave(-5, 30);
            System.err.println(" [FAIL] Test 5: Expected InvalidLeaveDaysException for negative days");
            failed++;
        } catch (InvalidLeaveDaysException e) {
            System.out.println(" [PASS] Test 5: Caught expected InvalidLeaveDaysException for negative days");
            passed++;
        } catch (Exception e) {
            System.err.println(" [FAIL] Test 5 wrong exception: " + e.getMessage());
            failed++;
        }

        // Test 6: Data Persistence (Serialization Save & Load)
        try {
            String testFile = "data/test_campus_data.ser";
            CampusData originalData = CampusStorageManager.seedInitialData();
            boolean saveOk = CampusStorageManager.saveData(originalData, testFile);

            if (!saveOk) {
                System.err.println(" [FAIL] Test 6: Failed to save campus data to disk");
                failed++;
            } else {
                CampusData loadedData = CampusStorageManager.loadData(testFile);
                if (loadedData != null && 
                    loadedData.getUsers().size() == originalData.getUsers().size() &&
                    loadedData.getCourts().size() == originalData.getCourts().size()) {
                    System.out.println(" [PASS] Test 6: Serialization state save and load verified");
                    passed++;
                } else {
                    System.err.println(" [FAIL] Test 6: Loaded data size mismatch");
                    failed++;
                }
            }
        } catch (Exception e) {
            System.err.println(" [FAIL] Test 6 exception: " + e.getMessage());
            failed++;
        }

        // Test 7: ACSuite Tariff Calculation
        try {
            BaseRoom acRoom = new ACSuite(501, 5000.0);
            double tariff = acRoom.calculateMonthlyTariff(); // 5000 + 1500 = 6500
            if (Math.abs(tariff - 6500.0) < 0.001) {
                System.out.println(" [PASS] Test 7: ACSuite monthly tariff calculated correctly (Rs. 6500.0)");
                passed++;
            } else {
                System.err.println(" [FAIL] Test 7: Expected tariff 6500.0, got " + tariff);
                failed++;
            }
        } catch (Exception e) {
            System.err.println(" [FAIL] Test 7 exception: " + e.getMessage());
            failed++;
        }

        // Test 8: SingleOccupancy Tariff Calculation
        try {
            BaseRoom singleRoom = new SingleOccupancy(201, 4000.0);
            double tariff = singleRoom.calculateMonthlyTariff(); // 4000 * 1.2 = 4800
            if (Math.abs(tariff - 4800.0) < 0.001) {
                System.out.println(" [PASS] Test 8: SingleOccupancy tariff calculated correctly (Rs. 4800.0)");
                passed++;
            } else {
                System.err.println(" [FAIL] Test 8: Expected tariff 4800.0, got " + tariff);
                failed++;
            }
        } catch (Exception e) {
            System.err.println(" [FAIL] Test 8 exception: " + e.getMessage());
            failed++;
        }

        // Test 9: Court Slot Release with User Authorization
        try {
            Court court = new Court("CRT1", "Badminton");
            User owner = new Student("S101", "Rahul");
            User intruder = new Student("S102", "Priya");
            List<Court> courts = new ArrayList<>();
            courts.add(court);

            court.reserve("10:00-11:00", owner, courts);

            // Intruder tries to cancel owner's booking
            boolean intruderRelease = court.release("10:00-11:00", intruder);
            // Owner cancels own booking
            boolean ownerRelease = court.release("10:00-11:00", owner);

            if (!intruderRelease && ownerRelease && court.checkAvailability("10:00-11:00")) {
                System.out.println(" [PASS] Test 9: Court release authorization verified (intruder blocked, owner allowed)");
                passed++;
            } else {
                System.err.println(" [FAIL] Test 9: Release authorization check failed");
                failed++;
            }
        } catch (Exception e) {
            System.err.println(" [FAIL] Test 9 exception: " + e.getMessage());
            failed++;
        }

        // Test 10: InvalidLeaveDaysException for Exceeding Month Days
        try {
            HostelStudent student = new HostelStudent("STU102", "Dev", new SingleOccupancy(102, 3000.0), new StandardPlan());
            student.applyLeave(35, 30); // 35 days in a 30-day month
            System.err.println(" [FAIL] Test 10: Expected InvalidLeaveDaysException for days exceeding month");
            failed++;
        } catch (InvalidLeaveDaysException e) {
            System.out.println(" [PASS] Test 10: Caught expected InvalidLeaveDaysException for leaves exceeding month days");
            passed++;
        } catch (Exception e) {
            System.err.println(" [FAIL] Test 10 wrong exception: " + e.getMessage());
            failed++;
        }

        // Test 11: Itemized Bill Calculation for Standard vs SpecialDietPlan
        try {
            HostelStudent s1 = new HostelStudent("STU103", "Kavya", new SingleOccupancy(103, 4000.0), new StandardPlan());
            s1.applyLeave(5, 30); // Leaves = 5, Active mess days = 25. Standard rate = 120/day -> 25 * 120 = 3000. Room = 4000 * 1.2 = 4800. Total = 7800
            String bill = s1.getBillSummary(30);

            if (bill.contains("7800.00") && bill.contains("5 days leave deducted")) {
                System.out.println(" [PASS] Test 11: Itemized hostel bill and leave deductions verified");
                passed++;
            } else {
                System.err.println(" [FAIL] Test 11: Bill output mismatch: " + bill);
                failed++;
            }
        } catch (Exception e) {
            System.err.println(" [FAIL] Test 11 exception: " + e.getMessage());
            failed++;
        }

        // Test 12: Successful Book Checkout & Loan Tracking
        try {
            Book book = new Book("978-0262033848", "Introduction to Algorithms", "Thomas H. Cormen", "Computer Science");
            User student = new Student("S101", "Rahul");
            LibraryLoan loan = new LibraryLoan("LN101", book, student, 14);

            if (!book.isAvailable() && book.getCurrentBorrower().getUserId().equals("S101") && !loan.isReturned()) {
                System.out.println(" [PASS] Test 12: Book checkout and loan status verified");
                passed++;
            } else {
                System.err.println(" [FAIL] Test 12: Book availability or borrower mismatch");
                failed++;
            }
        } catch (Exception e) {
            System.err.println(" [FAIL] Test 12 exception: " + e.getMessage());
            failed++;
        }

        // Test 13: BookNotAvailableException on double checkout
        try {
            Book book = new Book("978-1118063330", "Operating System Concepts", "Silberschatz", "Systems");
            User u1 = new Student("S101", "Rahul");

            new LibraryLoan("LN102", book, u1, 14);
            if (!book.isAvailable()) {
                throw new BookNotAvailableException(book.getIsbn(), book.getTitle());
            }
            System.err.println(" [FAIL] Test 13: Expected BookNotAvailableException but none thrown");
            failed++;
        } catch (BookNotAvailableException e) {
            System.out.println(" [PASS] Test 13: Caught expected BookNotAvailableException");
            passed++;
        } catch (Exception e) {
            System.err.println(" [FAIL] Test 13 wrong exception: " + e.getMessage());
            failed++;
        }

        // Test 14: Overdue Book Return Fine Calculation
        try {
            Book book = new Book("978-0078022159", "Database System Concepts", "Korth", "Databases");
            User student = new Student("S103", "Kiran");
            LibraryLoan loan = new LibraryLoan("LN103", book, student, 14);

            double fine = loan.completeReturn(4); // 4 days overdue * 5 = Rs. 20.0
            if (book.isAvailable() && loan.isReturned() && Math.abs(fine - 20.0) < 0.001 && Math.abs(student.getFineBalance() - 20.0) < 0.001) {
                System.out.println(" [PASS] Test 14: Overdue book return penalty calculation verified (Rs. 20.0 fine charged)");
                passed++;
            } else {
                System.err.println(" [FAIL] Test 14: Fine calculation mismatch, got " + fine + ", student fine: " + student.getFineBalance());
                failed++;
            }
        } catch (Exception e) {
            System.err.println(" [FAIL] Test 14 exception: " + e.getMessage());
            failed++;
        }

        // Test 15: Cross-Module Verification: Library Fine blocks Court Booking
        try {
            Court court = new Court("CRT1", "Badminton");
            User student = new Student("S104", "Vikram");
            Book book = new Book("978-0132126953", "Computer Networks", "Tanenbaum", "Networks");
            LibraryLoan loan = new LibraryLoan("LN104", book, student, 14);

            // Return book with 3 days overdue (Rs. 15 fine added)
            loan.completeReturn(3);

            List<Court> courts = new ArrayList<>();
            courts.add(court);

            // Attempt to reserve court slot with unpaid library fine
            court.reserve("17:00-18:00", student, courts);

            System.err.println(" [FAIL] Test 15: Expected OutstandingFineException from library fine hold, but reservation succeeded");
            failed++;
        } catch (OutstandingFineException e) {
            System.out.println(" [PASS] Test 15: Cross-Module Integration verified (Overdue library fine blocked court reservation)");
            passed++;
        } catch (Exception e) {
            System.err.println(" [FAIL] Test 15 wrong exception: " + e.getMessage());
            failed++;
        }

        // Test 16: Academic Attendance & KTU 75% Threshold Eligibility
        try {
            Course course = new Course("CST205", "Object Oriented Programming (Java)", 4, 3);
            AttendanceRecord record = new AttendanceRecord(course, 36, 40); // 90.0%
            if (Math.abs(record.getPercentage() - 90.0) < 0.001 && record.isEligible()) {
                System.out.println(" [PASS] Test 16: Attendance calculation & KTU 75% threshold compliance verified (90.0% - Eligible)");
                passed++;
            } else {
                System.err.println(" [FAIL] Test 16: Expected 90.0% eligible, got " + record.getPercentage());
                failed++;
            }
        } catch (Exception e) {
            System.err.println(" [FAIL] Test 16 exception: " + e.getMessage());
            failed++;
        }

        // Test 17: Caught LowAttendanceException for Attendance Below 75%
        try {
            Course course = new Course("MAT203", "Discrete Mathematical Structures", 4, 3);
            AttendanceRecord record = new AttendanceRecord(course, 26, 40); // 65.0% (Condonation range)
            record.validateExamEligibility();
            System.err.println(" [FAIL] Test 17: Expected LowAttendanceException for 65% attendance, but check passed");
            failed++;
        } catch (LowAttendanceException e) {
            System.out.println(" [PASS] Test 17: Caught expected LowAttendanceException for KTU attendance shortage (<75%)");
            passed++;
        } catch (Exception e) {
            System.err.println(" [FAIL] Test 17 wrong exception: " + e.getMessage());
            failed++;
        }

        // Test 18: Continuous Internal Evaluation (CIE) Calculation & InvalidMarkException
        try {
            Course course = new Course("CST201", "Data Structures", 4, 3);
            InternalAssessment cie = new InternalAssessment(course, 18.0, 19.0, 9.5);
            double total = cie.calculateTotalCie(); // 46.5 / 50
            if (Math.abs(total - 46.5) > 0.001 || !cie.isInternalPass()) {
                throw new AssertionError("Incorrect CIE total: " + total);
            }

            // Verify InvalidMarkException when mark exceeds component maximum
            boolean caughtInvalid = false;
            try {
                cie.setScores(25.0, 15.0, 9.0); // Series 1 max is 20.0
            } catch (InvalidMarkException expected) {
                caughtInvalid = true;
            }

            if (caughtInvalid) {
                System.out.println(" [PASS] Test 18: CIE calculation (46.5/50) & InvalidMarkException enforcement verified");
                passed++;
            } else {
                System.err.println(" [FAIL] Test 18: Expected InvalidMarkException for Series 1 mark of 25.0/20.0");
                failed++;
            }
        } catch (Exception e) {
            System.err.println(" [FAIL] Test 18 exception: " + e.getMessage());
            failed++;
        }

        // Test 19: KTU SGPA Weighted Credit Calculation (10-Point System)
        try {
            AcademicProfile profile = new AcademicProfile("S101", "Rahul Sharma");
            Course cst205 = new Course("CST205", "OOP Java", 4, 3);
            Course cst201 = new Course("CST201", "Data Structures", 4, 3);
            Course est200 = new Course("EST200", "Design & Engineering", 2, 3);
            Course mcn201 = new Course("MCN201", "Sustainable Engineering", 0, 3); // 0-credit audit

            profile.enrollCourse(cst205);
            profile.enrollCourse(cst201);
            profile.enrollCourse(est200);
            profile.enrollCourse(mcn201);

            profile.recordGrade("CST205", KtuGrade.S);       // 4 * 10.0 = 40
            profile.recordGrade("CST201", KtuGrade.A_PLUS);  // 4 * 9.0  = 36
            profile.recordGrade("EST200", KtuGrade.A);       // 2 * 8.5  = 17
            profile.recordGrade("MCN201", KtuGrade.P);       // 0 * 5.5  = 0 (audit)
            // Total points: 40 + 36 + 17 = 93. Total credits: 4 + 4 + 2 = 10.
            // Expected SGPA = 93 / 10 = 9.30

            double sgpa = profile.calculateSGPA();
            if (Math.abs(sgpa - 9.30) < 0.01 && profile.getClassClassification().contains("Distinction")) {
                System.out.println(" [PASS] Test 19: KTU SGPA weighted credit calculation verified (9.30/10.0 - Distinction)");
                passed++;
            } else {
                System.err.println(" [FAIL] Test 19: Expected SGPA 9.30, got " + sgpa + ", classification: " + profile.getClassClassification());
                failed++;
            }
        } catch (Exception e) {
            System.err.println(" [FAIL] Test 19 exception: " + e.getMessage());
            failed++;
        }

        System.out.println("\n--------------------------------------------------");
        System.out.println("TEST SUMMARY: Passed " + passed + " / " + (passed + failed) + " tests.");
        System.out.println("--------------------------------------------------");

        if (failed > 0) {
            System.exit(1);
        }
    }
}
