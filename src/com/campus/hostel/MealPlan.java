package com.campus.hostel;

import java.io.Serializable;

public abstract class MealPlan implements Serializable {
    private static final long serialVersionUID = 1L;
    public abstract double calculateMealCost(int totalDays, int leavesTaken);
}
