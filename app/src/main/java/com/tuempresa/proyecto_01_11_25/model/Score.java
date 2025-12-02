package com.tuempresa.proyecto_01_11_25.model;

import java.util.Date;

/**
 * Modelo de Score (Puntaje) para la API.
 * Representa un puntaje obtenido al completar un hábito.
 */
public class Score {
    private long id = 0;
    private long userId = -1; // ID del usuario (requerido por API)
    private long habitId;
    private String habitTitle;
    private int points;
    private Date date;

    // Constructor sin parámetros para Gson
    public Score() {
        this.date = new Date();
    }

    public Score(long habitId, String habitTitle, int points) {
        this.habitId = habitId;
        this.habitTitle = habitTitle;
        this.points = points;
        this.date = new Date();
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

    public long getHabitId() {
        return habitId;
    }

    public void setHabitId(long habitId) {
        this.habitId = habitId;
    }

    public String getHabitTitle() {
        return habitTitle;
    }

    public void setHabitTitle(String habitTitle) {
        this.habitTitle = habitTitle;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}

