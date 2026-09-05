package com.campus.hostel;

import com.campus.exceptions.InvalidLeaveDaysException;
import java.io.Serializable;

public class HostelStudent implements Serializable {
    private static final long serialVersionUID = 1L;
    private String rollNo;
    private String name;
    private BaseRoom assignedRoom;
    private MealPlan chosenMealPlan;
    private int leavesThisMonth;

    public HostelStudent(String rollNo, String name, BaseRoom room, MealPlan plan) {
        this.rollNo = rollNo;
        this.name = name;
        this.assignedRoom = room;
        this.chosenMealPlan = plan;
        this.leavesThisMonth = 0;
    }

    public String getRollNo() { return rollNo; }
    public String getName() { return name; }
    public BaseRoom getAssignedRoom() { return assignedRoom; }
    public MealPlan getChosenMealPlan() { return chosenMealPlan; }
    public int getLeavesThisMonth() { return leavesThisMonth; }

    public void setAssignedRoom(BaseRoom room) { this.assignedRoom = room; }
    public void setChosenMealPlan(MealPlan plan) { this.chosenMealPlan = plan; }
    public void resetLeaves() { this.leavesThisMonth = 0; }

    public void applyLeave(int days) {
        this.leavesThisMonth += days;
    }

    public void applyLeave(int days, int monthDays) throws InvalidLeaveDaysException {
        if (days < 0) {
            throw new InvalidLeaveDaysException("Leave days cannot be negative (" + days + ").");
        }
        if (this.leavesThisMonth + days > monthDays) {
            throw new InvalidLeaveDaysException("Total leaves (" + (this.leavesThisMonth + days) + ") cannot exceed month days (" + monthDays + ").");
        }
        this.leavesThisMonth += days;
    }

    public String getBillSummary(int monthDays) {
        double roomCost = assignedRoom.calculateMonthlyTariff();
        double messCost = chosenMealPlan.calculateMealCost(monthDays, leavesThisMonth);
        double total = roomCost + messCost;

        StringBuilder sb = new StringBuilder();
        sb.append("\n--- Itemized Bill for ").append(name).append(" (").append(rollNo).append(") ---\n");
        sb.append("Room Type: ").append(assignedRoom.getClass().getSimpleName())
          .append(" (Room ").append(assignedRoom.getRoomNumber()).append(")\n");
        sb.append("Room Charges: Rs. ").append(String.format("%.2f", roomCost)).append("\n");
        sb.append("Mess Plan: ").append(chosenMealPlan.getClass().getSimpleName()).append("\n");
        sb.append("Mess Charges (").append(leavesThisMonth).append(" days leave deducted): Rs. ")
          .append(String.format("%.2f", messCost)).append("\n");
        sb.append("Total Amount Due: Rs. ").append(String.format("%.2f", total)).append("\n");
        return sb.toString();
    }

    public void generateBill(int monthDays) {
        System.out.print(getBillSummary(monthDays));
    }
}
