package com.example.inspirationrewards;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UserInfo implements Serializable, Comparable<UserInfo> {
    private String studentID;
    private String username;
    private String password;
    private String first_name;
    private String last_name;
    private String department;
    private String story;
    private String position;
    private int points_to_award;
    private boolean admin;
    private String location;
    private String image;
    private List<RewardsInfo> rewards = new ArrayList<>();
    private int points;


    public UserInfo(String studentID, String first_name, String last_name, String username,
                    String department, String story, String position, String password,
                    int points_to_award, boolean admin, String location, String image,
                    List<RewardsInfo> rewards) {

        this.username = username;
        this.password = password;
        this.first_name = first_name;
        this.last_name = last_name;
        this.department = department;
        this.story = story;
        this.position = position;
        this.points_to_award = points_to_award;
        this.admin = admin;
        this.location = location;
        this.image = image;

        if(!studentID.equalsIgnoreCase("A20392360"))
            this.studentID = "A20392360";
        else
            this.studentID = studentID;


        if(rewards == null || rewards.size() == 0 || rewards.isEmpty()) {

        } else {
            this.rewards = rewards;
        }
        setPoints();

    }
    @Override
    public int compareTo(UserInfo user) {

        if (this.points > user.points)
            return -1;
        if (this.points < user.points)
            return 1;
        else
            return 0;
    }

    public int getPoints() {
        int points = 0;
        for(RewardsInfo r : this.rewards){
            points += Integer.parseInt(r.getPoints());
        }
        return points;
    }

    public void setPoints() {
        int points = 0;
        for(RewardsInfo r : this.rewards){
            points += Integer.parseInt(r.getPoints());
        }
        this.points = points;
    }


    public String getStudentID() {
        return studentID;
    }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getFirst_name() { return first_name; }
    public String getLast_name() {return last_name;}
    public String getDepartment() {
        return department;
    }
    public String getStory() {
        return story;
    }
    public String getPosition() {
        return position;
    }
    public int getPoints_to_award() {
        return points_to_award;
    }
    public boolean getAdmin() {
        return admin;
    }
    public String getLocation() {
        return location;
    }
    public String getImage() { return image; }
    public List<RewardsInfo> getRewards() {
        return rewards;
    }


    public void setStudentID(String studentID) {
        this.studentID = studentID;
    }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setFirst_name(String first_name) {this.first_name = first_name; }
    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }
    public void setDepartment(String department) {
        this.department = department;
    }
    public void setStory(String story) {
        this.story = story;
    }
    public void setPosition(String position) {
        this.position = position;
    }
    public void setPoints_to_award(int points_to_award) {
        this.points_to_award = points_to_award;
    }
    public void setAdmin(boolean admin) {
        this.admin = admin;
    }
    public void setLocation(String location) {
        this.location = location;
    }
    public void setImage(String image) {
        this.image = image;
    }
    public void setRewards(List<RewardsInfo> rewards) {
        this.rewards = rewards;
    }

}