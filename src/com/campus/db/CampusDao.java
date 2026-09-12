package com.campus.db;

import com.campus.academic.*;
import com.campus.exceptions.*;
import com.campus.hostel.*;
import com.campus.library.*;
import com.campus.sports.*;

import java.sql.*;
import java.util.*;

/**
 * Data Access Object (DAO) for executing SQL queries and mapping between JDBC ResultSets
 * and the domain Object Model. Enforces all business rules and custom exceptions.
 */
public class CampusDao {

    private final DatabaseManager db;

    public CampusDao() {
        this.db = DatabaseManager.getInstance();
    }

    // ==========================================
    // Users CRUD
    // ==========================================

    public List<User> getAllUsers() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY user_id";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String id = rs.getString("user_id");
                String name = rs.getString("name");
                String role = rs.getString("role");
                double fine = rs.getDouble("fine_balance");

                User u;
                if ("Faculty".equalsIgnoreCase(role)) {
                    u = new Faculty(id, name);
                } else if ("Coach".equalsIgnoreCase(role)) {
                    u = new Coach(id, name);
                } else {
                    u = new Student(id, name);
                }
                if (fine > 0) {
                    u.addFine(fine);
                }
                list.add(u);
            }
        }
        return list;
    }

    public User getUserById(String userId) throws SQLException {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String role = rs.getString("role");
                    String name = rs.getString("name");
                    double fine = rs.getDouble("fine_balance");
                    User u = "Faculty".equalsIgnoreCase(role) ? new Faculty(userId, name)
                           : ("Coach".equalsIgnoreCase(role) ? new Coach(userId, name) : new Student(userId, name));
                    if (fine > 0) u.addFine(fine);
                    return u;
                }
            }
        }
        return null;
    }

    public void updateUserFine(String userId, double newBalance) throws SQLException {
        String sql = "UPDATE users SET fine_balance = ? WHERE user_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, Math.max(0, newBalance));
            ps.setString(2, userId);
            ps.executeUpdate();
        }
    }

    // ==========================================
    // Sports Courts & Reservations CRUD
    // ==========================================

    public List<Court> getAllCourts() throws SQLException {
        List<Court> courts = new ArrayList<>();
        String sql = "SELECT * FROM courts ORDER BY court_id";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                courts.add(new Court(rs.getString("court_id"), rs.getString("court_type")));
            }
        }

        // Load reservations
        Map<String, User> userMap = new HashMap<>();
        for (User u : getAllUsers()) {
            userMap.put(u.getUserId().toUpperCase(), u);
        }

        String resSql = "SELECT * FROM court_reservations";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(resSql)) {
            while (rs.next()) {
                String cId = rs.getString("court_id");
                String slot = rs.getString("slot");
                String uId = rs.getString("user_id");
                User resUser = userMap.get(uId.toUpperCase());
                if (resUser == null) {
                    resUser = new Student(uId, uId);
                }
                for (Court c : courts) {
                    if (c.getCourtId().equalsIgnoreCase(cId)) {
                        c.assignReservation(slot, resUser);
                    }
                }
            }
        }
        return courts;
    }

    public synchronized void reserveCourt(String courtId, String slot, String userId) 
            throws SQLException, SlotAlreadyBookedException, OutstandingFineException, BookingQuotaExceededException {
        User user = getUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + userId);
        }

        if (user.getFineBalance() > 0) {
            throw new OutstandingFineException(user.getName(), user.getFineBalance());
        }

        // Count active reservations in DB
        int activeBookings = 0;
        String countSql = "SELECT COUNT(*) FROM court_reservations WHERE user_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(countSql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) activeBookings = rs.getInt(1);
            }
        }

        if (activeBookings >= user.getBookingLimit()) {
            throw new BookingQuotaExceededException(user.getName(), user.getBookingLimit(), activeBookings);
        }

        // Check slot collision
        String checkSql = "SELECT id FROM court_reservations WHERE court_id = ? AND slot = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setString(1, courtId);
            ps.setString(2, slot);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    throw new SlotAlreadyBookedException(slot);
                }
            }
        }

        // Insert reservation
        String insertSql = "INSERT INTO court_reservations (court_id, slot, user_id) VALUES (?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertSql)) {
            ps.setString(1, courtId);
            ps.setString(2, slot);
            ps.setString(3, userId);
            ps.executeUpdate();
        }
    }

    public synchronized boolean releaseCourt(String courtId, String slot, String requestingUserId) throws SQLException {
        String ownerSql = "SELECT user_id FROM court_reservations WHERE court_id = ? AND slot = ?";
        String owner = null;
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(ownerSql)) {
            ps.setString(1, courtId);
            ps.setString(2, slot);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) owner = rs.getString("user_id");
            }
        }

        if (owner == null) return false;
        if (requestingUserId != null && !requestingUserId.equalsIgnoreCase(owner) && !"ADMIN".equalsIgnoreCase(requestingUserId)) {
            return false;
        }

        String deleteSql = "DELETE FROM court_reservations WHERE court_id = ? AND slot = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(deleteSql)) {
            ps.setString(1, courtId);
            ps.setString(2, slot);
            ps.executeUpdate();
            return true;
        }
    }

    // ==========================================
    // Hostel Module CRUD
    // ==========================================

    public List<HostelStudent> getAllHostelStudents() throws SQLException {
        List<HostelStudent> list = new ArrayList<>();
        String sql = "SELECT * FROM hostel_students ORDER BY roll_no";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String roll = rs.getString("roll_no");
                String name = rs.getString("name");
                int roomNum = rs.getInt("room_number");
                String roomType = rs.getString("room_type");
                String mealPlanName = rs.getString("meal_plan");
                int leaves = rs.getInt("leaves_this_month");

                BaseRoom room = "ACSuite".equalsIgnoreCase(roomType) 
                    ? new ACSuite(roomNum, 6000.0) 
                    : new SingleOccupancy(roomNum, 4500.0);
                MealPlan mealPlan = "SpecialDietPlan".equalsIgnoreCase(mealPlanName) 
                    ? new SpecialDietPlan() 
                    : new StandardPlan();

                HostelStudent s = new HostelStudent(roll, name, room, mealPlan);
                if (leaves > 0) {
                    try {
                        s.applyLeave(leaves, 30);
                    } catch (InvalidLeaveDaysException ignored) {}
                }
                list.add(s);
            }
        }
        return list;
    }

    public void registerHostelStudent(String rollNo, String name, BaseRoom room, MealPlan mealPlan) throws SQLException {
        String sql = "INSERT OR REPLACE INTO hostel_students (roll_no, name, room_number, room_type, room_tariff, meal_plan, leaves_this_month) VALUES (?, ?, ?, ?, ?, ?, 0)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rollNo);
            ps.setString(2, name);
            ps.setInt(3, room.getRoomNumber());
            ps.setString(4, room.getClass().getSimpleName());
            ps.setDouble(5, room.calculateMonthlyTariff());
            ps.setString(6, mealPlan.getClass().getSimpleName());
            ps.executeUpdate();
        }
    }

    public void applyHostelLeave(String rollNo, int days) throws SQLException, InvalidLeaveDaysException {
        if (days < 0 || days > 30) {
            throw new InvalidLeaveDaysException("Leave days must be between 1 and 30 days. Received: " + days);
        }
        String sql = "UPDATE hostel_students SET leaves_this_month = leaves_this_month + ? WHERE roll_no = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, days);
            ps.setString(2, rollNo);
            ps.executeUpdate();
        }
    }

    // ==========================================
    // Library Module CRUD
    // ==========================================

    public List<Book> getAllBooks() throws SQLException {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM library_books ORDER BY isbn";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Book b = new Book(
                    rs.getString("isbn"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("category")
                );
                if (rs.getInt("is_available") == 0) {
                    String uId = rs.getString("borrower_id");
                    String uName = rs.getString("borrower_name");
                    User borrower = getUserById(uId);
                    if (borrower == null) borrower = new Student(uId, (uName != null ? uName : uId));
                    b.issueTo(borrower);
                }
                books.add(b);
            }
        }
        return books;
    }

    public synchronized void borrowBook(String isbn, String userId) 
            throws SQLException, BookNotAvailableException, OutstandingFineException {
        User user = getUserById(userId);
        if (user == null) throw new IllegalArgumentException("User not found: " + userId);
        if (user.getFineBalance() > 0) {
            throw new OutstandingFineException(user.getName(), user.getFineBalance());
        }

        // Check availability
        String bookSql = "SELECT is_available, title FROM library_books WHERE isbn = ?";
        String bookTitle = "Unknown Title";
        boolean isAvailable = false;
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(bookSql)) {
            ps.setString(1, isbn);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    bookTitle = rs.getString("title");
                    isAvailable = (rs.getInt("is_available") == 1);
                }
            }
        }

        if (!isAvailable) {
            throw new BookNotAvailableException(isbn, bookTitle);
        }

        int loanDays = (user instanceof Student) ? 14 : ((user instanceof Coach) ? 21 : 30);
        String updateSql = "UPDATE library_books SET is_available = 0, borrower_id = ?, borrower_name = ? WHERE isbn = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(updateSql)) {
            ps.setString(1, user.getUserId());
            ps.setString(2, user.getName());
            ps.setString(3, isbn);
            ps.executeUpdate();
        }

        String loanSql = "INSERT INTO library_loans (loan_id, isbn, user_id, due_days, is_returned) VALUES (?, ?, ?, ?, 0)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(loanSql)) {
            ps.setString(1, "LN" + System.currentTimeMillis() % 100000);
            ps.setString(2, isbn);
            ps.setString(3, userId);
            ps.setInt(4, loanDays);
            ps.executeUpdate();
        }
    }

    public synchronized double returnBook(String isbn, int overdueDays) throws SQLException {
        String updateSql = "UPDATE library_books SET is_available = 1, borrower_id = NULL, borrower_name = NULL WHERE isbn = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(updateSql)) {
            ps.setString(1, isbn);
            ps.executeUpdate();
        }

        double penalty = 0.0;
        if (overdueDays > 0) {
            penalty = overdueDays * 2.0; // Rs. 2/day overdue
            // Find borrower to add fine
            String loanSql = "SELECT user_id FROM library_loans WHERE isbn = ? AND is_returned = 0 LIMIT 1";
            try (Connection conn = db.getConnection();
                 PreparedStatement ps = conn.prepareStatement(loanSql)) {
                ps.setString(1, isbn);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String borrowerId = rs.getString("user_id");
                        User u = getUserById(borrowerId);
                        if (u != null) {
                            updateUserFine(borrowerId, u.getFineBalance() + penalty);
                        }
                    }
                }
            }
        }

        String markLoanSql = "UPDATE library_loans SET is_returned = 1 WHERE isbn = ? AND is_returned = 0";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(markLoanSql)) {
            ps.setString(1, isbn);
            ps.executeUpdate();
        }
        return penalty;
    }

    // ==========================================
    // KTU Academic & Attendance Module CRUD
    // ==========================================

    public List<Course> getAllCourses() throws SQLException {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT * FROM academic_courses ORDER BY course_code";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Course(
                    rs.getString("course_code"),
                    rs.getString("name"),
                    rs.getInt("credits"),
                    rs.getInt("semester")
                ));
            }
        }
        return list;
    }

    public void recordAttendance(String studentId, String courseCode, int attended, int total) 
            throws SQLException, LowAttendanceException {
        if (total < 0 || attended < 0 || attended > total) {
            throw new IllegalArgumentException("Invalid attendance counts: attended=" + attended + ", total=" + total);
        }
        String sql = "INSERT OR REPLACE INTO student_attendance (student_id, course_code, classes_attended, total_classes) VALUES (?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.setString(2, courseCode);
            ps.setInt(3, attended);
            ps.setInt(4, total);
            ps.executeUpdate();
        }

        double pct = (total == 0) ? 100.0 : ((double) attended / total * 100.0);
        if (pct < AttendanceRecord.KTU_MIN_THRESHOLD) {
            throw new LowAttendanceException(courseCode, pct);
        }
    }

    public void recordInternals(String studentId, String courseCode, double series1, double series2, double assignments) 
            throws SQLException, InvalidMarkException {
        if (series1 < 0 || series1 > InternalAssessment.MAX_SERIES_TEST) {
            throw new InvalidMarkException("Series 1 mark must be between 0 and 20. Provided: " + series1);
        }
        if (series2 < 0 || series2 > InternalAssessment.MAX_SERIES_TEST) {
            throw new InvalidMarkException("Series 2 mark must be between 0 and 20. Provided: " + series2);
        }
        if (assignments < 0 || assignments > InternalAssessment.MAX_ASSIGNMENTS) {
            throw new InvalidMarkException("Assignments mark must be between 0 and 10. Provided: " + assignments);
        }

        double totalCie = Math.min(50.0, series1 + series2 + assignments);
        String sql = "INSERT OR REPLACE INTO student_internals (student_id, course_code, series_1, series_2, assignments, total_cie) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.setString(2, courseCode);
            ps.setDouble(3, series1);
            ps.setDouble(4, series2);
            ps.setDouble(5, assignments);
            ps.setDouble(6, totalCie);
            ps.executeUpdate();
        }
    }

    public void recordCourseGrade(String studentId, String courseCode, KtuGrade grade) throws SQLException {
        String sql = "INSERT OR REPLACE INTO student_course_grades (student_id, course_code, grade, grade_point) VALUES (?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.setString(2, courseCode);
            ps.setString(3, grade.getLabel());
            ps.setDouble(4, grade.getGradePoint());
            ps.executeUpdate();
        }
    }

    public AcademicProfile getStudentAcademicProfile(String studentId) throws SQLException {
        User u = getUserById(studentId);
        String name = (u != null) ? u.getName() : studentId;
        AcademicProfile profile = new AcademicProfile(studentId, name);

        // Load all courses
        List<Course> courses = getAllCourses();
        for (Course c : courses) {
            profile.enrollCourse(c);
        }

        // Load Attendance
        String attSql = "SELECT * FROM student_attendance WHERE student_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(attSql)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    profile.recordAttendance(
                        rs.getString("course_code"),
                        rs.getInt("classes_attended"),
                        rs.getInt("total_classes")
                    );
                }
            }
        }

        // Load Internals
        String cieSql = "SELECT * FROM student_internals WHERE student_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(cieSql)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    try {
                        profile.recordInternals(
                            rs.getString("course_code"),
                            rs.getDouble("series_1"),
                            rs.getDouble("series_2"),
                            rs.getDouble("assignments")
                        );
                    } catch (InvalidMarkException ignored) {}
                }
            }
        }

        // Load Grades
        String grSql = "SELECT * FROM student_course_grades WHERE student_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(grSql)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    KtuGrade gr = KtuGrade.fromString(rs.getString("grade"));
                    profile.recordGrade(rs.getString("course_code"), gr);
                }
            }
        }

        return profile;
    }
}
