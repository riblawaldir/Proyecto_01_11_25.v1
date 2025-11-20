package com.tuempresa.proyecto_01_11_25.model;

import java.util.List;

/**
 * Clase wrapper para la respuesta de la API que contiene una lista de hábitos.
 * Esta clase permite que la API devuelva todos los hábitos en una sola llamada.
 */
public class HabitsResponse {
    private boolean success;
    private String message;
    private List<Habit> habits;
    private int count;

    // Constructor sin parámetros para Gson
    public HabitsResponse() {
    }

    public HabitsResponse(boolean success, String message, List<Habit> habits) {
        this.success = success;
        this.message = message;
        this.habits = habits;
        this.count = habits != null ? habits.size() : 0;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Habit> getHabits() {
        return habits;
    }

    public void setHabits(List<Habit> habits) {
        this.habits = habits;
        this.count = habits != null ? habits.size() : 0;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}

