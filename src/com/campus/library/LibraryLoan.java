package com.campus.library;

import com.campus.sports.User;
import java.io.Serializable;
import java.time.LocalDate;

public class LibraryLoan implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final double DAILY_OVERDUE_FINE = 5.0; // Rs. 5.00 per day overdue

    private String loanId;
    private Book book;
    private User borrower;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private double overdueFineCharged;
    private boolean returned;

    public LibraryLoan(String loanId, Book book, User borrower, int loanDays) {
        this.loanId = loanId;
        this.book = book;
        this.borrower = borrower;
        this.issueDate = LocalDate.now();
        this.dueDate = this.issueDate.plusDays(loanDays);
        this.overdueFineCharged = 0.0;
        this.returned = false;
        book.issueTo(borrower);
    }

    public String getLoanId() { return loanId; }
    public Book getBook() { return book; }
    public User getBorrower() { return borrower; }
    public LocalDate getIssueDate() { return issueDate; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public double getOverdueFineCharged() { return overdueFineCharged; }
    public boolean isReturned() { return returned; }

    /**
     * Processes return of the book.
     * If overdue days > 0, calculates fine and automatically adds to borrower's fine balance.
     */
    public double completeReturn(int overdueDays) {
        this.returned = true;
        this.returnDate = LocalDate.now();
        this.book.markReturned();

        if (overdueDays > 0) {
            this.overdueFineCharged = overdueDays * DAILY_OVERDUE_FINE;
            this.borrower.addFine(this.overdueFineCharged);
        } else {
            this.overdueFineCharged = 0.0;
        }
        return this.overdueFineCharged;
    }
}
