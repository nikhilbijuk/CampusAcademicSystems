package com.campus.storage;

import com.campus.hostel.HostelStudent;
import com.campus.sports.Court;
import com.campus.sports.User;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CampusData implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<User> users = new ArrayList<>();
    private List<Court> courts = new ArrayList<>();
    private List<HostelStudent> hostelStudents = new ArrayList<>();

    public List<User> getUsers() { return users; }
    public List<Court> getCourts() { return courts; }
    public List<HostelStudent> getHostelStudents() { return hostelStudents; }

    public void setUsers(List<User> users) { this.users = users; }
    public void setCourts(List<Court> courts) { this.courts = courts; }
    public void setHostelStudents(List<HostelStudent> hostelStudents) { this.hostelStudents = hostelStudents; }
}
