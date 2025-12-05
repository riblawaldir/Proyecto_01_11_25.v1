package com.tuempresa.proyecto_01_11_25.model;

/**
 * Representa un hábito completado con ubicación GPS para el mapa
 */
public class HabitCompletion {
    private long id;
    private long habitId;
    private long userId;
    private String completionDate; // Formato: 'YYYY-MM-DD'
    private double latitude;
    private double longitude;
    private long createdAt;
    
    // Información del hábito (para el mapa)
    private String habitTitle;
    private Habit.HabitType habitType;
    private String habitIcon;
    
    public HabitCompletion() {
    }
    
    public HabitCompletion(long habitId, long userId, String completionDate, 
                          double latitude, double longitude) {
        this.habitId = habitId;
        this.userId = userId;
        this.completionDate = completionDate;
        this.latitude = latitude;
        this.longitude = longitude;
        this.createdAt = System.currentTimeMillis();
    }
    
    // Getters y Setters
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public long getHabitId() {
        return habitId;
    }
    
    public void setHabitId(long habitId) {
        this.habitId = habitId;
    }
    
    public long getUserId() {
        return userId;
    }
    
    public void setUserId(long userId) {
        this.userId = userId;
    }
    
    public String getCompletionDate() {
        return completionDate;
    }
    
    public void setCompletionDate(String completionDate) {
        this.completionDate = completionDate;
    }
    
    public double getLatitude() {
        return latitude;
    }
    
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    
    public double getLongitude() {
        return longitude;
    }
    
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getHabitTitle() {
        return habitTitle;
    }
    
    public void setHabitTitle(String habitTitle) {
        this.habitTitle = habitTitle;
    }
    
    public Habit.HabitType getHabitType() {
        return habitType;
    }
    
    public void setHabitType(Habit.HabitType habitType) {
        this.habitType = habitType;
    }
    
    public String getHabitIcon() {
        return habitIcon;
    }
    
    public void setHabitIcon(String habitIcon) {
        this.habitIcon = habitIcon;
    }
}

