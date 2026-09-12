package com.campus.exceptions;

/**
 * Checked exception thrown when a student's attendance falls below
 * the mandatory KTU 75% threshold required for university exam eligibility.
 */
public class LowAttendanceException extends Exception {
    private static final long serialVersionUID = 1L;

    private final String courseCode;
    private final double attendancePercentage;

    public LowAttendanceException(String courseCode, double attendancePercentage) {
        super(String.format("Attendance shortage in course %s: %.2f%% is below the mandatory KTU 75.0%% minimum threshold.", 
                courseCode, attendancePercentage));
        this.courseCode = courseCode;
        this.attendancePercentage = attendancePercentage;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public double getAttendancePercentage() {
        return attendancePercentage;
    }
}
