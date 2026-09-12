package com.campus.academic;

import com.campus.exceptions.InvalidMarkException;
import com.campus.exceptions.LowAttendanceException;
import java.io.Serializable;
import java.util.*;

/**
 * Encapsulates a student's complete academic portfolio, including course enrollments,
 * attendance records, continuous internal evaluations (CIE), and SGPA/CGPA calculations.
 */
public class AcademicProfile implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String studentId;
    private final String studentName;
    private final Map<String, Course> courseCatalog = new LinkedHashMap<>();
    private final Map<String, AttendanceRecord> attendanceRecords = new HashMap<>();
    private final Map<String, InternalAssessment> internalAssessments = new HashMap<>();
    private final Map<String, KtuGrade> courseGrades = new HashMap<>();

    public AcademicProfile(String studentId, String studentName) {
        this.studentId = studentId;
        this.studentName = studentName;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void enrollCourse(Course course) {
        if (course != null) {
            courseCatalog.put(course.getCourseCode(), course);
        }
    }

    public List<Course> getEnrolledCourses() {
        return new ArrayList<>(courseCatalog.values());
    }

    public void recordAttendance(String courseCode, int attended, int total) {
        Course course = courseCatalog.get(courseCode.toUpperCase());
        if (course == null) {
            course = new Course(courseCode, courseCode, 3, 3);
            enrollCourse(course);
        }
        attendanceRecords.put(courseCode.toUpperCase(), new AttendanceRecord(course, attended, total));
    }

    public AttendanceRecord getAttendance(String courseCode) {
        return attendanceRecords.get(courseCode.toUpperCase());
    }

    public Map<String, AttendanceRecord> getAllAttendance() {
        return Collections.unmodifiableMap(attendanceRecords);
    }

    public void recordInternals(String courseCode, double s1, double s2, double assign) throws InvalidMarkException {
        Course course = courseCatalog.get(courseCode.toUpperCase());
        if (course == null) {
            course = new Course(courseCode, courseCode, 3, 3);
            enrollCourse(course);
        }
        internalAssessments.put(courseCode.toUpperCase(), new InternalAssessment(course, s1, s2, assign));
    }

    public InternalAssessment getInternals(String courseCode) {
        return internalAssessments.get(courseCode.toUpperCase());
    }

    public Map<String, InternalAssessment> getAllInternals() {
        return Collections.unmodifiableMap(internalAssessments);
    }

    public void recordGrade(String courseCode, KtuGrade grade) {
        courseGrades.put(courseCode.toUpperCase(), grade != null ? grade : KtuGrade.F);
    }

    public KtuGrade getGrade(String courseCode) {
        return courseGrades.getOrDefault(courseCode.toUpperCase(), KtuGrade.F);
    }

    public Map<String, KtuGrade> getAllGrades() {
        return Collections.unmodifiableMap(courseGrades);
    }

    /**
     * Calculates the KTU Semester Grade Point Average (SGPA):
     * SGPA = Sum(Credits_i * GradePoints_i) / Sum(Credits_i)
     * Excludes audit/zero-credit courses.
     */
    public double calculateSGPA() {
        int totalCredits = 0;
        double totalWeightedPoints = 0.0;

        for (Course course : courseCatalog.values()) {
            int credits = course.getCredits();
            if (credits <= 0) continue; // Skip 0-credit audit courses

            KtuGrade grade = courseGrades.get(course.getCourseCode());
            if (grade != null) {
                totalCredits += credits;
                totalWeightedPoints += credits * grade.getGradePoint();
            }
        }

        if (totalCredits == 0) return 0.0;
        return Math.round((totalWeightedPoints / totalCredits) * 100.0) / 100.0;
    }

    public String getClassClassification() {
        boolean hasBacklog = false;
        for (KtuGrade g : courseGrades.values()) {
            if (!g.isPass()) {
                hasBacklog = true;
                break;
            }
        }

        if (hasBacklog) return "Withheld / Arrears Pending (F/FE)";
        double sgpa = calculateSGPA();
        if (sgpa >= 8.5) return "First Class with Distinction (Honours Candidate)";
        if (sgpa >= 6.5) return "First Class";
        if (sgpa >= 5.5) return "Pass Class";
        return "Not Eligible for Degree (SGPA < 5.5)";
    }

    /**
     * Checks if all enrolled courses meet the 75% attendance threshold.
     */
    public List<LowAttendanceException> checkAttendanceCompliance() {
        List<LowAttendanceException> shortages = new ArrayList<>();
        for (AttendanceRecord record : attendanceRecords.values()) {
            if (!record.isEligible()) {
                shortages.add(new LowAttendanceException(
                    record.getCourse().getCourseCode(), record.getPercentage()));
            }
        }
        return shortages;
    }
}
