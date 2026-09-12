package com.campus.academic;

/**
 * Enum representing KTU Letter Grades and corresponding Grade Points
 * on the KTU 10-Point Absolute Grading Scale.
 */
public enum KtuGrade {
    S("S", 10.0, "Outstanding"),
    A_PLUS("A+", 9.0, "Excellent"),
    A("A", 8.5, "Very Good"),
    B_PLUS("B+", 8.0, "Good"),
    B("B", 7.5, "Above Average"),
    C_PLUS("C+", 7.0, "Average"),
    C("C", 6.5, "Pass"),
    D("D", 6.0, "Below Average"),
    P("P", 5.5, "Marginal Pass"),
    F("F", 0.0, "Failed"),
    FE("FE", 0.0, "Failed (Eligibility / Shortage)");

    private final String label;
    private final double gradePoint;
    private final String description;

    KtuGrade(String label, double gradePoint, String description) {
        this.label = label;
        this.gradePoint = gradePoint;
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public double getGradePoint() {
        return gradePoint;
    }

    public String getDescription() {
        return description;
    }

    public boolean isPass() {
        return this != F && this != FE;
    }

    /**
     * Derives KTU Grade from total percentage marks (CIE + ESE) out of 100,
     * taking attendance eligibility into account.
     */
    public static KtuGrade fromMarks(double marks, boolean isAttendanceEligible) {
        if (!isAttendanceEligible) {
            return FE;
        }
        if (marks >= 90.0) return S;
        if (marks >= 85.0) return A_PLUS;
        if (marks >= 80.0) return A;
        if (marks >= 75.0) return B_PLUS;
        if (marks >= 70.0) return B;
        if (marks >= 65.0) return C_PLUS;
        if (marks >= 60.0) return C;
        if (marks >= 55.0) return D;
        if (marks >= 45.0) return P;
        return F;
    }

    /**
     * Parses grade string safely.
     */
    public static KtuGrade fromString(String str) {
        if (str == null) return F;
        String clean = str.trim().toUpperCase().replace("+", "_PLUS");
        for (KtuGrade g : values()) {
            if (g.name().equalsIgnoreCase(clean) || g.label.equalsIgnoreCase(str.trim())) {
                return g;
            }
        }
        return F;
    }

    @Override
    public String toString() {
        return String.format("%s (%.1f)", label, gradePoint);
    }
}
