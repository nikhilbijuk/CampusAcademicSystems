package com.campus.sports;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CourtBooking implements Serializable {
    private static final long serialVersionUID = 1L;

    private String courtId;
    private String slot;
    private User captain;
    private String matchFormat;
    private List<String> playerRoster;
    private long bookedAtMillis;

    public CourtBooking(String courtId, String slot, User captain, String matchFormat, List<String> playerRoster) {
        this.courtId = courtId;
        this.slot = slot;
        this.captain = captain;
        this.matchFormat = (matchFormat != null && !matchFormat.trim().isEmpty()) ? matchFormat.trim() : "Standard";
        this.playerRoster = new ArrayList<>();
        if (playerRoster != null) {
            for (String p : playerRoster) {
                if (p != null && !p.trim().isEmpty()) {
                    this.playerRoster.add(p.trim());
                }
            }
        }
        if (captain != null && !this.playerRoster.contains(captain.getUserId())) {
            this.playerRoster.add(0, captain.getUserId());
        }
        this.bookedAtMillis = System.currentTimeMillis();
    }

    public String getCourtId() { return courtId; }
    public String getSlot() { return slot; }
    public void setSlot(String slot) { this.slot = slot; }

    public User getCaptain() { return captain; }
    public String getMatchFormat() { return matchFormat; }
    public void setMatchFormat(String matchFormat) { this.matchFormat = matchFormat; }

    public List<String> getPlayerRoster() { return Collections.unmodifiableList(playerRoster); }
    public void setPlayerRoster(List<String> playerRoster) {
        this.playerRoster = new ArrayList<>();
        if (playerRoster != null) {
            for (String p : playerRoster) {
                if (p != null && !p.trim().isEmpty()) {
                    this.playerRoster.add(p.trim());
                }
            }
        }
        if (captain != null && !this.playerRoster.contains(captain.getUserId())) {
            this.playerRoster.add(0, captain.getUserId());
        }
    }

    public int getPlayerCount() { return playerRoster.size(); }
    public long getBookedAtMillis() { return bookedAtMillis; }

    @Override
    public String toString() {
        return "Booking[" + courtId + "@" + slot + " | Format: " + matchFormat + " | Players: " + playerRoster.size() + "]";
    }
}
