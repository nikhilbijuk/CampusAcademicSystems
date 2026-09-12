package com.campus.academic;

import com.campus.exceptions.LowAttendanceException;
import java.io.Serializable;

/**
 * Tracks subject-level attendance and checks compliance with KTU's 75% minimum threshold.
 */
public class AttendanceRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final double KTU_MIN_THRESHOLD = 75.0;
    public static final double KTU_CONDONATION_THRESHOLD = 60.0;

    private final Course course;
    private int classesAttended;
    private int totalClasses;

    public AttendanceRecord(Course course, int classesAttended, int totalClasses) {
        if (course == null) {
            throw new IllegalArgumentException("Course cannot be null.");
        }
        if (totalClasses < 0 || classesAttended < 0 || classesAttended > totalClasses) {
            throw new IllegalArgumentException(String.format(
                "Invalid attendance numbers: attended=%d, total=%d", classesAttended, totalClasses));
        }
        this.course = course;
        this.classesAttended = classesAttended;
        this.totalClasses = totalClasses;
    }

    public Course getCourse() {
        return course;
    }

    public int getClassesAttended() {
        return classesAttended;
    }

    public int getTotalClasses() {
        return totalClasses;
    }

    /**
     * Calculates the attendance percentage. Returns 100.0 if no classes conducted yet.
     */
    public double getPercentage() {
        if (totalClasses == 0) return 100.0;
        return (double) classesAttended / totalClasses * 100.0;
    }

    public boolean isEligible() {
        return getPercentage() >= KTU_MIN_THRESHOLD;
    }

    public boolean isCondonationRequired() {
        double pct = getPercentage();
        return pct >= KTU_CONDONATION_THRESHOLD && pct < KTU_MIN_THRESHOLD;
    }

    public boolean isDetained() {
        return getPercentage() < KTU_CONDONATION_THRESHOLD;
    }

    public String getStatusBadge() {
        if (isEligible()) {
            return "Eligible (>=75%)";
        } else if (isCondonationRequired()) {
            return "Condonation Required (60-74%)";
        } else {
            return "Detained / Shortage (<60%)";
        }
    }

    public void recordClass(boolean attended) {
        this.totalClasses++;
        if (attended) {
            this.classesAttended++;
        }
    }

    public void updateAttendance(int attended, int total) {
        if (total < 0 || attended < 0 || attended > total) {
            throw new IllegalArgumentException("Invalid attendance update counts.");
        }
        this.classesAttended = attended;
        this.totalClasses = total;
    }

    /**
     * Validates eligibility; throws LowAttendanceException if below 75%.
     */
    public void validateExamEligibility() throws LowAttendanceException {
        if (!isEligible()) {
            throw new LowAttendanceException(course.getCourseCode(), getPercentage());
        }
    }

    @Override
    public String toString() {
        return String.format("%s: %d/%d (%.1f%%) - %s", 
                course.getCourseCode(), classesAttended, totalClasses, getPercentage(), getStatusBadge());
    }
}
