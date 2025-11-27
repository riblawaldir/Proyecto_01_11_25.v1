package com.tuempresa.proyecto_01_11_25.model;

public class UserRanking {
    private String userName;
    private int totalScore;
    private int habitsCompletedCount;

    public UserRanking(String userName, int totalScore, int habitsCompletedCount) {
        this.userName = userName;
        this.totalScore = totalScore;
        this.habitsCompletedCount = habitsCompletedCount;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public int getHabitsCompletedCount() {
        return habitsCompletedCount;
    }

    public void setHabitsCompletedCount(int habitsCompletedCount) {
        this.habitsCompletedCount = habitsCompletedCount;
    }
}
