package com.campus.hostel;

public class SpecialDietPlan extends MealPlan {
    private static final long serialVersionUID = 1L;
    private double dailyRate = 150.0;
    @Override
    public double calculateMealCost(int totalDays, int leavesTaken) {
        return (totalDays - leavesTaken) * dailyRate + 500.0; // Premium charge
    }
}
