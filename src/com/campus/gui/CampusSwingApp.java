package com.campus.gui;

import com.campus.academic.*;
import com.campus.db.CampusDao;
import com.campus.exceptions.*;
import com.campus.hostel.*;
import com.campus.library.Book;
import com.campus.sports.Court;
import com.campus.sports.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Desktop Graphical User Interface (GUI) built with Java Swing (Module 4 / CO5 Requirement).
 * Uses MVC-style separation backed by CampusDao and Singleton DatabaseManager.
 */
public class CampusSwingApp extends JFrame {

    private final CampusDao dao;

    // UI Components
    private JComboBox<String> userCombo;
    private JLabel statusLabel;

    // Tables
    private DefaultTableModel sportsTableModel;
    private DefaultTableModel hostelTableModel;
    private DefaultTableModel libraryTableModel;
    private DefaultTableModel attendanceTableModel;
    private DefaultTableModel internalsTableModel;
    private JLabel sgpaLabel;
    private JLabel classificationLabel;

    public CampusSwingApp() {
        super("Campus Academic Management System | KTU PBL Course Project");
        this.dao = new CampusDao();

        initUI();
        loadAllData();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 720);
        setLocationRelativeTo(null);

        // Main Layout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(24, 28, 36));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel titleLabel = new JLabel("🎓 Campus Academic Systems (Java Swing & SQLite JDBC)");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);

        statusLabel = new JLabel("🟢 Connected to SQLite via Singleton DatabaseManager");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(110, 231, 183));

        JPanel userSelectPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userSelectPanel.setOpaque(false);
        JLabel userLbl = new JLabel("Acting User: ");
        userLbl.setForeground(Color.WHITE);
        userCombo = new JComboBox<>();
        userCombo.addActionListener(e -> loadAcademicData());
        userSelectPanel.add(userLbl);
        userSelectPanel.add(userCombo);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(statusLabel, BorderLayout.SOUTH);
        headerPanel.add(userSelectPanel, BorderLayout.EAST);

        // Tabbed Pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 13));

        tabbedPane.addTab("🏟️ Sports Facilities", createSportsPanel());
        tabbedPane.addTab("🏨 Hostel & Mess Billing", createHostelPanel());
        tabbedPane.addTab("📚 Library Catalog", createLibraryPanel());
        tabbedPane.addTab("📊 KTU Academics & Attendance", createAcademicPanel());

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    // ==========================================
    // Sports Panel
    // ==========================================
    private JPanel createSportsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Controls
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JComboBox<String> courtCombo = new JComboBox<>(new String[]{"CRT1 (Badminton)", "CRT2 (Tennis)", "CRT3 (Basketball)"});
        JComboBox<String> slotCombo = new JComboBox<>(new String[]{
            "06:00-07:00", "07:00-08:00", "08:00-09:00", "16:00-17:00", "17:00-18:00", "18:00-19:00", "19:00-20:00"
        });

        JButton reserveBtn = new JButton("Reserve Slot");
        JButton cancelBtn = new JButton("Cancel Slot");
        JButton refreshBtn = new JButton("Refresh");

        controls.add(new JLabel("Court:"));
        controls.add(courtCombo);
        controls.add(new JLabel("Time Slot:"));
        controls.add(slotCombo);
        controls.add(reserveBtn);
        controls.add(cancelBtn);
        controls.add(refreshBtn);

        // Table
        sportsTableModel = new DefaultTableModel(new String[]{"Court ID", "Type", "Time Slot", "Reserved By (User ID)"}, 0);
        JTable table = new JTable(sportsTableModel);
        table.setRowHeight(24);

        reserveBtn.addActionListener(e -> {
            String courtStr = (String) courtCombo.getSelectedItem();
            String courtId = courtStr.substring(0, 4);
            String slot = (String) slotCombo.getSelectedItem();
            String userStr = (String) userCombo.getSelectedItem();
            if (userStr == null) return;
            String userId = userStr.split(" - ")[0];

            try {
                dao.reserveCourt(courtId, slot, userId);
                JOptionPane.showMessageDialog(this, "Success! Slot " + slot + " reserved for " + userId, "Reservation Confirmed", JOptionPane.INFORMATION_MESSAGE);
                loadAllData();
            } catch (SlotAlreadyBookedException | OutstandingFineException | BookingQuotaExceededException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Reservation Blocked", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> {
            String courtStr = (String) courtCombo.getSelectedItem();
            String courtId = courtStr.substring(0, 4);
            String slot = (String) slotCombo.getSelectedItem();
            String userStr = (String) userCombo.getSelectedItem();
            if (userStr == null) return;
            String userId = userStr.split(" - ")[0];

            try {
                boolean released = dao.releaseCourt(courtId, slot, userId);
                if (released) {
                    JOptionPane.showMessageDialog(this, "Slot " + slot + " released successfully.", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
                    loadAllData();
                } else {
                    JOptionPane.showMessageDialog(this, "Cannot cancel: You are not authorized or slot is not booked.", "Cancellation Denied", JOptionPane.WARNING_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        refreshBtn.addActionListener(e -> loadAllData());

        panel.add(controls, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // ==========================================
    // Hostel Panel
    // ==========================================
    private JPanel createHostelPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Form & Actions
        JPanel topBox = new JPanel(new GridLayout(2, 1, 5, 5));

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JTextField rollField = new JTextField(7);
        JTextField nameField = new JTextField(10);
        JComboBox<String> roomCombo = new JComboBox<>(new String[]{"SingleOccupancy (Rs.5400)", "ACSuite (Rs.7500)"});
        JComboBox<String> mealCombo = new JComboBox<>(new String[]{"StandardPlan (Rs.3600)", "SpecialDietPlan (Rs.4500)"});
        JButton registerBtn = new JButton("Register Student");

        form.add(new JLabel("Roll No:"));
        form.add(rollField);
        form.add(new JLabel("Name:"));
        form.add(nameField);
        form.add(new JLabel("Room:"));
        form.add(roomCombo);
        form.add(new JLabel("Meal:"));
        form.add(mealCombo);
        form.add(registerBtn);

        JPanel leaveForm = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JTextField leaveDaysField = new JTextField("3", 4);
        JButton applyLeaveBtn = new JButton("Apply Leave Days");
        JButton billBtn = new JButton("Generate Monthly Bill");

        leaveForm.add(new JLabel("Leave Days:"));
        leaveForm.add(leaveDaysField);
        leaveForm.add(applyLeaveBtn);
        leaveForm.add(billBtn);

        topBox.add(form);
        topBox.add(leaveForm);

        // Table
        hostelTableModel = new DefaultTableModel(new String[]{"Roll No", "Student Name", "Room No", "Room Type", "Monthly Tariff", "Meal Plan", "Leaves This Month"}, 0);
        JTable table = new JTable(hostelTableModel);
        table.setRowHeight(24);

        registerBtn.addActionListener(e -> {
            String roll = rollField.getText().trim();
            String name = nameField.getText().trim();
            if (roll.isEmpty() || name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter both Roll No and Name.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int roomIndex = roomCombo.getSelectedIndex();
            int rNum = 100 + (int)(Math.random() * 400);
            BaseRoom room = (roomIndex == 1) ? new ACSuite(rNum, 6000.0) : new SingleOccupancy(rNum, 4500.0);
            MealPlan meal = (mealCombo.getSelectedIndex() == 1) ? new SpecialDietPlan() : new StandardPlan();

            try {
                dao.registerHostelStudent(roll, name, room, meal);
                JOptionPane.showMessageDialog(this, "Student " + name + " registered successfully in room " + rNum, "Registered", JOptionPane.INFORMATION_MESSAGE);
                rollField.setText("");
                nameField.setText("");
                loadAllData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        applyLeaveBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(this, "Please select a student from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String roll = (String) hostelTableModel.getValueAt(selectedRow, 0);
            try {
                int days = Integer.parseInt(leaveDaysField.getText().trim());
                dao.applyHostelLeave(roll, days);
                JOptionPane.showMessageDialog(this, days + " leave days approved and applied for " + roll, "Leave Approved", JOptionPane.INFORMATION_MESSAGE);
                loadAllData();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Leave days must be an integer.", "Validation", JOptionPane.WARNING_MESSAGE);
            } catch (InvalidLeaveDaysException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid Leave Days", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        billBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(this, "Please select a student from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String roll = (String) hostelTableModel.getValueAt(selectedRow, 0);
            try {
                List<HostelStudent> list = dao.getAllHostelStudents();
                for (HostelStudent s : list) {
                    if (s.getRollNo().equalsIgnoreCase(roll)) {
                        String receipt = s.getBillSummary(30);
                        JOptionPane.showMessageDialog(this, receipt, "Monthly Bill Summary", JOptionPane.INFORMATION_MESSAGE);
                        break;
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(topBox, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // ==========================================
    // Library Panel
    // ==========================================
    private JPanel createLibraryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JButton borrowBtn = new JButton("Borrow Selected Book");
        JButton returnBtn = new JButton("Return Book");
        JButton clearFineBtn = new JButton("Clear User Fine (Pay)");

        controls.add(borrowBtn);
        controls.add(returnBtn);
        controls.add(clearFineBtn);

        libraryTableModel = new DefaultTableModel(new String[]{"ISBN", "Title", "Author", "Category", "Status", "Borrower"}, 0);
        JTable table = new JTable(libraryTableModel);
        table.setRowHeight(24);

        borrowBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(this, "Please select a book from the table.", "Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String isbn = (String) libraryTableModel.getValueAt(selectedRow, 0);
            String userStr = (String) userCombo.getSelectedItem();
            if (userStr == null) return;
            String userId = userStr.split(" - ")[0];

            try {
                dao.borrowBook(isbn, userId);
                JOptionPane.showMessageDialog(this, "Book successfully borrowed by " + userId, "Book Loaned", JOptionPane.INFORMATION_MESSAGE);
                loadAllData();
            } catch (BookNotAvailableException | OutstandingFineException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Borrowing Blocked", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        returnBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(this, "Please select a book from the table.", "Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String isbn = (String) libraryTableModel.getValueAt(selectedRow, 0);
            String daysStr = JOptionPane.showInputDialog(this, "Enter overdue days (0 if returned on time):", "0");
            if (daysStr == null) return;
            try {
                int overdue = Integer.parseInt(daysStr.trim());
                double penalty = dao.returnBook(isbn, overdue);
                if (penalty > 0) {
                    JOptionPane.showMessageDialog(this, "Book returned with overdue penalty: Rs. " + penalty + " charged to borrower.", "Overdue Fine Charged", JOptionPane.WARNING_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Book returned on time. No fines applied.", "Returned", JOptionPane.INFORMATION_MESSAGE);
                }
                loadAllData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        clearFineBtn.addActionListener(e -> {
            String userStr = (String) userCombo.getSelectedItem();
            if (userStr == null) return;
            String userId = userStr.split(" - ")[0];
            try {
                dao.updateUserFine(userId, 0.0);
                JOptionPane.showMessageDialog(this, "All outstanding fines cleared for user " + userId, "Fine Paid", JOptionPane.INFORMATION_MESSAGE);
                loadAllData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(controls, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // ==========================================
    // KTU Academic & Attendance Panel
    // ==========================================
    private JPanel createAcademicPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Action Toolbar
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        JButton updateAttBtn = new JButton("📝 Update Attendance");
        JButton updateCieBtn = new JButton("📊 Enter / Update CIE");
        JButton updateGradeBtn = new JButton("🎯 Assign KTU Grade");
        JButton refreshBtn = new JButton("🔄 Refresh");

        controls.add(updateAttBtn);
        controls.add(updateCieBtn);
        controls.add(updateGradeBtn);
        controls.add(refreshBtn);

        // Center split tables
        attendanceTableModel = new DefaultTableModel(
            new String[]{"Course Code", "Course Title", "Credits", "Attended", "Total Classes", "Attendance %", "KTU Status"}, 0);
        JTable attTable = new JTable(attendanceTableModel);
        attTable.setRowHeight(22);

        internalsTableModel = new DefaultTableModel(
            new String[]{"Course Code", "Course Title", "Series 1 (20)", "Series 2 (20)", "Assign (10)", "Total CIE (50)", "Grade", "Grade Point"}, 0);
        JTable cieTable = new JTable(internalsTableModel);
        cieTable.setRowHeight(22);

        JPanel tablesPanel = new JPanel(new GridLayout(2, 1, 10, 10));

        JPanel attPanel = new JPanel(new BorderLayout(5, 5));
        JLabel attTitle = new JLabel("📌 Course Attendance (KTU 75% Minimum Threshold Tracker)");
        attTitle.setFont(new Font("SansSerif", Font.BOLD, 13));
        attPanel.add(attTitle, BorderLayout.NORTH);
        attPanel.add(new JScrollPane(attTable), BorderLayout.CENTER);

        JPanel ciePanel = new JPanel(new BorderLayout(5, 5));
        JLabel cieTitle = new JLabel("📈 Continuous Internal Evaluation (CIE / 50) & KTU Course Grades");
        cieTitle.setFont(new Font("SansSerif", Font.BOLD, 13));
        ciePanel.add(cieTitle, BorderLayout.NORTH);
        ciePanel.add(new JScrollPane(cieTable), BorderLayout.CENTER);

        tablesPanel.add(attPanel);
        tablesPanel.add(ciePanel);

        // Summary Bar
        JPanel summaryBar = new JPanel(new BorderLayout(10, 5));
        summaryBar.setBackground(new Color(30, 41, 59));
        summaryBar.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        sgpaLabel = new JLabel("KTU S3 SGPA: - / 10.0");
        sgpaLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        sgpaLabel.setForeground(new Color(52, 211, 153));

        classificationLabel = new JLabel("Classification: -");
        classificationLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        classificationLabel.setForeground(Color.WHITE);

        JLabel rulesHint = new JLabel("KTU Rules: Min Attendance 75% | Condonation 60-74% | Detained <60% | S=10, A+=9, A=8.5, B+=8, B=7.5, C+=7, C=6.5, D=6, P=5.5, F=0");
        rulesHint.setFont(new Font("SansSerif", Font.ITALIC, 11));
        rulesHint.setForeground(new Color(148, 163, 184));

        JPanel leftBox = new JPanel(new GridLayout(2, 1, 2, 2));
        leftBox.setOpaque(false);
        leftBox.add(sgpaLabel);
        leftBox.add(classificationLabel);

        summaryBar.add(leftBox, BorderLayout.WEST);
        summaryBar.add(rulesHint, BorderLayout.SOUTH);

        // Listeners
        updateAttBtn.addActionListener(e -> {
            String userStr = (String) userCombo.getSelectedItem();
            if (userStr == null) return;
            String userId = userStr.split(" - ")[0];

            try {
                List<Course> courses = dao.getAllCourses();
                String[] courseCodes = courses.stream().map(Course::getCourseCode).toArray(String[]::new);
                String selectedCode = (String) JOptionPane.showInputDialog(this, "Select Course:", "Update Attendance", 
                        JOptionPane.PLAIN_MESSAGE, null, courseCodes, courseCodes[0]);
                if (selectedCode == null) return;

                String attStr = JOptionPane.showInputDialog(this, "Classes Attended:", "36");
                if (attStr == null) return;
                String totStr = JOptionPane.showInputDialog(this, "Total Classes Conducted:", "40");
                if (totStr == null) return;

                int attended = Integer.parseInt(attStr.trim());
                int total = Integer.parseInt(totStr.trim());

                dao.recordAttendance(userId, selectedCode, attended, total);
                JOptionPane.showMessageDialog(this, "Attendance recorded successfully: " + attended + "/" + total, 
                        "Attendance Updated", JOptionPane.INFORMATION_MESSAGE);
                loadAcademicData();
            } catch (LowAttendanceException ex) {
                JOptionPane.showMessageDialog(this, "⚠️ KTU ATTENDANCE WARNING:\n" + ex.getMessage(), 
                        "Attendance Shortage Warning", JOptionPane.WARNING_MESSAGE);
                loadAcademicData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        updateCieBtn.addActionListener(e -> {
            String userStr = (String) userCombo.getSelectedItem();
            if (userStr == null) return;
            String userId = userStr.split(" - ")[0];

            try {
                List<Course> courses = dao.getAllCourses();
                String[] courseCodes = courses.stream().map(Course::getCourseCode).toArray(String[]::new);
                String selectedCode = (String) JOptionPane.showInputDialog(this, "Select Course:", "Enter CIE Internals", 
                        JOptionPane.PLAIN_MESSAGE, null, courseCodes, courseCodes[0]);
                if (selectedCode == null) return;

                String s1Str = JOptionPane.showInputDialog(this, "Series Test 1 Mark (Max 20):", "18.0");
                if (s1Str == null) return;
                String s2Str = JOptionPane.showInputDialog(this, "Series Test 2 Mark (Max 20):", "19.0");
                if (s2Str == null) return;
                String aStr = JOptionPane.showInputDialog(this, "Assignments & Quizzes Mark (Max 10):", "9.5");
                if (aStr == null) return;

                double s1 = Double.parseDouble(s1Str.trim());
                double s2 = Double.parseDouble(s2Str.trim());
                double a = Double.parseDouble(aStr.trim());

                dao.recordInternals(userId, selectedCode, s1, s2, a);
                double total = s1 + s2 + a;
                JOptionPane.showMessageDialog(this, String.format("CIE Recorded! Total Internal: %.1f / 50 (%s)", 
                        total, (total >= 22.5 ? "Passed CIE" : "CIE Shortage")), "CIE Updated", JOptionPane.INFORMATION_MESSAGE);
                loadAcademicData();
            } catch (InvalidMarkException ex) {
                JOptionPane.showMessageDialog(this, "Validation Error: " + ex.getMessage(), "Invalid Mark", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        updateGradeBtn.addActionListener(e -> {
            String userStr = (String) userCombo.getSelectedItem();
            if (userStr == null) return;
            String userId = userStr.split(" - ")[0];

            try {
                List<Course> courses = dao.getAllCourses();
                String[] courseCodes = courses.stream().map(Course::getCourseCode).toArray(String[]::new);
                String selectedCode = (String) JOptionPane.showInputDialog(this, "Select Course:", "Assign Grade", 
                        JOptionPane.PLAIN_MESSAGE, null, courseCodes, courseCodes[0]);
                if (selectedCode == null) return;

                KtuGrade[] grades = KtuGrade.values();
                KtuGrade selectedGrade = (KtuGrade) JOptionPane.showInputDialog(this, "Select KTU Grade:", "Assign Grade",
                        JOptionPane.PLAIN_MESSAGE, null, grades, KtuGrade.S);
                if (selectedGrade == null) return;

                dao.recordCourseGrade(userId, selectedCode, selectedGrade);
                JOptionPane.showMessageDialog(this, "Recorded grade " + selectedGrade + " for " + selectedCode, 
                        "Grade Recorded", JOptionPane.INFORMATION_MESSAGE);
                loadAcademicData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        refreshBtn.addActionListener(e -> loadAcademicData());

        panel.add(controls, BorderLayout.NORTH);
        panel.add(tablesPanel, BorderLayout.CENTER);
        panel.add(summaryBar, BorderLayout.SOUTH);
        return panel;
    }

    // ==========================================
    // Data Loading
    // ==========================================
    private void loadAllData() {
        try {
            // Load Users into Combo
            String currentSelected = (String) userCombo.getSelectedItem();
            userCombo.removeAllItems();
            List<User> users = dao.getAllUsers();
            for (User u : users) {
                String item = u.getUserId() + " - " + u.getName() + " (" + u.getRole() + ")" + (u.getFineBalance() > 0 ? " [FINE: Rs." + u.getFineBalance() + "]" : "");
                userCombo.addItem(item);
                if (currentSelected != null && currentSelected.startsWith(u.getUserId())) {
                    userCombo.setSelectedItem(item);
                }
            }

            // Load Sports Table
            sportsTableModel.setRowCount(0);
            List<Court> courts = dao.getAllCourts();
            String[] slots = new String[]{"06:00-07:00", "07:00-08:00", "08:00-09:00", "16:00-17:00", "17:00-18:00", "18:00-19:00", "19:00-20:00"};
            for (Court c : courts) {
                java.util.Map<String, User> resMap = c.getSlotReservations();
                for (String s : slots) {
                    User resUser = resMap.get(s);
                    String reservedBy = (resUser != null) ? (resUser.getUserId() + " (" + resUser.getName() + ")") : "Available";
                    sportsTableModel.addRow(new Object[]{c.getCourtId(), c.getCourtType(), s, reservedBy});
                }
            }

            // Load Hostel Table
            hostelTableModel.setRowCount(0);
            List<HostelStudent> students = dao.getAllHostelStudents();
            for (HostelStudent s : students) {
                hostelTableModel.addRow(new Object[]{
                    s.getRollNo(), s.getName(), s.getAssignedRoom().getRoomNumber(),
                    s.getAssignedRoom().getClass().getSimpleName(), "Rs. " + s.getAssignedRoom().calculateMonthlyTariff(),
                    s.getChosenMealPlan().getClass().getSimpleName(), s.getLeavesThisMonth()
                });
            }

            // Load Library Table
            libraryTableModel.setRowCount(0);
            List<Book> books = dao.getAllBooks();
            for (Book b : books) {
                libraryTableModel.addRow(new Object[]{
                    b.getIsbn(), b.getTitle(), b.getAuthor(), b.getCategory(),
                    b.isAvailable() ? "Available" : "Checked Out",
                    b.isAvailable() ? "-" : (b.getCurrentBorrower() != null ? b.getCurrentBorrower().getName() : "Unknown")
                });
            }

            // Load Academic Table
            loadAcademicData();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Connection Error: " + e.getMessage(), "SQL Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadAcademicData() {
        if (attendanceTableModel == null || internalsTableModel == null || userCombo == null) return;
        String userStr = (String) userCombo.getSelectedItem();
        if (userStr == null) return;
        String userId = userStr.split(" - ")[0];

        try {
            AcademicProfile profile = dao.getStudentAcademicProfile(userId);
            List<Course> courses = dao.getAllCourses();

            // Populate Attendance Table
            attendanceTableModel.setRowCount(0);
            for (Course c : courses) {
                AttendanceRecord att = profile.getAttendance(c.getCourseCode());
                int attended = (att != null) ? att.getClassesAttended() : 0;
                int total = (att != null) ? att.getTotalClasses() : 0;
                double pct = (att != null) ? att.getPercentage() : 0.0;
                String status = (att != null) ? att.getStatusBadge() : "No Records";

                attendanceTableModel.addRow(new Object[]{
                    c.getCourseCode(), c.getCourseName(), c.getCredits(),
                    attended, total, String.format("%.1f%%", pct), status
                });
            }

            // Populate Internals & Grades Table
            internalsTableModel.setRowCount(0);
            for (Course c : courses) {
                InternalAssessment cie = profile.getInternals(c.getCourseCode());
                double s1 = (cie != null) ? cie.getSeriesTest1() : 0.0;
                double s2 = (cie != null) ? cie.getSeriesTest2() : 0.0;
                double a = (cie != null) ? cie.getAssignments() : 0.0;
                double totCie = (cie != null) ? cie.calculateTotalCie() : 0.0;
                KtuGrade grade = profile.getGrade(c.getCourseCode());

                internalsTableModel.addRow(new Object[]{
                    c.getCourseCode(), c.getCourseName(),
                    String.format("%.1f", s1), String.format("%.1f", s2), String.format("%.1f", a),
                    String.format("%.1f / 50", totCie),
                    (grade != null ? grade.getLabel() : "-"),
                    (grade != null ? String.format("%.1f", grade.getGradePoint()) : "-")
                });
            }

            // Update SGPA and Classification
            double sgpa = profile.calculateSGPA();
            sgpaLabel.setText(String.format("🎓 KTU S3 SGPA: %.2f / 10.0", sgpa));
            classificationLabel.setText("Classification: " + profile.getClassClassification());

        } catch (SQLException e) {
            System.err.println("Failed to load academic data: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new CampusSwingApp().setVisible(true);
        });
    }
}
