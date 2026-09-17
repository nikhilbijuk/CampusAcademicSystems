package com.campus.web;

import com.campus.academic.*;
import com.campus.exceptions.*;
import com.campus.hostel.*;
import com.campus.library.*;
import com.campus.sports.*;
import com.campus.storage.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class CampusWebServer {
    private static final String DEFAULT_STORAGE_PATH = "data/campus_data.ser";
    private static final String STATIC_DIR = "web";

    private final int port;
    private final String storagePath;
    private final CampusData data;
    private HttpServer server;

    public CampusWebServer(int port, String storagePath) {
        this.port = port;
        this.storagePath = storagePath;
        this.data = CampusStorageManager.loadData(storagePath);
    }

    public CampusWebServer(int port) {
        this(port, DEFAULT_STORAGE_PATH);
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        // API Endpoints
        server.createContext("/api/data", new ApiDataHandler());
        server.createContext("/api/reserve", new ApiReserveHandler());
        server.createContext("/api/release", new ApiReleaseHandler());
        server.createContext("/api/fine", new ApiFineHandler());
        server.createContext("/api/hostel/register", new ApiHostelRegisterHandler());
        server.createContext("/api/hostel/leave", new ApiHostelLeaveHandler());
        server.createContext("/api/hostel/bill", new ApiHostelBillHandler());
        server.createContext("/api/library/borrow", new ApiLibraryBorrowHandler());
        server.createContext("/api/library/return", new ApiLibraryReturnHandler());
        server.createContext("/api/academic", new ApiAcademicHandler());
        server.createContext("/api/auth/google", new ApiGoogleAuthHandler());
        server.createContext("/api/save", new ApiSaveHandler());

        // Static Web UI Files
        server.createContext("/", new StaticFileHandler());

        server.setExecutor(null); // default executor
        server.start();
        System.out.println(">>> Campus Academic Web Server is running at: http://localhost:" + port);
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println(">>> Campus Web Server stopped.");
        }
    }

    public int getPort() {
        return port;
    }

    // ==========================================
    // Handlers
    // ==========================================

    private class ApiDataHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}", "application/json");
                return;
            }

            synchronized (data) {
                StringBuilder json = new StringBuilder("{");

                // Users
                json.append("\"users\":[");
                List<User> users = data.getUsers();
                for (int i = 0; i < users.size(); i++) {
                    User u = users.get(i);
                    if (i > 0) json.append(",");
                    json.append("{")
                        .append("\"userId\":\"").append(JsonUtils.escapeJson(u.getUserId())).append("\",")
                        .append("\"name\":\"").append(JsonUtils.escapeJson(u.getName())).append("\",")
                        .append("\"role\":\"").append(u.getClass().getSimpleName()).append("\",")
                        .append("\"bookingLimit\":").append(u.getBookingLimit()).append(",")
                        .append("\"fineBalance\":").append(u.getFineBalance())
                        .append("}");
                }
                json.append("],");

                // Courts
                json.append("\"courts\":[");
                List<Court> courts = data.getCourts();
                for (int i = 0; i < courts.size(); i++) {
                    Court c = courts.get(i);
                    if (i > 0) json.append(",");
                    json.append("{")
                        .append("\"courtId\":\"").append(JsonUtils.escapeJson(c.getCourtId())).append("\",")
                        .append("\"courtType\":\"").append(JsonUtils.escapeJson(c.getCourtType())).append("\",")
                        .append("\"reservations\":{");

                    Map<String, User> res = c.getSlotReservations();
                    int slotIdx = 0;
                    for (Map.Entry<String, User> entry : res.entrySet()) {
                        if (slotIdx++ > 0) json.append(",");
                        json.append("\"").append(JsonUtils.escapeJson(entry.getKey())).append("\":{");
                        User bookedUser = entry.getValue();
                        if (bookedUser != null) {
                            json.append("\"userId\":\"").append(JsonUtils.escapeJson(bookedUser.getUserId())).append("\",")
                                .append("\"name\":\"").append(JsonUtils.escapeJson(bookedUser.getName())).append("\"");
                        } else {
                            json.append("\"userId\":null,\"name\":\"Reserved\"");
                        }
                        json.append("}");
                    }
                    json.append("}}");
                }
                json.append("],");

                // Hostel Students
                json.append("\"hostelStudents\":[");
                List<HostelStudent> students = data.getHostelStudents();
                for (int i = 0; i < students.size(); i++) {
                    HostelStudent hs = students.get(i);
                    if (i > 0) json.append(",");
                    json.append("{")
                        .append("\"rollNo\":\"").append(JsonUtils.escapeJson(hs.getRollNo())).append("\",")
                        .append("\"name\":\"").append(JsonUtils.escapeJson(hs.getName())).append("\",")
                        .append("\"roomNumber\":").append(hs.getAssignedRoom().getRoomNumber()).append(",")
                        .append("\"roomType\":\"").append(hs.getAssignedRoom().getClass().getSimpleName()).append("\",")
                        .append("\"roomTariff\":").append(hs.getAssignedRoom().calculateMonthlyTariff()).append(",")
                        .append("\"mealPlan\":\"").append(hs.getChosenMealPlan().getClass().getSimpleName()).append("\",")
                        .append("\"leavesThisMonth\":").append(hs.getLeavesThisMonth())
                        .append("}");
                }
                json.append("],");

                // Library Books
                json.append("\"books\":[");
                List<Book> books = data.getBooks();
                if (books != null) {
                    for (int i = 0; i < books.size(); i++) {
                        Book b = books.get(i);
                        if (i > 0) json.append(",");
                        json.append("{")
                            .append("\"isbn\":\"").append(JsonUtils.escapeJson(b.getIsbn())).append("\",")
                            .append("\"title\":\"").append(JsonUtils.escapeJson(b.getTitle())).append("\",")
                            .append("\"author\":\"").append(JsonUtils.escapeJson(b.getAuthor())).append("\",")
                            .append("\"category\":\"").append(JsonUtils.escapeJson(b.getCategory())).append("\",")
                            .append("\"available\":").append(b.isAvailable()).append(",");
                        if (b.getCurrentBorrower() != null) {
                            json.append("\"borrowerId\":\"").append(JsonUtils.escapeJson(b.getCurrentBorrower().getUserId())).append("\",")
                                .append("\"borrowerName\":\"").append(JsonUtils.escapeJson(b.getCurrentBorrower().getName())).append("\"");
                        } else {
                            json.append("\"borrowerId\":null,\"borrowerName\":null");
                        }
                        json.append("}");
                    }
                }
                json.append("]}");

                sendResponse(exchange, 200, json.toString(), "application/json");
            }
        }
    }

    private class ApiReserveHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}", "application/json");
                return;
            }

            String body = readRequestBody(exchange);
            Map<String, String> params = JsonUtils.parseFlatJson(body);

            String courtId = params.get("courtId");
            String slot = params.get("slot");
            String userId = params.get("userId");

            if (courtId == null || slot == null || userId == null) {
                sendResponse(exchange, 400, "{\"success\":false,\"error\":\"courtId, slot, and userId are required\"}", "application/json");
                return;
            }

            synchronized (data) {
                Court targetCourt = null;
                for (Court c : data.getCourts()) {
                    if (c.getCourtId().equalsIgnoreCase(courtId)) {
                        targetCourt = c;
                        break;
                    }
                }

                User targetUser = null;
                for (User u : data.getUsers()) {
                    if (u.getUserId().equalsIgnoreCase(userId)) {
                        targetUser = u;
                        break;
                    }
                }

                if (targetCourt == null) {
                    sendResponse(exchange, 404, "{\"success\":false,\"error\":\"Court not found: " + courtId + "\"}", "application/json");
                    return;
                }
                if (targetUser == null) {
                    sendResponse(exchange, 404, "{\"success\":false,\"error\":\"User not found: " + userId + "\"}", "application/json");
                    return;
                }

                try {
                    targetCourt.reserve(slot, targetUser, data.getCourts());
                    sendResponse(exchange, 200, "{\"success\":true,\"message\":\"Slot '" + slot + "' reserved successfully for " + targetUser.getName() + "\"}", "application/json");
                } catch (SlotAlreadyBookedException e) {
                    sendResponse(exchange, 409, "{\"success\":false,\"error\":\"" + JsonUtils.escapeJson(e.getMessage()) + "\"}", "application/json");
                } catch (OutstandingFineException e) {
                    sendResponse(exchange, 403, "{\"success\":false,\"error\":\"" + JsonUtils.escapeJson(e.getMessage()) + "\"}", "application/json");
                } catch (BookingQuotaExceededException e) {
                    sendResponse(exchange, 429, "{\"success\":false,\"error\":\"" + JsonUtils.escapeJson(e.getMessage()) + "\"}", "application/json");
                }
            }
        }
    }

    private class ApiReleaseHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}", "application/json");
                return;
            }

            String body = readRequestBody(exchange);
            Map<String, String> params = JsonUtils.parseFlatJson(body);

            String courtId = params.get("courtId");
            String slot = params.get("slot");
            String userId = params.get("userId"); // "admin" or userId

            if (courtId == null || slot == null) {
                sendResponse(exchange, 400, "{\"success\":false,\"error\":\"courtId and slot are required\"}", "application/json");
                return;
            }

            synchronized (data) {
                Court targetCourt = null;
                for (Court c : data.getCourts()) {
                    if (c.getCourtId().equalsIgnoreCase(courtId)) {
                        targetCourt = c;
                        break;
                    }
                }

                if (targetCourt == null) {
                    sendResponse(exchange, 404, "{\"success\":false,\"error\":\"Court not found\"}", "application/json");
                    return;
                }

                if (targetCourt.checkAvailability(slot)) {
                    sendResponse(exchange, 400, "{\"success\":false,\"error\":\"Slot '" + slot + "' is not currently booked\"}", "application/json");
                    return;
                }

                // If admin or no userId specified, force release
                if (userId == null || userId.equalsIgnoreCase("admin") || userId.equalsIgnoreCase("0")) {
                    targetCourt.release(slot);
                    sendResponse(exchange, 200, "{\"success\":true,\"message\":\"Slot released successfully (Admin override)\"}", "application/json");
                    return;
                }

                User authUser = null;
                for (User u : data.getUsers()) {
                    if (u.getUserId().equalsIgnoreCase(userId)) {
                        authUser = u;
                        break;
                    }
                }

                if (authUser == null) {
                    sendResponse(exchange, 404, "{\"success\":false,\"error\":\"Authorizing user not found\"}", "application/json");
                    return;
                }

                boolean released = targetCourt.release(slot, authUser);
                if (released) {
                    sendResponse(exchange, 200, "{\"success\":true,\"message\":\"Slot released successfully by " + authUser.getName() + "\"}", "application/json");
                } else {
                    sendResponse(exchange, 403, "{\"success\":false,\"error\":\"Cancellation denied: Slot was not booked by " + authUser.getName() + "\"}", "application/json");
                }
            }
        }
    }

    private class ApiFineHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}", "application/json");
                return;
            }

            String body = readRequestBody(exchange);
            Map<String, String> params = JsonUtils.parseFlatJson(body);

            String userId = params.get("userId");
            String action = params.get("action"); // "add" or "pay"
            String amountStr = params.get("amount");

            if (userId == null || action == null || amountStr == null) {
                sendResponse(exchange, 400, "{\"success\":false,\"error\":\"userId, action, and amount are required\"}", "application/json");
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    sendResponse(exchange, 400, "{\"success\":false,\"error\":\"Amount must be positive\"}", "application/json");
                    return;
                }
            } catch (NumberFormatException e) {
                sendResponse(exchange, 400, "{\"success\":false,\"error\":\"Invalid amount format\"}", "application/json");
                return;
            }

            synchronized (data) {
                User user = null;
                for (User u : data.getUsers()) {
                    if (u.getUserId().equalsIgnoreCase(userId)) {
                        user = u;
                        break;
                    }
                }

                if (user == null) {
                    sendResponse(exchange, 404, "{\"success\":false,\"error\":\"User not found\"}", "application/json");
                    return;
                }

                if ("add".equalsIgnoreCase(action)) {
                    user.addFine(amount);
                } else if ("pay".equalsIgnoreCase(action)) {
                    user.payFine(amount);
                } else {
                    sendResponse(exchange, 400, "{\"success\":false,\"error\":\"Action must be 'add' or 'pay'\"}", "application/json");
                    return;
                }

                sendResponse(exchange, 200, "{\"success\":true,\"userId\":\"" + user.getUserId() + "\",\"name\":\"" + user.getName() + "\",\"fineBalance\":" + user.getFineBalance() + "}", "application/json");
            }
        }
    }

    private class ApiHostelRegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}", "application/json");
                return;
            }

            String body = readRequestBody(exchange);
            Map<String, String> params = JsonUtils.parseFlatJson(body);

            String rollNo = params.get("rollNo");
            String name = params.get("name");
            String roomType = params.get("roomType"); // "ACSuite" or "SingleOccupancy"
            String roomNumberStr = params.get("roomNumber");
            String baseRateStr = params.get("baseRate");
            String mealPlanType = params.get("mealPlan"); // "SpecialDietPlan" or "StandardPlan"

            if (rollNo == null || name == null || roomNumberStr == null) {
                sendResponse(exchange, 400, "{\"success\":false,\"error\":\"rollNo, name, and roomNumber are required\"}", "application/json");
                return;
            }

            int roomNumber;
            double baseRate = 4500.0;
            try {
                roomNumber = Integer.parseInt(roomNumberStr);
                if (baseRateStr != null && !baseRateStr.isEmpty()) {
                    baseRate = Double.parseDouble(baseRateStr);
                }
            } catch (NumberFormatException e) {
                sendResponse(exchange, 400, "{\"success\":false,\"error\":\"Invalid room number or rate\"}", "application/json");
                return;
            }

            BaseRoom room = "ACSuite".equalsIgnoreCase(roomType)
                    ? new ACSuite(roomNumber, baseRate)
                    : new SingleOccupancy(roomNumber, baseRate);

            MealPlan plan = "SpecialDietPlan".equalsIgnoreCase(mealPlanType)
                    ? new SpecialDietPlan()
                    : new StandardPlan();

            synchronized (data) {
                for (HostelStudent s : data.getHostelStudents()) {
                    if (s.getRollNo().equalsIgnoreCase(rollNo)) {
                        sendResponse(exchange, 409, "{\"success\":false,\"error\":\"Student with roll " + rollNo + " is already registered\"}", "application/json");
                        return;
                    }
                }

                HostelStudent newStudent = new HostelStudent(rollNo, name, room, plan);
                data.getHostelStudents().add(newStudent);
                sendResponse(exchange, 200, "{\"success\":true,\"message\":\"Hostel student " + name + " registered successfully!\"}", "application/json");
            }
        }
    }

    private class ApiHostelLeaveHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}", "application/json");
                return;
            }

            String body = readRequestBody(exchange);
            Map<String, String> params = JsonUtils.parseFlatJson(body);

            String rollNo = params.get("rollNo");
            String daysStr = params.get("days");
            String monthDaysStr = params.get("monthDays");

            if (rollNo == null || daysStr == null) {
                sendResponse(exchange, 400, "{\"success\":false,\"error\":\"rollNo and days are required\"}", "application/json");
                return;
            }

            int days;
            int monthDays = 30;
            try {
                days = Integer.parseInt(daysStr);
                if (monthDaysStr != null && !monthDaysStr.isEmpty()) {
                    monthDays = Integer.parseInt(monthDaysStr);
                }
            } catch (NumberFormatException e) {
                sendResponse(exchange, 400, "{\"success\":false,\"error\":\"Invalid integer for days\"}", "application/json");
                return;
            }

            synchronized (data) {
                HostelStudent student = null;
                for (HostelStudent hs : data.getHostelStudents()) {
                    if (hs.getRollNo().equalsIgnoreCase(rollNo)) {
                        student = hs;
                        break;
                    }
                }

                if (student == null) {
                    sendResponse(exchange, 404, "{\"success\":false,\"error\":\"Student not found\"}", "application/json");
                    return;
                }

                try {
                    student.applyLeave(days, monthDays);
                    sendResponse(exchange, 200, "{\"success\":true,\"rollNo\":\"" + student.getRollNo() + "\",\"leavesThisMonth\":" + student.getLeavesThisMonth() + ",\"message\":\"Applied " + days + " days leave successfully\"}", "application/json");
                } catch (InvalidLeaveDaysException e) {
                    sendResponse(exchange, 400, "{\"success\":false,\"error\":\"" + JsonUtils.escapeJson(e.getMessage()) + "\"}", "application/json");
                }
            }
        }
    }

    private class ApiHostelBillHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}", "application/json");
                return;
            }

            String query = exchange.getRequestURI().getQuery();
            Map<String, String> queryParams = parseQueryParams(query);

            String rollNo = queryParams.get("rollNo");
            String monthDaysStr = queryParams.get("monthDays");

            int monthDays = 30;
            if (monthDaysStr != null && !monthDaysStr.isEmpty()) {
                try {
                    monthDays = Integer.parseInt(monthDaysStr);
                } catch (NumberFormatException ignored) {}
            }

            if (rollNo == null) {
                sendResponse(exchange, 400, "{\"error\":\"rollNo query parameter is required\"}", "application/json");
                return;
            }

            synchronized (data) {
                HostelStudent student = null;
                for (HostelStudent hs : data.getHostelStudents()) {
                    if (hs.getRollNo().equalsIgnoreCase(rollNo)) {
                        student = hs;
                        break;
                    }
                }

                if (student == null) {
                    sendResponse(exchange, 404, "{\"error\":\"Student not found\"}", "application/json");
                    return;
                }

                double roomCost = student.getAssignedRoom().calculateMonthlyTariff();
                double messCost = student.getChosenMealPlan().calculateMealCost(monthDays, student.getLeavesThisMonth());
                double total = roomCost + messCost;

                String json = "{"
                        + "\"rollNo\":\"" + JsonUtils.escapeJson(student.getRollNo()) + "\","
                        + "\"name\":\"" + JsonUtils.escapeJson(student.getName()) + "\","
                        + "\"roomType\":\"" + student.getAssignedRoom().getClass().getSimpleName() + "\","
                        + "\"roomNumber\":" + student.getAssignedRoom().getRoomNumber() + ","
                        + "\"roomCost\":" + String.format(Locale.US, "%.2f", roomCost) + ","
                        + "\"mealPlan\":\"" + student.getChosenMealPlan().getClass().getSimpleName() + "\","
                        + "\"leavesThisMonth\":" + student.getLeavesThisMonth() + ","
                        + "\"monthDays\":" + monthDays + ","
                        + "\"messCost\":" + String.format(Locale.US, "%.2f", messCost) + ","
                        + "\"totalDue\":" + String.format(Locale.US, "%.2f", total) + ","
                        + "\"summary\":\"" + JsonUtils.escapeJson(student.getBillSummary(monthDays).replace("\n", " | ")) + "\""
                        + "}";

                sendResponse(exchange, 200, json, "application/json");
            }
        }
    }

    private class ApiSaveHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}", "application/json");
                return;
            }

            synchronized (data) {
                boolean ok = CampusStorageManager.saveData(data, storagePath);
                if (ok) {
                    sendResponse(exchange, 200, "{\"success\":true,\"message\":\"System state successfully saved to " + storagePath + "\"}", "application/json");
                } else {
                    sendResponse(exchange, 500, "{\"success\":false,\"error\":\"Failed to save campus data\"}", "application/json");
                }
            }
        }
    }

    private class ApiLibraryBorrowHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}", "application/json");
                return;
            }

            String body = readRequestBody(exchange);
            Map<String, String> params = JsonUtils.parseFlatJson(body);

            String isbn = params.get("isbn");
            String userId = params.get("userId");

            if (isbn == null || userId == null) {
                sendResponse(exchange, 400, "{\"success\":false,\"error\":\"isbn and userId are required\"}", "application/json");
                return;
            }

            synchronized (data) {
                Book targetBook = null;
                for (Book b : data.getBooks()) {
                    if (b.getIsbn().equalsIgnoreCase(isbn)) {
                        targetBook = b;
                        break;
                    }
                }

                User targetUser = null;
                for (User u : data.getUsers()) {
                    if (u.getUserId().equalsIgnoreCase(userId)) {
                        targetUser = u;
                        break;
                    }
                }

                if (targetBook == null) {
                    sendResponse(exchange, 404, "{\"success\":false,\"error\":\"Book not found: " + isbn + "\"}", "application/json");
                    return;
                }
                if (targetUser == null) {
                    sendResponse(exchange, 404, "{\"success\":false,\"error\":\"User not found: " + userId + "\"}", "application/json");
                    return;
                }

                if (!targetBook.isAvailable()) {
                    String borrowerName = targetBook.getCurrentBorrower() != null ? targetBook.getCurrentBorrower().getName() : "another user";
                    sendResponse(exchange, 409, "{\"success\":false,\"error\":\"Book '" + JsonUtils.escapeJson(targetBook.getTitle()) + "' is already checked out to " + JsonUtils.escapeJson(borrowerName) + "\"}", "application/json");
                    return;
                }

                // Check unpaid fine restriction
                if (targetUser.getFineBalance() > 0) {
                    sendResponse(exchange, 403, "{\"success\":false,\"error\":\"Library access blocked: " + JsonUtils.escapeJson(targetUser.getName()) + " has outstanding fines of Rs. " + targetUser.getFineBalance() + ". Please clear fines first.\"}", "application/json");
                    return;
                }

                // Check borrowing limit (Student: 3, Faculty: 10, Coach: 5)
                int activeLoans = 0;
                for (LibraryLoan loan : data.getLibraryLoans()) {
                    if (!loan.isReturned() && loan.getBorrower().getUserId().equalsIgnoreCase(userId)) {
                        activeLoans++;
                    }
                }
                int maxBooks = (targetUser instanceof Student) ? 3 : (targetUser instanceof Coach ? 5 : 10);
                if (activeLoans >= maxBooks) {
                    sendResponse(exchange, 429, "{\"success\":false,\"error\":\"Borrowing limit exceeded: " + JsonUtils.escapeJson(targetUser.getName()) + " already has " + activeLoans + " active book loans (Limit: " + maxBooks + ")\"}", "application/json");
                    return;
                }

                // Issue book
                String loanId = "LN" + (data.getLibraryLoans().size() + 101);
                int loanDays = (targetUser instanceof Student) ? 14 : (targetUser instanceof Coach ? 21 : 30);
                LibraryLoan loan = new LibraryLoan(loanId, targetBook, targetUser, loanDays);
                data.getLibraryLoans().add(loan);

                sendResponse(exchange, 200, "{\"success\":true,\"message\":\"Book '" + JsonUtils.escapeJson(targetBook.getTitle()) + "' checked out to " + JsonUtils.escapeJson(targetUser.getName()) + " (Due in " + loanDays + " days)\"}", "application/json");
            }
        }
    }

    private class ApiLibraryReturnHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}", "application/json");
                return;
            }

            String body = readRequestBody(exchange);
            Map<String, String> params = JsonUtils.parseFlatJson(body);

            String isbn = params.get("isbn");
            String overdueDaysStr = params.get("overdueDays");

            int overdueDays = 0;
            if (overdueDaysStr != null && !overdueDaysStr.isEmpty()) {
                try {
                    overdueDays = Math.max(0, Integer.parseInt(overdueDaysStr));
                } catch (NumberFormatException ignored) {}
            }

            if (isbn == null) {
                sendResponse(exchange, 400, "{\"success\":false,\"error\":\"isbn is required\"}", "application/json");
                return;
            }

            synchronized (data) {
                Book targetBook = null;
                for (Book b : data.getBooks()) {
                    if (b.getIsbn().equalsIgnoreCase(isbn)) {
                        targetBook = b;
                        break;
                    }
                }

                if (targetBook == null) {
                    sendResponse(exchange, 404, "{\"success\":false,\"error\":\"Book not found\"}", "application/json");
                    return;
                }

                if (targetBook.isAvailable()) {
                    sendResponse(exchange, 400, "{\"success\":false,\"error\":\"Book is not currently on loan\"}", "application/json");
                    return;
                }

                // Find active loan
                LibraryLoan activeLoan = null;
                for (LibraryLoan loan : data.getLibraryLoans()) {
                    if (!loan.isReturned() && loan.getBook().getIsbn().equalsIgnoreCase(isbn)) {
                        activeLoan = loan;
                        break;
                    }
                }

                if (activeLoan == null) {
                    targetBook.markReturned();
                    sendResponse(exchange, 200, "{\"success\":true,\"message\":\"Book returned successfully\"}", "application/json");
                    return;
                }

                User borrower = activeLoan.getBorrower();
                double fineCharged = activeLoan.completeReturn(overdueDays);

                String msg = "Book '" + targetBook.getTitle() + "' successfully returned by " + borrower.getName() + ".";
                if (fineCharged > 0) {
                    msg += " Overdue penalty of Rs. " + String.format(Locale.US, "%.2f", fineCharged) + " added to " + borrower.getName() + "'s campus account (Total Outstanding Fine: Rs. " + String.format(Locale.US, "%.2f", borrower.getFineBalance()) + ").";
                }

                sendResponse(exchange, 200, "{\"success\":true,\"message\":\"" + JsonUtils.escapeJson(msg) + "\",\"fineCharged\":" + fineCharged + ",\"newFineBalance\":" + borrower.getFineBalance() + "}", "application/json");
            }
        }
    }

    private class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) {
                path = "/index.html";
            }

            File staticBase = new File(STATIC_DIR).getCanonicalFile();
            File file = new File(staticBase, path.substring(1)).getCanonicalFile();

            // Prevent path traversal outside static directory
            if (!file.getPath().startsWith(staticBase.getPath()) || !file.exists() || file.isDirectory()) {
                sendResponse(exchange, 404, "404 Not Found", "text/plain");
                return;
            }

            String mimeType = getMimeType(file.getName());
            byte[] bytes = Files.readAllBytes(file.toPath());
            exchange.getResponseHeaders().set("Content-Type", mimeType + "; charset=utf-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    // ==========================================
    // Utility Methods
    // ==========================================

    private static String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = is.read(buffer)) != -1) {
                baos.write(buffer, 0, read);
            }
            return baos.toString(StandardCharsets.UTF_8.name());
        }
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String response, String contentType) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=utf-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static Map<String, String> parseQueryParams(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            try {
                if (idx > 0) {
                    map.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"),
                            URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
                } else {
                    map.put(URLDecoder.decode(pair, "UTF-8"), "");
                }
            } catch (UnsupportedEncodingException ignored) {}
        }
        return map;
    }

    private class ApiAcademicHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}", "application/json");
                return;
            }

            Map<String, String> query = parseQueryParams(exchange.getRequestURI().getQuery());
            String studentId = query.getOrDefault("studentId", "S101");

            AcademicProfile profile = createDefaultKtuProfile(studentId);

            StringBuilder json = new StringBuilder("{");
            json.append("\"studentId\":\"").append(JsonUtils.escapeJson(profile.getStudentId())).append("\",");
            json.append("\"studentName\":\"").append(JsonUtils.escapeJson(profile.getStudentName())).append("\",");
            json.append("\"sgpa\":").append(String.format(Locale.US, "%.2f", profile.calculateSGPA())).append(",");

            json.append("\"courses\":[");
            List<Course> courses = profile.getEnrolledCourses();
            for (int i = 0; i < courses.size(); i++) {
                Course c = courses.get(i);
                AttendanceRecord att = profile.getAttendance(c.getCourseCode());
                InternalAssessment cie = profile.getInternals(c.getCourseCode());
                KtuGrade grade = profile.getGrade(c.getCourseCode());

                if (i > 0) json.append(",");
                json.append("{")
                    .append("\"code\":\"").append(JsonUtils.escapeJson(c.getCourseCode())).append("\",")
                    .append("\"name\":\"").append(JsonUtils.escapeJson(c.getCourseName())).append("\",")
                    .append("\"credits\":").append(c.getCredits()).append(",")
                    .append("\"attended\":").append(att != null ? att.getClassesAttended() : 0).append(",")
                    .append("\"totalSlots\":").append(att != null ? att.getTotalClasses() : 0).append(",")
                    .append("\"percentage\":").append(att != null ? String.format(Locale.US, "%.1f", att.getPercentage()) : "0.0").append(",")
                    .append("\"isLowAttendance\":").append(att != null ? !att.isEligible() : false).append(",")
                    .append("\"cieTotal\":").append(cie != null ? String.format(Locale.US, "%.1f", cie.calculateTotalCie()) : "0.0").append(",")
                    .append("\"grade\":\"").append(grade != null ? grade.getLabel() : "P").append("\",")
                    .append("\"gradePoints\":").append(grade != null ? grade.getGradePoint() : 0.0)
                    .append("}");
            }
            json.append("]}");

            sendResponse(exchange, 200, json.toString(), "application/json");
        }
    }

    private static AcademicProfile createDefaultKtuProfile(String studentId) {
        String studentName = "S102".equalsIgnoreCase(studentId) ? "Priya Nair" : "Rahul Sharma";
        AcademicProfile profile = new AcademicProfile(studentId, studentName);

        Course mat203 = new Course("MAT203", "Discrete Mathematical Structures", 4, 4);
        Course cst201 = new Course("CST201", "Data Structures", 4, 4);
        Course cst203 = new Course("CST203", "Logic System Design", 4, 4);
        Course cst205 = new Course("CST205", "Object Oriented Programming Using Java", 4, 4);
        Course est200 = new Course("EST200", "Design & Engineering", 2, 2);
        Course mcn201 = new Course("MCN201", "Sustainable Engineering", 0, 2);

        profile.enrollCourse(mat203);
        profile.enrollCourse(cst201);
        profile.enrollCourse(cst203);
        profile.enrollCourse(cst205);
        profile.enrollCourse(est200);
        profile.enrollCourse(mcn201);

        profile.recordAttendance("MAT203", 46, 50);
        profile.recordAttendance("CST201", 44, 50);
        profile.recordAttendance("CST203", 41, 50);
        profile.recordAttendance("CST205", 48, 50);
        profile.recordAttendance("EST200", 18, 25);
        profile.recordAttendance("MCN201", 18, 20);

        try {
            profile.recordInternals("MAT203", 18.0, 19.0, 9.5);
            profile.recordInternals("CST201", 17.0, 18.0, 9.0);
            profile.recordInternals("CST203", 16.0, 17.0, 8.5);
            profile.recordInternals("CST205", 19.5, 20.0, 10.0);
            profile.recordInternals("EST200", 14.0, 15.0, 8.0);
            profile.recordInternals("MCN201", 15.0, 16.0, 8.0);
        } catch (Exception ignored) {}

        profile.recordGrade("MAT203", KtuGrade.A_PLUS);
        profile.recordGrade("CST201", KtuGrade.A);
        profile.recordGrade("CST203", KtuGrade.B_PLUS);
        profile.recordGrade("CST205", KtuGrade.S);
        profile.recordGrade("EST200", KtuGrade.B);
        profile.recordGrade("MCN201", KtuGrade.P);

        return profile;
    }

    private class ApiGoogleAuthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}", "application/json");
                return;
            }

            String body = readRequestBody(exchange);
            Map<String, String> params = JsonUtils.parseFlatJson(body);
            String email = params.get("email");
            String name = params.get("name");

            if (email == null) {
                sendResponse(exchange, 400, "{\"success\":false,\"error\":\"Email is required\"}", "application/json");
                return;
            }

            synchronized (data) {
                User user = null;
                for (User u : data.getUsers()) {
                    if (u.getName().equalsIgnoreCase(name) || u.getUserId().equalsIgnoreCase(email)) {
                        user = u;
                        break;
                    }
                }

                if (user == null) {
                    String newId = "S" + (100 + data.getUsers().size() + 1);
                    user = new Student(newId, name != null ? name : email);
                    data.getUsers().add(user);
                }

                String json = "{"
                    + "\"success\":true,"
                    + "\"userId\":\"" + JsonUtils.escapeJson(user.getUserId()) + "\","
                    + "\"name\":\"" + JsonUtils.escapeJson(user.getName()) + "\","
                    + "\"role\":\"" + user.getClass().getSimpleName() + "\","
                    + "\"fineBalance\":" + user.getFineBalance()
                    + "}";

                sendResponse(exchange, 200, json, "application/json");
            }
        }
    }

    private static String getMimeType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".html")) return "text/html";
        if (lower.endsWith(".css")) return "text/css";
        if (lower.endsWith(".js")) return "application/javascript";
        if (lower.endsWith(".json")) return "application/json";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".ico")) return "image/x-icon";
        if (lower.endsWith(".woff2")) return "font/woff2";
        if (lower.endsWith(".woff")) return "font/woff";
        if (lower.endsWith(".ttf")) return "font/ttf";
        if (lower.endsWith(".webp")) return "image/webp";
        return "application/octet-stream";
    }
}
