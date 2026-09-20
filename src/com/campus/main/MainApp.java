package com.campus.main;

import com.campus.exceptions.*;
import com.campus.hostel.*;
import com.campus.sports.*;
import com.campus.storage.*;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class MainApp {
    private static final String STORAGE_PATH = "data/campus_data.ser";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("==========================================================");
        System.out.println("=== Campus Management System (KTU Mini Project Framework) ===");
        System.out.println("==========================================================");

        CampusData data = CampusStorageManager.loadData(STORAGE_PATH);
        List<User> users = data.getUsers();
        List<Court> courts = data.getCourts();
        List<HostelStudent> hostelStudents = data.getHostelStudents();

        System.out.println("[System loaded state: " + users.size() + " users, " + 
                           courts.size() + " courts, " + hostelStudents.size() + " hostel students]");

        boolean running = true;
        while (running) {
            System.out.println("\n---------------- MENU OPTIONS ----------------");
            System.out.println("--- SPORTS FACILITY MODULE ---");
            System.out.println(" 1. Reserve Court Slot (Quota & Fine Enforced)");
            System.out.println(" 2. Cancel Court Booking");
            System.out.println(" 3. View Courts & Active Reservations");
            System.out.println(" 4. Manage User Fines (Add / Pay Fine)");
            System.out.println("--- HOSTEL & MESS MODULE ---");
            System.out.println(" 5. Register Hostel Student");
            System.out.println(" 6. Apply Leave Days (Validation Enforced)");
            System.out.println(" 7. Generate Monthly Itemized Bill");
            System.out.println(" 8. View All Hostel Students");
            System.out.println("--- SYSTEM ---");
            System.out.println(" 9. Save & Exit");
            System.out.print("Choose an option [1-9]: ");

            if (!scanner.hasNextInt()) {
                scanner.next();
                System.out.println("Invalid input. Please enter a number between 1 and 9.");
                continue;
            }

            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1: { // Reserve Court Slot
                    System.out.println("\n--- Select User ---");
                    for (int i = 0; i < users.size(); i++) {
                        User u = users.get(i);
                        System.out.println((i + 1) + ". " + u.getName() + " [" + u.getClass().getSimpleName() + 
                                           ", Max Limit: " + u.getBookingLimit() + 
                                           ", Fine: Rs. " + u.getFineBalance() + "]");
                    }
                    System.out.print("Select User (number): ");
                    int uIdx = scanner.nextInt() - 1;
                    if (uIdx < 0 || uIdx >= users.size()) {
                        System.out.println("Invalid user selection.");
                        break;
                    }
                    User selectedUser = users.get(uIdx);

                    System.out.println("\n--- Select Court ---");
                    for (int i = 0; i < courts.size(); i++) {
                        System.out.println((i + 1) + ". " + courts.get(i));
                    }
                    System.out.print("Select Court (number): ");
                    int cIdx = scanner.nextInt() - 1;
                    if (cIdx < 0 || cIdx >= courts.size()) {
                        System.out.println("Invalid court selection.");
                        break;
                    }
                    Court selectedCourt = courts.get(cIdx);

                    System.out.print("Enter Time Slot (e.g. 09:00-10:00): ");
                    String slot = scanner.next();

                    try {
                        selectedCourt.reserve(slot, selectedUser, courts);
                        System.out.println(" SUCCESS: Slot '" + slot + "' reserved on " + selectedCourt + 
                                           " for " + selectedUser.getName() + "!");
                    } catch (SlotAlreadyBookedException | OutstandingFineException | BookingQuotaExceededException e) {
                        System.err.println(" RESERVATION ERROR: " + e.getMessage());
                    }
                    break;
                }

                case 2: { // Cancel Booking
                    System.out.println("\n--- Select Court ---");
                    for (int i = 0; i < courts.size(); i++) {
                        System.out.println((i + 1) + ". " + courts.get(i));
                    }
                    System.out.print("Select Court (number): ");
                    int cIdx = scanner.nextInt() - 1;
                    if (cIdx < 0 || cIdx >= courts.size()) {
                        System.out.println("Invalid court selection.");
                        break;
                    }
                    Court selectedCourt = courts.get(cIdx);

                    System.out.print("Enter Slot to Cancel (e.g. 09:00-10:00): ");
                    String slot = scanner.next();

                    if (selectedCourt.checkAvailability(slot)) {
                        System.out.println("Slot '" + slot + "' is not currently booked.");
                    } else {
                        System.out.println("\n--- Cancellation Authorization ---");
                        System.out.println("0. Admin Override");
                        for (int i = 0; i < users.size(); i++) {
                            System.out.println((i + 1) + ". " + users.get(i).getName() + " (" + users.get(i).getUserId() + ")");
                        }
                        System.out.print("Select User authorizing cancellation [0-" + users.size() + "]: ");
                        int authIdx = scanner.nextInt();
                        if (authIdx == 0) {
                            selectedCourt.release(slot);
                            System.out.println(" SUCCESS: Slot '" + slot + "' released on " + selectedCourt + " (Admin override).");
                        } else if (authIdx >= 1 && authIdx <= users.size()) {
                            User authUser = users.get(authIdx - 1);
                            boolean released = selectedCourt.release(slot, authUser);
                            if (released) {
                                System.out.println(" SUCCESS: Slot '" + slot + "' released on " + selectedCourt + " by " + authUser.getName() + ".");
                            } else {
                                System.out.println(" ERROR: Slot '" + slot + "' was not booked by " + authUser.getName() + ". Cancellation denied.");
                            }
                        } else {
                            System.out.println("Invalid user selection.");
                        }
                    }
                    break;
                }

                case 3: { // View Courts & Reservations
                    System.out.println("\n--- COURT STATUS & RESERVATIONS ---");
                    for (Court court : courts) {
                        System.out.println(court + ":");
                        Map<String, User> resMap = court.getSlotReservations();
                        if (resMap.isEmpty()) {
                            System.out.println("  No current bookings.");
                        } else {
                            for (Map.Entry<String, User> entry : resMap.entrySet()) {
                                User u = entry.getValue();
                                String userDesc = (u != null) ? u.getName() + " (" + u.getUserId() + ")" : "Anonymous";
                                System.out.println("  Slot [" + entry.getKey() + "] -> Reserved by " + userDesc);
                            }
                        }
                    }
                    break;
                }

                case 4: { // Manage Fines
                    System.out.println("\n--- USER FINE MANAGEMENT ---");
                    for (int i = 0; i < users.size(); i++) {
                        User u = users.get(i);
                        System.out.println((i + 1) + ". " + u.getName() + " (" + u.getUserId() + ") - Outstanding Fine: Rs. " + u.getFineBalance());
                    }
                    System.out.print("Select User (number): ");
                    int uIdx = scanner.nextInt() - 1;
                    if (uIdx < 0 || uIdx >= users.size()) {
                        System.out.println("Invalid selection.");
                        break;
                    }
                    User targetUser = users.get(uIdx);

                    System.out.println("1. Add Fine");
                    System.out.println("2. Pay Fine");
                    System.out.print("Choose action [1-2]: ");
                    int subChoice = scanner.nextInt();
                    System.out.print("Enter Amount (Rs.): ");
                    double amount = scanner.nextDouble();

                    if (subChoice == 1) {
                        targetUser.addFine(amount);
                        System.out.println("Fine of Rs. " + amount + " added to " + targetUser.getName() + ". Total fine: Rs. " + targetUser.getFineBalance());
                    } else if (subChoice == 2) {
                        targetUser.payFine(amount);
                        System.out.println("Payment of Rs. " + amount + " recorded. Remaining fine balance: Rs. " + targetUser.getFineBalance());
                    } else {
                        System.out.println("Invalid sub-option.");
                    }
                    break;
                }

                case 5: { // Register Hostel Student
                    System.out.print("\nEnter Roll No: ");
                    String roll = scanner.next();
                    scanner.nextLine();
                    System.out.print("Enter Student Name: ");
                    String name = scanner.nextLine();

                    System.out.println("Select Room Type: 1. Single Occupancy  2. AC Suite");
                    int rmChoice = scanner.nextInt();
                    System.out.print("Enter Room Number: ");
                    int rmNum = scanner.nextInt();
                    System.out.print("Enter Monthly Base Rate (e.g. 4500): Rs. ");
                    double baseRate = scanner.nextDouble();

                    BaseRoom room = (rmChoice == 2) ? new ACSuite(rmNum, baseRate) : new SingleOccupancy(rmNum, baseRate);

                    System.out.println("Select Mess Plan: 1. Standard Plan  2. Special Diet Plan");
                    int mpChoice = scanner.nextInt();
                    MealPlan plan = (mpChoice == 2) ? new SpecialDietPlan() : new StandardPlan();

                    HostelStudent newStudent = new HostelStudent(roll, name, room, plan);
                    hostelStudents.add(newStudent);
                    System.out.println(" SUCCESS: Registered hostel student " + name + " (" + roll + ") with room " + rmNum + " [" + room.getClass().getSimpleName() + "]!");
                    break;
                }

                case 6: { // Apply Leave
                    if (hostelStudents.isEmpty()) {
                        System.out.println("No hostel students registered.");
                        break;
                    }
                    System.out.println("\n--- Select Hostel Student ---");
                    for (int i = 0; i < hostelStudents.size(); i++) {
                        HostelStudent hs = hostelStudents.get(i);
                        System.out.println((i + 1) + ". " + hs.getName() + " (" + hs.getRollNo() + ") - Current Leaves: " + hs.getLeavesThisMonth() + " days");
                    }
                    System.out.print("Select Student (number): ");
                    int sIdx = scanner.nextInt() - 1;
                    if (sIdx < 0 || sIdx >= hostelStudents.size()) {
                        System.out.println("Invalid selection.");
                        break;
                    }
                    HostelStudent selectedStudent = hostelStudents.get(sIdx);

                    System.out.print("Enter Additional Leave Days: ");
                    int leaveDays = scanner.nextInt();

                    try {
                        selectedStudent.applyLeave(leaveDays, 30);
                        System.out.println(" SUCCESS: Applied " + leaveDays + " days leave. Total monthly leaves: " + selectedStudent.getLeavesThisMonth() + " days.");
                    } catch (InvalidLeaveDaysException e) {
                        System.err.println(" INVALID LEAVE ERROR: " + e.getMessage());
                    }
                    break;
                }

                case 7: { // Generate Monthly Bill
                    if (hostelStudents.isEmpty()) {
                        System.out.println("No hostel students registered.");
                        break;
                    }
                    System.out.println("\n--- Select Hostel Student for Billing ---");
                    for (int i = 0; i < hostelStudents.size(); i++) {
                        HostelStudent hs = hostelStudents.get(i);
                        System.out.println((i + 1) + ". " + hs.getName() + " (" + hs.getRollNo() + ")");
                    }
                    System.out.print("Select Student (number): ");
                    int sIdx = scanner.nextInt() - 1;
                    if (sIdx < 0 || sIdx >= hostelStudents.size()) {
                        System.out.println("Invalid selection.");
                        break;
                    }
                    HostelStudent selectedStudent = hostelStudents.get(sIdx);
                    selectedStudent.generateBill(30);
                    break;
                }

                case 8: { // View All Hostel Students
                    System.out.println("\n--- REGISTERED HOSTEL STUDENTS ---");
                    if (hostelStudents.isEmpty()) {
                        System.out.println("No hostel students found.");
                    } else {
                        for (HostelStudent hs : hostelStudents) {
                            System.out.println("- " + hs.getName() + " (" + hs.getRollNo() + ") | Room: " + 
                                               hs.getAssignedRoom().getRoomNumber() + " [" + hs.getAssignedRoom().getClass().getSimpleName() + "] | Plan: " +
                                               hs.getChosenMealPlan().getClass().getSimpleName() + " | Leaves: " + hs.getLeavesThisMonth() + " days");
                        }
                    }
                    break;
                }

                case 9: { // Save & Exit
                    System.out.println("\nSaving system state to " + STORAGE_PATH + "...");
                    boolean saved = CampusStorageManager.saveData(data, STORAGE_PATH);
                    if (saved) {
                        System.out.println("State successfully persisted via Java Serialization!");
                    } else {
                        System.err.println("Warning: Failed to save state.");
                    }
                    System.out.println("Exiting Campus Academic Management System. Goodbye!");
                    running = false;
                    break;
                }

                default:
                    System.out.println("Invalid option. Please choose between 1 and 9.");
                    break;
            }
        }
        scanner.close();
    }
}
