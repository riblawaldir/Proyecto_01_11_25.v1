package com.tuempresa.proyecto_01_11_25.model;

/**
 * DTO para enviar/recibir checkins (hábitos completados con ubicación) desde/hacia la API.
 */
public class HabitCheckinDto {
    private Long id;
    private Long userId;
    private Long habitId;
    private Double latitude;
    private Double longitude;
    private String note;
    private Integer pointsAwarded;
    private String createdAt;
    
    // Campos opcionales con información del hábito (cuando se recibe desde el servidor)
    private String habitTitle;
    private String habitType;
    private String habitIcon;

    public HabitCheckinDto() {
    }

    public HabitCheckinDto(Long habitId, Double latitude, Double longitude, String note) {
        this.habitId = habitId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.note = note;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getHabitId() {
        return habitId;
    }

    public void setHabitId(Long habitId) {
        this.habitId = habitId;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Integer getPointsAwarded() {
        return pointsAwarded;
    }

    public void setPointsAwarded(Integer pointsAwarded) {
        this.pointsAwarded = pointsAwarded;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getHabitTitle() {
        return habitTitle;
    }

    public void setHabitTitle(String habitTitle) {
        this.habitTitle = habitTitle;
    }

    public String getHabitType() {
        return habitType;
    }

    public void setHabitType(String habitType) {
        this.habitType = habitType;
    }

    public String getHabitIcon() {
        return habitIcon;
    }

    public void setHabitIcon(String habitIcon) {
        this.habitIcon = habitIcon;
    }
}

