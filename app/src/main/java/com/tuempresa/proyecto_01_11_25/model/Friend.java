package com.tuempresa.proyecto_01_11_25.model;

public class Friend {
    private long id;
    private long userId; // ID del usuario que agregó este amigo
    private long friendUserId; // ID del amigo en el servidor
    private String friendEmail;
    private String friendName;
    private int totalHabits;
    private int totalPoints;
    private int currentStreak;
    private long addedAt;

    public Friend() {
    }

    public Friend(long id, long userId, long friendUserId, String friendEmail, String friendName) {
        this.id = id;
        this.userId = userId;
        this.friendUserId = friendUserId;
        this.friendEmail = friendEmail;
        this.friendName = friendName;
        this.addedAt = System.currentTimeMillis();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getFriendUserId() {
        return friendUserId;
    }

    public void setFriendUserId(long friendUserId) {
        this.friendUserId = friendUserId;
    }

    public String getFriendEmail() {
        return friendEmail;
    }

    public void setFriendEmail(String friendEmail) {
        this.friendEmail = friendEmail;
    }

    public String getFriendName() {
        return friendName;
    }

    public void setFriendName(String friendName) {
        this.friendName = friendName;
    }

    public int getTotalHabits() {
        return totalHabits;
    }

    public void setTotalHabits(int totalHabits) {
        this.totalHabits = totalHabits;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
    }

    public long getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(long addedAt) {
        this.addedAt = addedAt;
    }
}

