package com.campus.db;

public class DbSetup {
    public static void main(String[] args) {
        System.out.println("Initializing SQLite Database...");
        DatabaseManager db = DatabaseManager.getInstance();
        CampusDao dao = new CampusDao();

        try {
            dao.reserveCourt("CRT1", "06:00-07:00", "S101");
            System.out.println("Court booking created: CRT1 @ 06:00-07:00 by S101");
        } catch (Exception e) {
            System.out.println("Booking notice: " + e.getMessage());
        }

        System.out.println("SQLite database successfully created at data/campus.db");
    }
}
