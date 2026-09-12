package com.campus.academic;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a KTU University Course/Subject with credits and course code.
 */
public class Course implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String courseCode;
    private final String courseName;
    private final int credits;
    private final int semester;

    public Course(String courseCode, String courseName, int credits, int semester) {
        if (courseCode == null || courseCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Course code cannot be null or empty.");
        }
        if (credits < 0) {
            throw new IllegalArgumentException("Course credits cannot be negative.");
        }
        this.courseCode = courseCode.trim().toUpperCase();
        this.courseName = courseName != null ? courseName.trim() : "";
        this.credits = credits;
        this.semester = semester;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public int getCredits() {
        return credits;
    }

    public int getSemester() {
        return semester;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Course course = (Course) o;
        return courseCode.equalsIgnoreCase(course.courseCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseCode.toUpperCase());
    }

    @Override
    public String toString() {
        return String.format("%s - %s (%d Credits, Sem %d)", courseCode, courseName, credits, semester);
    }
}
