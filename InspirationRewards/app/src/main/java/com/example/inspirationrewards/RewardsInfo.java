package com.example.inspirationrewards;


import java.io.Serializable;

public class RewardsInfo implements Serializable {
    private String studentID;
    private String UserName;
    private String reward_sender;
    private String points;
    private String date;
    private String comments;

    public RewardsInfo(String studentID, String userName, String reward_sender,
                       String points, String date, String comments) {
        this.studentID = studentID;
        UserName = userName;
        this.reward_sender = reward_sender;
        this.points = points;
        this.date = date;
        this.comments = comments;
    }


    public String getStudentID() {
        return studentID;
    }
    public String getUserName() {
        return UserName;
    }
    public String getReward_sender() {
        return reward_sender;
    }
    public String getPoints() {
        return points;
    }
    public String getDate() {
        return date;
    }
    public String getComments() {
        return comments;
    }

    public void setStudentID(String studentID) {
        this.studentID = studentID;
    }
    public void setUserName(String userName) {
        UserName = userName;
    }
    public void setReward_sender(String reward_sender) {
        this.reward_sender = reward_sender;
    }
    public void setPoints(String points) {
        this.points = points;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public void setComments(String comments) {
        this.comments = comments;
    }


}
