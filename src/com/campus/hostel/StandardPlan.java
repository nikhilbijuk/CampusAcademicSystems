package com.campus.hostel;

public class StandardPlan extends MealPlan {
    private static final long serialVersionUID = 1L;
    private double dailyRate = 120.0;
    @Override
    public double calculateMealCost(int totalDays, int leavesTaken) {
        return (totalDays - leavesTaken) * dailyRate;
    }
}
