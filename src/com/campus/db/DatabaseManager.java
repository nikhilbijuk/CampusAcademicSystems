package com.campus.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Singleton DatabaseManager for SQLite JDBC persistence.
 * Implements thread-safe Double-Checked Locking Singleton Design Pattern (Module 3 & 4 requirement).
 */
public class DatabaseManager {

    private static final String DB_DIRECTORY = "data";
    private static final String DB_URL = "jdbc:sqlite:" + DB_DIRECTORY + "/campus.db";

    // Singleton volatile instance
    private static volatile DatabaseManager instance;

    // Private constructor to prevent direct instantiation
    private DatabaseManager() {
        initDatabase();
    }

    /**
     * Thread-safe Singleton access point.
     * @return Single shared DatabaseManager instance.
     */
    public static DatabaseManager getInstance() {
        if (instance == null) {
            synchronized (DatabaseManager.class) {
                if (instance == null) {
                    instance = new DatabaseManager();
                }
            }
        }
        return instance;
    }

    /**
     * Obtains a live JDBC Connection to SQLite database.
     */
    public Connection getConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ignored) {
            // Driver automatically registered in modern JDBC
        }
        return DriverManager.getConnection(DB_URL);
    }

    /**
     * Initializes the relational database schema and seeds initial data if empty.
     */
    private void initDatabase() {
        File dataDir = new File(DB_DIRECTORY);
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // 1. Users Table
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS users (" +
                "  user_id TEXT PRIMARY KEY," +
                "  name TEXT NOT NULL," +
                "  role TEXT NOT NULL," +
                "  booking_limit INTEGER NOT NULL," +
                "  fine_balance REAL DEFAULT 0.0" +
                ");"
            );

            // 2. Courts Table
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS courts (" +
                "  court_id TEXT PRIMARY KEY," +
                "  court_type TEXT NOT NULL" +
                ");"
            );

            // 3. Court Reservations Table
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS court_reservations (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  court_id TEXT NOT NULL," +
                "  slot TEXT NOT NULL," +
                "  user_id TEXT NOT NULL," +
                "  reserved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "  UNIQUE(court_id, slot)," +
                "  FOREIGN KEY(court_id) REFERENCES courts(court_id)," +
                "  FOREIGN KEY(user_id) REFERENCES users(user_id)" +
                ");"
            );

            // 4. Hostel Students Table
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS hostel_students (" +
                "  roll_no TEXT PRIMARY KEY," +
                "  name TEXT NOT NULL," +
                "  room_number INTEGER NOT NULL," +
                "  room_type TEXT NOT NULL," +
                "  room_tariff REAL NOT NULL," +
                "  meal_plan TEXT NOT NULL," +
                "  leaves_this_month INTEGER DEFAULT 0" +
                ");"
            );

            // 5. Library Books Table
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS library_books (" +
                "  isbn TEXT PRIMARY KEY," +
                "  title TEXT NOT NULL," +
                "  author TEXT NOT NULL," +
                "  category TEXT NOT NULL," +
                "  is_available INTEGER DEFAULT 1," +
                "  borrower_id TEXT," +
                "  borrower_name TEXT" +
                ");"
            );

            // 6. Library Loans Table
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS library_loans (" +
                "  loan_id TEXT PRIMARY KEY," +
                "  isbn TEXT NOT NULL," +
                "  user_id TEXT NOT NULL," +
                "  due_days INTEGER NOT NULL," +
                "  is_returned INTEGER DEFAULT 0," +
                "  FOREIGN KEY(isbn) REFERENCES library_books(isbn)," +
                "  FOREIGN KEY(user_id) REFERENCES users(user_id)" +
                ");"
            );

            // 7. KTU Academic Courses Table
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS academic_courses (" +
                "  course_code TEXT PRIMARY KEY," +
                "  name TEXT NOT NULL," +
                "  credits INTEGER NOT NULL," +
                "  semester INTEGER NOT NULL" +
                ");"
            );

            // 8. Student Attendance Table
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS student_attendance (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  student_id TEXT NOT NULL," +
                "  course_code TEXT NOT NULL," +
                "  classes_attended INTEGER NOT NULL," +
                "  total_classes INTEGER NOT NULL," +
                "  UNIQUE(student_id, course_code)," +
                "  FOREIGN KEY(student_id) REFERENCES users(user_id)," +
                "  FOREIGN KEY(course_code) REFERENCES academic_courses(course_code)" +
                ");"
            );

            // 9. Student Internal Assessment (CIE) Table
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS student_internals (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  student_id TEXT NOT NULL," +
                "  course_code TEXT NOT NULL," +
                "  series_1 REAL NOT NULL," +
                "  series_2 REAL NOT NULL," +
                "  assignments REAL NOT NULL," +
                "  total_cie REAL NOT NULL," +
                "  UNIQUE(student_id, course_code)," +
                "  FOREIGN KEY(student_id) REFERENCES users(user_id)," +
                "  FOREIGN KEY(course_code) REFERENCES academic_courses(course_code)" +
                ");"
            );

            // 10. Student Course Grades Table
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS student_course_grades (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  student_id TEXT NOT NULL," +
                "  course_code TEXT NOT NULL," +
                "  grade TEXT NOT NULL," +
                "  grade_point REAL NOT NULL," +
                "  UNIQUE(student_id, course_code)," +
                "  FOREIGN KEY(student_id) REFERENCES users(user_id)," +
                "  FOREIGN KEY(course_code) REFERENCES academic_courses(course_code)" +
                ");"
            );

            // Seed initial data if tables are empty
            seedInitialData(conn);

        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Schema initialization failed: " + e.getMessage());
        }
    }

    private void seedInitialData(Connection conn) throws SQLException {
        // Check if users exist
        try (Statement checkStmt = conn.createStatement();
             ResultSet rs = checkStmt.executeQuery("SELECT COUNT(*) FROM users")) {
            if (rs.next() && rs.getInt(1) > 0) {
                // Ensure courses seeded even if users existed from earlier runs
                seedAcademicCoursesIfEmpty(conn);
                return;
            }
        }

        // Seed Users
        String userSql = "INSERT INTO users (user_id, name, role, booking_limit, fine_balance) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(userSql)) {
            insertUser(ps, "S101", "Rahul Sharma", "Student", 2, 0.0);
            insertUser(ps, "S102", "Priya Nair", "Student", 2, 0.0);
            insertUser(ps, "F201", "Dr. Suresh Kumar", "Faculty", 5, 0.0);
            insertUser(ps, "C301", "Coach Vikram", "Coach", 10, 0.0);
        }

        // Seed Courts
        String courtSql = "INSERT INTO courts (court_id, court_type) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(courtSql)) {
            ps.setString(1, "CRT1"); ps.setString(2, "Badminton"); ps.executeUpdate();
            ps.setString(1, "CRT2"); ps.setString(2, "Tennis"); ps.executeUpdate();
            ps.setString(1, "CRT3"); ps.setString(2, "Basketball"); ps.executeUpdate();
        }

        // Seed Hostel Students
        String hostelSql = "INSERT INTO hostel_students (roll_no, name, room_number, room_type, room_tariff, meal_plan, leaves_this_month) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(hostelSql)) {
            ps.setString(1, "STU202"); ps.setString(2, "Anjali Menon"); ps.setInt(3, 302);
            ps.setString(4, "SingleOccupancy"); ps.setDouble(5, 5400.0); ps.setString(6, "StandardPlan"); ps.setInt(7, 0);
            ps.executeUpdate();

            ps.setString(1, "STU203"); ps.setString(2, "Rohan Das"); ps.setInt(3, 101);
            ps.setString(4, "ACSuite"); ps.setDouble(5, 7500.0); ps.setString(6, "SpecialDietPlan"); ps.setInt(7, 0);
            ps.executeUpdate();
        }

        // Seed Books
        String bookSql = "INSERT INTO library_books (isbn, title, author, category, is_available) VALUES (?, ?, ?, ?, 1)";
        try (PreparedStatement ps = conn.prepareStatement(bookSql)) {
            insertBook(ps, "978-0262033848", "Introduction to Algorithms (CLRS)", "Thomas H. Cormen", "Computer Science");
            insertBook(ps, "978-1118063330", "Operating System Concepts", "Abraham Silberschatz", "Systems");
            insertBook(ps, "978-0078022159", "Database System Concepts", "Henry F. Korth", "Databases");
            insertBook(ps, "978-0132126953", "Computer Networks", "Andrew S. Tanenbaum", "Networks");
            insertBook(ps, "978-0132350884", "Clean Code: Handbook of Agile Craftsmanship", "Robert C. Martin", "Software Engineering");
        }

        // Seed KTU Courses & Academic Performance
        seedAcademicCoursesIfEmpty(conn);
    }

    private void seedAcademicCoursesIfEmpty(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM academic_courses")) {
            if (rs.next() && rs.getInt(1) > 0) return;
        }

        String courseSql = "INSERT INTO academic_courses (course_code, name, credits, semester) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(courseSql)) {
            insertCourse(ps, "MAT203", "Discrete Mathematical Structures", 4, 3);
            insertCourse(ps, "CST201", "Data Structures", 4, 3);
            insertCourse(ps, "CST203", "Logic System Design", 4, 3);
            insertCourse(ps, "CST205", "Object Oriented Programming (Java)", 4, 3);
            insertCourse(ps, "EST200", "Design & Engineering", 2, 3);
            insertCourse(ps, "MCN201", "Sustainable Engineering", 0, 3);
        }

        // Seed sample attendance for S101 (Rahul Sharma)
        String attSql = "INSERT OR REPLACE INTO student_attendance (student_id, course_code, classes_attended, total_classes) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(attSql)) {
            insertAtt(ps, "S101", "MAT203", 38, 40); // 95%
            insertAtt(ps, "S101", "CST201", 35, 40); // 87.5%
            insertAtt(ps, "S101", "CST203", 32, 40); // 80%
            insertAtt(ps, "S101", "CST205", 36, 40); // 90%
            insertAtt(ps, "S101", "EST200", 18, 20); // 90%
            insertAtt(ps, "S101", "MCN201", 19, 20); // 95%

            // Seed S102 (Priya Nair) with attendance warning cases:
            insertAtt(ps, "S102", "MAT203", 26, 40); // 65% (Condonation)
            insertAtt(ps, "S102", "CST201", 23, 40); // 57.5% (Detained / Shortage)
            insertAtt(ps, "S102", "CST205", 32, 40); // 80%
        }

        // Seed CIE Internals for S101
        String cieSql = "INSERT OR REPLACE INTO student_internals (student_id, course_code, series_1, series_2, assignments, total_cie) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(cieSql)) {
            insertCie(ps, "S101", "MAT203", 18.0, 19.0, 9.5, 46.5);
            insertCie(ps, "S101", "CST201", 17.0, 18.0, 9.0, 44.0);
            insertCie(ps, "S101", "CST203", 15.0, 16.0, 8.5, 39.5);
            insertCie(ps, "S101", "CST205", 19.0, 20.0, 10.0, 49.0);
            insertCie(ps, "S101", "EST200", 16.0, 17.0, 9.0, 42.0);
            insertCie(ps, "S101", "MCN201", 18.0, 18.0, 9.0, 45.0);

            // S102
            insertCie(ps, "S102", "MAT203", 12.0, 11.0, 7.0, 30.0);
            insertCie(ps, "S102", "CST201", 10.0, 9.0, 6.0, 25.0);
            insertCie(ps, "S102", "CST205", 16.0, 15.0, 8.0, 39.0);
        }

        // Seed Course Grades for S101
        String gradeSql = "INSERT OR REPLACE INTO student_course_grades (student_id, course_code, grade, grade_point) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(gradeSql)) {
            insertGrade(ps, "S101", "MAT203", "S", 10.0);
            insertGrade(ps, "S101", "CST201", "A+", 9.0);
            insertGrade(ps, "S101", "CST203", "A", 8.5);
            insertGrade(ps, "S101", "CST205", "S", 10.0);
            insertGrade(ps, "S101", "EST200", "A", 8.5);
            insertGrade(ps, "S101", "MCN201", "P", 5.5);

            // S102
            insertGrade(ps, "S102", "MAT203", "C+", 7.0);
            insertGrade(ps, "S102", "CST201", "FE", 0.0); // Attendance shortage
            insertGrade(ps, "S102", "CST205", "B+", 8.0);
        }
    }

    private void insertCourse(PreparedStatement ps, String code, String name, int cr, int sem) throws SQLException {
        ps.setString(1, code);
        ps.setString(2, name);
        ps.setInt(3, cr);
        ps.setInt(4, sem);
        ps.executeUpdate();
    }

    private void insertAtt(PreparedStatement ps, String stu, String code, int att, int tot) throws SQLException {
        ps.setString(1, stu);
        ps.setString(2, code);
        ps.setInt(3, att);
        ps.setInt(4, tot);
        ps.executeUpdate();
    }

    private void insertCie(PreparedStatement ps, String stu, String code, double s1, double s2, double a, double tot) throws SQLException {
        ps.setString(1, stu);
        ps.setString(2, code);
        ps.setDouble(3, s1);
        ps.setDouble(4, s2);
        ps.setDouble(5, a);
        ps.setDouble(6, tot);
        ps.executeUpdate();
    }

    private void insertGrade(PreparedStatement ps, String stu, String code, String gr, double gp) throws SQLException {
        ps.setString(1, stu);
        ps.setString(2, code);
        ps.setString(3, gr);
        ps.setDouble(4, gp);
        ps.executeUpdate();
    }

    private void insertUser(PreparedStatement ps, String id, String name, String role, int limit, double fine) throws SQLException {
        ps.setString(1, id);
        ps.setString(2, name);
        ps.setString(3, role);
        ps.setInt(4, limit);
        ps.setDouble(5, fine);
        ps.executeUpdate();
    }

    private void insertBook(PreparedStatement ps, String isbn, String title, String author, String cat) throws SQLException {
        ps.setString(1, isbn);
        ps.setString(2, title);
        ps.setString(3, author);
        ps.setString(4, cat);
        ps.executeUpdate();
    }
}
