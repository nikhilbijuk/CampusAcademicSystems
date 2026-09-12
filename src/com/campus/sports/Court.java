package com.campus.sports;

import com.campus.exceptions.BookingQuotaExceededException;
import com.campus.exceptions.OutstandingFineException;
import com.campus.exceptions.SlotAlreadyBookedException;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Court implements Reservable, Serializable {
    private static final long serialVersionUID = 1L;

    private String courtId;
    private String courtType;
    private Map<String, User> slotReservations = new HashMap<>();

    public Court(String courtId, String courtType) {
        this.courtId = courtId;
        this.courtType = courtType;
    }

    public String getCourtId() { return courtId; }
    public String getCourtType() { return courtType; }
    public Map<String, User> getSlotReservations() { return Collections.unmodifiableMap(slotReservations); }

    public void assignReservation(String slot, User user) {
        slotReservations.put(slot, user);
    }

    @Override
    public boolean checkAvailability(String slot) {
        return !slotReservations.containsKey(slot);
    }

    @Override
    public boolean reserve(String slot) {
        if (checkAvailability(slot)) {
            slotReservations.put(slot, null);
            return true;
        }
        return false;
    }

    public boolean reserve(String slot, User user, List<Court> allCourts) 
            throws SlotAlreadyBookedException, OutstandingFineException, BookingQuotaExceededException {
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

        slotReservations.put(slot, user);
        return true;
    }

    @Override
    public void release(String slot) {
        slotReservations.remove(slot);
    }

    public boolean release(String slot, User user) {
        if (slotReservations.containsKey(slot)) {
            User reservedUser = slotReservations.get(slot);
            if (reservedUser == null || reservedUser.getUserId().equalsIgnoreCase(user.getUserId())) {
                slotReservations.remove(slot);
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
