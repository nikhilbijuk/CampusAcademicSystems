package com.campus.storage;

import com.campus.hostel.*;
import com.campus.library.*;
import com.campus.sports.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CampusStorageManager {

    public static boolean saveData(CampusData data, String filePath) {
        File file = new File(filePath);
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(data);
            return true;
        } catch (IOException e) {
            System.err.println("Error saving campus data: " + e.getMessage());
            return false;
        }
    }

    public static CampusData loadData(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return seedInitialData();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            CampusData data = (CampusData) ois.readObject();
            if (data.getBooks() == null) data.setBooks(new ArrayList<>());
            if (data.getLibraryLoans() == null) data.setLibraryLoans(new ArrayList<>());
            if (data.getBooks().isEmpty()) {
                seedBooks(data);
            }
            return data;
        } catch (Exception e) {
            System.err.println("Notice: Could not load saved state (" + e.getMessage() + "). Seeding fresh data.");
            return seedInitialData();
        }
    }

    public static CampusData seedInitialData() {
        CampusData data = new CampusData();

        // Sample Users
        List<User> users = new ArrayList<>();
        users.add(new Student("S101", "Rahul Sharma"));
        users.add(new Student("S102", "Priya Nair"));
        users.add(new Faculty("F201", "Dr. Suresh Kumar"));
        users.add(new Coach("C301", "Coach Vikram"));
        data.setUsers(users);

        // Sample Courts
        List<Court> courts = new ArrayList<>();
        courts.add(new Court("CRT1", "Badminton"));
        courts.add(new Court("CRT2", "Tennis"));
        courts.add(new Court("CRT3", "Basketball"));
        data.setCourts(courts);

        // Sample Hostel Students
        List<HostelStudent> hostelStudents = new ArrayList<>();
        hostelStudents.add(new HostelStudent("STU202", "Anjali Menon", new SingleOccupancy(302, 4500.0), new StandardPlan()));
        hostelStudents.add(new HostelStudent("STU203", "Rohan Das", new ACSuite(101, 6000.0), new SpecialDietPlan()));
        data.setHostelStudents(hostelStudents);

        // Sample Library Books
        seedBooks(data);

        return data;
    }

    public static void seedBooks(CampusData data) {
        List<Book> books = new ArrayList<>();
        books.add(new Book("978-0262033848", "Introduction to Algorithms (CLRS)", "Thomas H. Cormen", "Computer Science"));
        books.add(new Book("978-1118063330", "Operating System Concepts", "Abraham Silberschatz", "Systems"));
        books.add(new Book("978-0078022159", "Database System Concepts", "Henry F. Korth", "Databases"));
        books.add(new Book("978-0132126953", "Computer Networks", "Andrew S. Tanenbaum", "Networks"));
        books.add(new Book("978-0132350884", "Clean Code: A Handbook of Agile Software Craftsmanship", "Robert C. Martin", "Software Engineering"));
        data.setBooks(books);
    }
}
