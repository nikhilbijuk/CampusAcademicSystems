package com.campus.test;

import com.campus.exceptions.*;
import com.campus.hostel.*;
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

        System.out.println("\n--------------------------------------------------");
        System.out.println("TEST SUMMARY: Passed " + passed + " / " + (passed + failed) + " tests.");
        System.out.println("--------------------------------------------------");

        if (failed > 0) {
            System.exit(1);
        }
    }
}
