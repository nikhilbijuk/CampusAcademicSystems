package com.campus.library;

import com.campus.sports.User;
import java.io.Serializable;

public class Book implements Serializable {
    private static final long serialVersionUID = 1L;

    private String isbn;
    private String title;
    private String author;
    private String category;
    private boolean available;
    private User currentBorrower;

    public Book(String isbn, String title, String author, String category) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.category = category;
        this.available = true;
        this.currentBorrower = null;
    }

    public String getIsbn() { return isbn; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getCategory() { return category; }
    public boolean isAvailable() { return available; }
    public User getCurrentBorrower() { return currentBorrower; }

    public void issueTo(User user) {
        this.available = false;
        this.currentBorrower = user;
    }

    public void markReturned() {
        this.available = true;
        this.currentBorrower = null;
    }

    @Override
    public String toString() {
        return title + " by " + author + " [ISBN: " + isbn + "] - " + (available ? "Available" : "Issued to " + (currentBorrower != null ? currentBorrower.getName() : "Unknown"));
    }
}
