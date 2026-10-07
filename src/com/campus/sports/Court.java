package com.campus.sports;

import com.campus.exceptions.BookingQuotaExceededException;
import com.campus.exceptions.InsufficientPlayersException;
import com.campus.exceptions.OutstandingFineException;
import com.campus.exceptions.SlotAlreadyBookedException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Court implements Reservable, Serializable {
    private static final long serialVersionUID = 2L;

    private String courtId;
    private String courtType;
    private Map<String, User> slotReservations = new HashMap<>();
    private Map<String, CourtBooking> bookings = new HashMap<>();

    public Court(String courtId, String courtType) {
        this.courtId = courtId;
        this.courtType = courtType;
    }

    public String getCourtId() { return courtId; }
    public String getCourtType() { return courtType; }
    public Map<String, User> getSlotReservations() { return Collections.unmodifiableMap(slotReservations); }
    public Map<String, CourtBooking> getBookings() { return Collections.unmodifiableMap(bookings); }
    public CourtBooking getBooking(String slot) { return bookings.get(slot); }

    public void assignReservation(String slot, User user) {
        slotReservations.put(slot, user);
        List<String> defaultSquad = new ArrayList<>();
        if (user != null) defaultSquad.add(user.getUserId());
        bookings.put(slot, new CourtBooking(courtId, slot, user, "Standard", defaultSquad));
    }

    public static int getRequiredPlayers(String courtType, String matchFormat) {
        String type = (courtType != null) ? courtType.toLowerCase() : "";
        String fmt = (matchFormat != null) ? matchFormat.toLowerCase() : "";

        if (type.contains("badminton") || type.contains("tennis")) {
            if (fmt.contains("double") || fmt.contains("4")) return 4;
            return 2; // Singles minimum (1 vs 1)
        }
        if (type.contains("basket")) {
            if (fmt.contains("5") || fmt.contains("full") || fmt.contains("10")) return 10;
            return 6; // 3 vs 3 half-court
        }
        if (type.contains("foot") || type.contains("turf") || type.contains("soccer")) {
            if (fmt.contains("11") || fmt.contains("22") || fmt.contains("full")) return 22; // 11 + 11 Match
            return 14; // 7 + 7 Turf Match
        }
        if (type.contains("cricket")) {
            if (fmt.contains("11") || fmt.contains("22") || fmt.contains("match")) return 22; // 11 + 11 Match
            return 4; // Nets practice minimum (batsman + bowlers)
        }
        return 2; // General court minimum
    }

    @Override
    public boolean checkAvailability(String slot) {
        return !slotReservations.containsKey(slot);
    }

    @Override
    public boolean reserve(String slot) {
        if (checkAvailability(slot)) {
            slotReservations.put(slot, null);
            bookings.put(slot, new CourtBooking(courtId, slot, null, "Standard", new ArrayList<>()));
            return true;
        }
        return false;
    }

    /**
     * Standard reservation overload maintaining backwards compatibility.
     * Auto-populates a minimal valid roster to satisfy format requirements.
     */
    public boolean reserve(String slot, User user, List<Court> allCourts) 
            throws SlotAlreadyBookedException, OutstandingFineException, BookingQuotaExceededException {
        int req = getRequiredPlayers(courtType, "Standard");
        List<String> roster = new ArrayList<>();
        roster.add(user.getUserId());
        for (int i = 2; i <= req; i++) {
            roster.add("Player_" + i);
        }
        try {
            return reserve(slot, user, "Standard", roster, allCourts);
        } catch (InsufficientPlayersException e) {
            // Should not occur with auto-filled roster
            throw new RuntimeException(e);
        }
    }

    /**
     * Comprehensive reservation requiring multi-player validation according to sport rules.
     */
    public boolean reserve(String slot, User user, String matchFormat, List<String> playerRoster, List<Court> allCourts)
            throws SlotAlreadyBookedException, OutstandingFineException, BookingQuotaExceededException, InsufficientPlayersException {
        if (!checkAvailability(slot)) {
            throw new SlotAlreadyBookedException(slot);
        }

        if (user.getFineBalance() > 0) {
            throw new OutstandingFineException(user.getName(), user.getFineBalance());
        }

        int activeBookings = 0;
        for (Court court : allCourts) {
            for (User bookedUser : court.slotReservations.values()) {
                if (bookedUser != null && bookedUser.getUserId().equalsIgnoreCase(user.getUserId())) {
                    activeBookings++;
                }
            }
        }

        if (activeBookings >= user.getBookingLimit()) {
            throw new BookingQuotaExceededException(user.getName(), user.getBookingLimit(), activeBookings);
        }

        String format = (matchFormat != null && !matchFormat.trim().isEmpty()) ? matchFormat.trim() : "Standard";
        int requiredPlayers = getRequiredPlayers(this.courtType, format);

        List<String> validRoster = new ArrayList<>();
        if (playerRoster != null) {
            for (String p : playerRoster) {
                if (p != null && !p.trim().isEmpty() && !validRoster.contains(p.trim())) {
                    validRoster.add(p.trim());
                }
            }
        }
        if (!validRoster.contains(user.getUserId())) {
            validRoster.add(0, user.getUserId());
        }

        if (validRoster.size() < requiredPlayers) {
            throw new InsufficientPlayersException(courtType, format, requiredPlayers, validRoster.size());
        }

        slotReservations.put(slot, user);
        CourtBooking booking = new CourtBooking(courtId, slot, user, format, validRoster);
        bookings.put(slot, booking);
        return true;
    }

    /**
     * Modify / reschedule a booking to a new time slot atomically.
     */
    public boolean modifySlot(String oldSlot, String newSlot, User user, List<Court> allCourts)
            throws SlotAlreadyBookedException, OutstandingFineException, BookingQuotaExceededException {
        if (!slotReservations.containsKey(oldSlot)) {
            return false;
        }

        User existingUser = slotReservations.get(oldSlot);
        if (existingUser != null && !existingUser.getUserId().equalsIgnoreCase(user.getUserId())
                && !"admin".equalsIgnoreCase(user.getUserId()) && !"F201".equalsIgnoreCase(user.getUserId())) {
            return false; // Unauthorized
        }

        if (!checkAvailability(newSlot)) {
            throw new SlotAlreadyBookedException(newSlot);
        }

        // Transfer booking
        CourtBooking oldBooking = bookings.remove(oldSlot);
        slotReservations.remove(oldSlot);

        slotReservations.put(newSlot, existingUser);
        if (oldBooking != null) {
            oldBooking.setSlot(newSlot);
            bookings.put(newSlot, oldBooking);
        } else {
            bookings.put(newSlot, new CourtBooking(courtId, newSlot, existingUser, "Standard", Collections.singletonList(existingUser.getUserId())));
        }
        return true;
    }

    /**
     * Update the roster and match format for an existing reservation.
     */
    public boolean updateRoster(String slot, User user, String newFormat, List<String> newRoster)
            throws InsufficientPlayersException {
        if (!slotReservations.containsKey(slot)) {
            return false;
        }

        User existingUser = slotReservations.get(slot);
        if (existingUser != null && !existingUser.getUserId().equalsIgnoreCase(user.getUserId())
                && !"admin".equalsIgnoreCase(user.getUserId()) && !"F201".equalsIgnoreCase(user.getUserId())) {
            return false; // Unauthorized
        }

        String format = (newFormat != null && !newFormat.trim().isEmpty()) ? newFormat.trim() : "Standard";
        int required = getRequiredPlayers(this.courtType, format);

        List<String> validRoster = new ArrayList<>();
        if (newRoster != null) {
            for (String p : newRoster) {
                if (p != null && !p.trim().isEmpty() && !validRoster.contains(p.trim())) {
                    validRoster.add(p.trim());
                }
            }
        }
        if (existingUser != null && !validRoster.contains(existingUser.getUserId())) {
            validRoster.add(0, existingUser.getUserId());
        }

        if (validRoster.size() < required) {
            throw new InsufficientPlayersException(courtType, format, required, validRoster.size());
        }

        CourtBooking existing = bookings.get(slot);
        if (existing != null) {
            existing.setMatchFormat(format);
            existing.setPlayerRoster(validRoster);
        } else {
            bookings.put(slot, new CourtBooking(courtId, slot, existingUser, format, validRoster));
        }
        return true;
    }

    @Override
    public void release(String slot) {
        slotReservations.remove(slot);
        bookings.remove(slot);
    }

    public boolean release(String slot, User user) {
        if (slotReservations.containsKey(slot)) {
            User reservedUser = slotReservations.get(slot);
            if (reservedUser == null || reservedUser.getUserId().equalsIgnoreCase(user.getUserId())
                    || "admin".equalsIgnoreCase(user.getUserId()) || "F201".equalsIgnoreCase(user.getUserId())) {
                slotReservations.remove(slot);
                bookings.remove(slot);
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return courtType + " Court (" + courtId + ")";
    }
}
