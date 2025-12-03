package com.tuempresa.proyecto_01_11_25.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Habit {

    public enum HabitType {
        EXERCISE, // acelerómetro
        WALK, // distancia
        DEMO, // botón
        READ, // cámara + ML Kit
        // Nuevos tipos
        READ_BOOK, // Leer X páginas al día
        VITAMINS, // Tomar vitaminas
        MEDITATE, // Meditar
        JOURNALING, // Journaling
        GYM, // Ir al gym
        WATER, // Beber agua
        COLD_SHOWER, // Ducha fría
        ENGLISH, // Practicar inglés
        CODING // Practicar coding
    }

    private long id = -1;
    @SerializedName(value = "userId", alternate = {"UserId", "user_id"})
    private long userId = -1; // ID del usuario (requerido por API)
    private String title;
    private String goal;
    private String category;
    private HabitType type;
    private boolean completed;
    private int points = 10; // Puntos por defecto
    private double targetValue = 0.0;
    private String targetUnit = null;

    // Nuevos campos por tipo de hábito
    private Integer pagesPerDay = null;
    private String reminderTimes = null; // JSON string
    private Integer durationMinutes = null;
    private Boolean dndMode = null;
    private Integer musicId = null;
    private Boolean journalEnabled = null;
    private String gymDays = null; // JSON string
    private Integer waterGoalGlasses = null;
    private Integer walkGoalMeters = null; // Meta de distancia en metros para caminar
    private Integer walkGoalSteps = null; // Meta de pasos para caminar (alternativa a metros)
    private Boolean oneClickComplete = null;
    private Boolean englishMode = null;
    private Boolean codingMode = null;
    private String habitIcon = null; // Nombre del ícono personalizado (ej: "ic_habit_book", "ic_habit_vitamins")

    // Campos para racha
    private int streakCount = 0;
    private long lastCompletedDate = 0;

    // Constructor sin parámetros para Gson
    public Habit() {
        this.completed = false;
    }

    public Habit(String title, String goal, String category, HabitType type) {
        this.title = title;
        this.goal = goal;
        this.category = category;
        this.type = type;
        this.completed = false;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public HabitType getType() {
        return type;
    }

    public void setType(HabitType type) {
        this.type = type;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public double getTargetValue() {
        return targetValue;
    }

    public void setTargetValue(double targetValue) {
        this.targetValue = targetValue;
    }

    public String getTargetUnit() {
        return targetUnit;
    }

    public void setTargetUnit(String targetUnit) {
        this.targetUnit = targetUnit;
    }

    // Getters y Setters para nuevos campos
    public Integer getPagesPerDay() {
        return pagesPerDay;
    }

    public void setPagesPerDay(Integer pagesPerDay) {
        this.pagesPerDay = pagesPerDay;
    }

    public String getReminderTimes() {
        return reminderTimes;
    }

    public void setReminderTimes(String reminderTimes) {
        this.reminderTimes = reminderTimes;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Boolean getDndMode() {
        return dndMode;
    }

    public boolean isDndMode() {
        return dndMode != null && dndMode;
    }

    public void setDndMode(Boolean dndMode) {
        this.dndMode = dndMode;
    }

    public Integer getMusicId() {
        return musicId;
    }

    public void setMusicId(Integer musicId) {
        this.musicId = musicId;
    }

    public Boolean getJournalEnabled() {
        return journalEnabled;
    }

    public boolean isJournalEnabled() {
        return journalEnabled != null && journalEnabled;
    }

    public void setJournalEnabled(Boolean journalEnabled) {
        this.journalEnabled = journalEnabled;
    }

    public String getGymDays() {
        return gymDays;
    }

    public void setGymDays(String gymDays) {
        this.gymDays = gymDays;
    }

    public Integer getWaterGoalGlasses() {
        return waterGoalGlasses;
    }

    public void setWaterGoalGlasses(Integer waterGoalGlasses) {
        this.waterGoalGlasses = waterGoalGlasses;
    }

    public Integer getWalkGoalMeters() {
        return walkGoalMeters;
    }

    public void setWalkGoalMeters(Integer walkGoalMeters) {
        this.walkGoalMeters = walkGoalMeters;
    }

    public Integer getWalkGoalSteps() {
        return walkGoalSteps;
    }

    public void setWalkGoalSteps(Integer walkGoalSteps) {
        this.walkGoalSteps = walkGoalSteps;
    }

    public Boolean getOneClickComplete() {
        return oneClickComplete;
    }

    public boolean isOneClickComplete() {
        return oneClickComplete != null && oneClickComplete;
    }

    public void setOneClickComplete(Boolean oneClickComplete) {
        this.oneClickComplete = oneClickComplete;
    }

    public Boolean getEnglishMode() {
        return englishMode;
    }

    public boolean isEnglishMode() {
        return englishMode != null && englishMode;
    }

    public void setEnglishMode(Boolean englishMode) {
        this.englishMode = englishMode;
    }

    public Boolean getCodingMode() {
        return codingMode;
    }

    public boolean isCodingMode() {
        return codingMode != null && codingMode;
    }

    public void setCodingMode(Boolean codingMode) {
        this.codingMode = codingMode;
    }

    public String getHabitIcon() {
        return habitIcon;
    }

    public void setHabitIcon(String habitIcon) {
        this.habitIcon = habitIcon;
    }

    public int getStreakCount() {
        return streakCount;
    }

    public void setStreakCount(int streakCount) {
        this.streakCount = streakCount;
    }

    public long getLastCompletedDate() {
        return lastCompletedDate;
    }

    public void setLastCompletedDate(long lastCompletedDate) {
        this.lastCompletedDate = lastCompletedDate;
    }
    // list.add(new Habit("Ejercicio", "Goal: movimiento detectado", "salud",
    // HabitType.EXERCISE));
    // list.add(new Habit("Caminar", "Goal: 150 metros", "salud", HabitType.WALK));
    // list.add(new Habit("Leer", "Goal: detectar página de libro", "educación",
    // HabitType.READ));
    // list.add(new Habit("Demo", "Goal: tocar para completar", "general",
    // HabitType.DEMO));
    // return list;
    // }
}
