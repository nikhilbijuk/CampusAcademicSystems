package com.campus.academic;

import com.campus.exceptions.InvalidMarkException;
import java.io.Serializable;

/**
 * Calculates and manages KTU Continuous Internal Evaluation (CIE) marks out of 50:
 * - Series Test 1: max 20 marks
 * - Series Test 2: max 20 marks
 * - Assignments / Quizzes: max 10 marks
 */
public class InternalAssessment implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final double MAX_SERIES_TEST = 20.0;
    public static final double MAX_ASSIGNMENTS = 10.0;
    public static final double MAX_TOTAL_CIE = 50.0;

    private final Course course;
    private double seriesTest1;
    private double seriesTest2;
    private double assignments;

    public InternalAssessment(Course course, double seriesTest1, double seriesTest2, double assignments) 
            throws InvalidMarkException {
        if (course == null) {
            throw new IllegalArgumentException("Course cannot be null.");
        }
        this.course = course;
        setScores(seriesTest1, seriesTest2, assignments);
    }

    public Course getCourse() {
        return course;
    }

    public double getSeriesTest1() {
        return seriesTest1;
    }

    public double getSeriesTest2() {
        return seriesTest2;
    }

    public double getAssignments() {
        return assignments;
    }

    public final void setScores(double series1, double series2, double assign) throws InvalidMarkException {
        validateScore("Series Test 1", series1, MAX_SERIES_TEST);
        validateScore("Series Test 2", series2, MAX_SERIES_TEST);
        validateScore("Assignments", assign, MAX_ASSIGNMENTS);

        this.seriesTest1 = series1;
        this.seriesTest2 = series2;
        this.assignments = assign;
    }

    private void validateScore(String component, double score, double max) throws InvalidMarkException {
        if (score < 0.0) {
            throw new InvalidMarkException(String.format("%s mark cannot be negative: %.2f", component, score));
        }
        if (score > max) {
            throw new InvalidMarkException(String.format("%s mark (%.2f) exceeds maximum allowed limit (%.2f)", component, score, max));
        }
    }

    /**
     * Calculates total CIE internal marks (out of 50).
     */
    public double calculateTotalCie() {
        double total = seriesTest1 + seriesTest2 + assignments;
        return Math.min(MAX_TOTAL_CIE, Math.round(total * 100.0) / 100.0);
    }

    /**
     * Checks if the student meets internal minimum requirement (e.g. 45% or 22.5/50).
     */
    public boolean isInternalPass() {
        return calculateTotalCie() >= (MAX_TOTAL_CIE * 0.45);
    }

    @Override
    public String toString() {
        return String.format("%s CIE: S1=%.1f/20, S2=%.1f/20, Assign=%.1f/10 -> Total=%.1f/50 (%s)",
                course.getCourseCode(), seriesTest1, seriesTest2, assignments, calculateTotalCie(),
                isInternalPass() ? "Passed CIE" : "Shortage in CIE");
    }
}
