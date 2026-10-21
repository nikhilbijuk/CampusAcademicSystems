package com.campus.db;

import java.sql.*;

public class DumpDb {
    public static void main(String[] args) {
        String url = "jdbc:sqlite:data/campus.db";
        try (Connection conn = DriverManager.getConnection(url)) {
            System.out.println("=== TABLES IN campus.db ===");
            DatabaseMetaData md = conn.getMetaData();
            try (ResultSet rs = md.getTables(null, null, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    System.out.println("Table: " + rs.getString("TABLE_NAME"));
                }
            }

            System.out.println("\n=== court_reservations ROWS ===");
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT * FROM court_reservations")) {
                ResultSetMetaData rmd = rs.getMetaData();
                int cols = rmd.getColumnCount();
                for (int i = 1; i <= cols; i++) {
                    System.out.print(rmd.getColumnName(i) + "\t");
                }
                System.out.println();
                while (rs.next()) {
                    for (int i = 1; i <= cols; i++) {
                        System.out.print(rs.getString(i) + "\t");
                    }
                    System.out.println();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
