package com.tuempresa.proyecto_01_11_25.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.tuempresa.proyecto_01_11_25.model.Habit;

import java.util.ArrayList;
import java.util.List;

public class HabitDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "habitus.db";
    private static final int DATABASE_VERSION = 4;

    // Tabla de hábitos
    private static final String TABLE_HABITS = "habits";
    private static final String COLUMN_HABIT_ID = "id";
    private static final String COLUMN_HABIT_TITLE = "title";
    private static final String COLUMN_HABIT_GOAL = "goal";
    private static final String COLUMN_HABIT_CATEGORY = "category";
    private static final String COLUMN_HABIT_TYPE = "type";
    private static final String COLUMN_HABIT_COMPLETED = "completed";
    private static final String COLUMN_HABIT_POINTS = "points";
    private static final String COLUMN_HABIT_CREATED_AT = "created_at";
    private static final String COLUMN_HABIT_TARGET_VALUE = "target_value"; // Para kilómetros, minutos, etc.
    private static final String COLUMN_HABIT_TARGET_UNIT = "target_unit"; // km, min, veces, etc.
    
    // Nuevos campos por tipo de hábito
    private static final String COLUMN_HABIT_PAGES_PER_DAY = "pages_per_day";
    private static final String COLUMN_HABIT_REMINDER_TIMES = "reminder_times";
    private static final String COLUMN_HABIT_DURATION_MINUTES = "duration_minutes";
    private static final String COLUMN_HABIT_DND_MODE = "dnd_mode";
    private static final String COLUMN_HABIT_MUSIC_ID = "music_id";
    private static final String COLUMN_HABIT_JOURNAL_ENABLED = "journal_enabled";
    private static final String COLUMN_HABIT_GYM_DAYS = "gym_days";
    private static final String COLUMN_HABIT_WATER_GOAL_GLASSES = "water_goal_glasses";
    private static final String COLUMN_HABIT_ONE_CLICK_COMPLETE = "one_click_complete";
    private static final String COLUMN_HABIT_ENGLISH_MODE = "english_mode";
    private static final String COLUMN_HABIT_CODING_MODE = "coding_mode";
    private static final String COLUMN_HABIT_ICON = "habit_icon";

    // Tabla de puntaje
    private static final String TABLE_SCORES = "scores";
    private static final String COLUMN_SCORE_ID = "id";
    private static final String COLUMN_SCORE_HABIT_ID = "habit_id";
    private static final String COLUMN_SCORE_POINTS = "points";
    private static final String COLUMN_SCORE_DATE = "date";
    private static final String COLUMN_SCORE_HABIT_TITLE = "habit_title";

    public HabitDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Crear tabla de hábitos
        String createHabitsTable = "CREATE TABLE " + TABLE_HABITS + " (" +
                COLUMN_HABIT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_HABIT_TITLE + " TEXT NOT NULL, " +
                COLUMN_HABIT_GOAL + " TEXT, " +
                COLUMN_HABIT_CATEGORY + " TEXT, " +
                COLUMN_HABIT_TYPE + " TEXT NOT NULL, " +
                COLUMN_HABIT_COMPLETED + " INTEGER DEFAULT 0, " +
                COLUMN_HABIT_POINTS + " INTEGER DEFAULT 10, " +
                COLUMN_HABIT_TARGET_VALUE + " REAL DEFAULT 0, " +
                COLUMN_HABIT_TARGET_UNIT + " TEXT, " +
                COLUMN_HABIT_PAGES_PER_DAY + " INTEGER, " +
                COLUMN_HABIT_REMINDER_TIMES + " TEXT, " +
                COLUMN_HABIT_DURATION_MINUTES + " INTEGER, " +
                COLUMN_HABIT_DND_MODE + " INTEGER DEFAULT 0, " +
                COLUMN_HABIT_MUSIC_ID + " INTEGER, " +
                COLUMN_HABIT_JOURNAL_ENABLED + " INTEGER DEFAULT 0, " +
                COLUMN_HABIT_GYM_DAYS + " TEXT, " +
                COLUMN_HABIT_WATER_GOAL_GLASSES + " INTEGER, " +
                COLUMN_HABIT_ONE_CLICK_COMPLETE + " INTEGER DEFAULT 0, " +
                COLUMN_HABIT_ENGLISH_MODE + " INTEGER DEFAULT 0, " +
                COLUMN_HABIT_CODING_MODE + " INTEGER DEFAULT 0, " +
                COLUMN_HABIT_ICON + " TEXT, " +
                COLUMN_HABIT_CREATED_AT + " INTEGER DEFAULT (strftime('%s', 'now'))" +
                ")";

        // Crear tabla de puntaje
        String createScoresTable = "CREATE TABLE " + TABLE_SCORES + " (" +
                COLUMN_SCORE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_SCORE_HABIT_ID + " INTEGER, " +
                COLUMN_SCORE_HABIT_TITLE + " TEXT NOT NULL, " +
                COLUMN_SCORE_POINTS + " INTEGER NOT NULL, " +
                COLUMN_SCORE_DATE + " INTEGER DEFAULT (strftime('%s', 'now')), " +
                "FOREIGN KEY(" + COLUMN_SCORE_HABIT_ID + ") REFERENCES " + TABLE_HABITS + "(" + COLUMN_HABIT_ID + ")" +
                ")";

        db.execSQL(createHabitsTable);
        db.execSQL(createScoresTable);

        // Insertar hábitos predeterminados
        insertDefaultHabits(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Agregar nuevas columnas si no existen
            try {
                db.execSQL("ALTER TABLE " + TABLE_HABITS + " ADD COLUMN " + COLUMN_HABIT_TARGET_VALUE + " REAL DEFAULT 0");
            } catch (Exception e) {
                // Si la columna ya existe, ignorar el error
            }
            try {
                db.execSQL("ALTER TABLE " + TABLE_HABITS + " ADD COLUMN " + COLUMN_HABIT_TARGET_UNIT + " TEXT");
            } catch (Exception e) {
                // Si la columna ya existe, ignorar el error
            }
        }
        
        if (oldVersion < 3) {
            // Agregar nuevos campos para tipos de hábitos personalizados
            addColumnIfNotExists(db, TABLE_HABITS, COLUMN_HABIT_PAGES_PER_DAY, "INTEGER");
            addColumnIfNotExists(db, TABLE_HABITS, COLUMN_HABIT_REMINDER_TIMES, "TEXT");
            addColumnIfNotExists(db, TABLE_HABITS, COLUMN_HABIT_DURATION_MINUTES, "INTEGER");
            addColumnIfNotExists(db, TABLE_HABITS, COLUMN_HABIT_DND_MODE, "INTEGER DEFAULT 0");
            addColumnIfNotExists(db, TABLE_HABITS, COLUMN_HABIT_MUSIC_ID, "INTEGER");
            addColumnIfNotExists(db, TABLE_HABITS, COLUMN_HABIT_JOURNAL_ENABLED, "INTEGER DEFAULT 0");
            addColumnIfNotExists(db, TABLE_HABITS, COLUMN_HABIT_GYM_DAYS, "TEXT");
            addColumnIfNotExists(db, TABLE_HABITS, COLUMN_HABIT_WATER_GOAL_GLASSES, "INTEGER");
            addColumnIfNotExists(db, TABLE_HABITS, COLUMN_HABIT_ONE_CLICK_COMPLETE, "INTEGER DEFAULT 0");
            addColumnIfNotExists(db, TABLE_HABITS, COLUMN_HABIT_ENGLISH_MODE, "INTEGER DEFAULT 0");
            addColumnIfNotExists(db, TABLE_HABITS, COLUMN_HABIT_CODING_MODE, "INTEGER DEFAULT 0");
        }
        
        if (oldVersion < 4) {
            // Agregar columna para ícono personalizado
            addColumnIfNotExists(db, TABLE_HABITS, COLUMN_HABIT_ICON, "TEXT");
        }
    }
    
    private void addColumnIfNotExists(SQLiteDatabase db, String table, String column, String type) {
        try {
            db.execSQL("ALTER TABLE " + table + " ADD COLUMN " + column + " " + type);
        } catch (Exception e) {
            // Si la columna ya existe, ignorar el error
        }
    }
    
    /**
     * Carga los campos adicionales de un hábito desde el cursor
     */
    private void loadHabitExtraFields(Cursor cursor, Habit habit) {
        try {
            int targetValueIndex = cursor.getColumnIndex(COLUMN_HABIT_TARGET_VALUE);
            int targetUnitIndex = cursor.getColumnIndex(COLUMN_HABIT_TARGET_UNIT);
            if (targetValueIndex >= 0 && !cursor.isNull(targetValueIndex)) {
                habit.setTargetValue(cursor.getDouble(targetValueIndex));
            }
            if (targetUnitIndex >= 0 && !cursor.isNull(targetUnitIndex)) {
                habit.setTargetUnit(cursor.getString(targetUnitIndex));
            }
            
            // Nuevos campos
            int pagesPerDayIndex = cursor.getColumnIndex(COLUMN_HABIT_PAGES_PER_DAY);
            if (pagesPerDayIndex >= 0 && !cursor.isNull(pagesPerDayIndex)) {
                habit.setPagesPerDay(cursor.getInt(pagesPerDayIndex));
            }
            
            int reminderTimesIndex = cursor.getColumnIndex(COLUMN_HABIT_REMINDER_TIMES);
            if (reminderTimesIndex >= 0 && !cursor.isNull(reminderTimesIndex)) {
                habit.setReminderTimes(cursor.getString(reminderTimesIndex));
            }
            
            int durationMinutesIndex = cursor.getColumnIndex(COLUMN_HABIT_DURATION_MINUTES);
            if (durationMinutesIndex >= 0 && !cursor.isNull(durationMinutesIndex)) {
                habit.setDurationMinutes(cursor.getInt(durationMinutesIndex));
            }
            
            int dndModeIndex = cursor.getColumnIndex(COLUMN_HABIT_DND_MODE);
            if (dndModeIndex >= 0 && !cursor.isNull(dndModeIndex)) {
                habit.setDndMode(cursor.getInt(dndModeIndex) == 1);
            }
            
            int musicIdIndex = cursor.getColumnIndex(COLUMN_HABIT_MUSIC_ID);
            if (musicIdIndex >= 0 && !cursor.isNull(musicIdIndex)) {
                habit.setMusicId(cursor.getInt(musicIdIndex));
            }
            
            int journalEnabledIndex = cursor.getColumnIndex(COLUMN_HABIT_JOURNAL_ENABLED);
            if (journalEnabledIndex >= 0 && !cursor.isNull(journalEnabledIndex)) {
                habit.setJournalEnabled(cursor.getInt(journalEnabledIndex) == 1);
            }
            
            int gymDaysIndex = cursor.getColumnIndex(COLUMN_HABIT_GYM_DAYS);
            if (gymDaysIndex >= 0 && !cursor.isNull(gymDaysIndex)) {
                habit.setGymDays(cursor.getString(gymDaysIndex));
            }
            
            int waterGoalGlassesIndex = cursor.getColumnIndex(COLUMN_HABIT_WATER_GOAL_GLASSES);
            if (waterGoalGlassesIndex >= 0 && !cursor.isNull(waterGoalGlassesIndex)) {
                habit.setWaterGoalGlasses(cursor.getInt(waterGoalGlassesIndex));
            }
            
            int oneClickCompleteIndex = cursor.getColumnIndex(COLUMN_HABIT_ONE_CLICK_COMPLETE);
            if (oneClickCompleteIndex >= 0 && !cursor.isNull(oneClickCompleteIndex)) {
                habit.setOneClickComplete(cursor.getInt(oneClickCompleteIndex) == 1);
            }
            
            int englishModeIndex = cursor.getColumnIndex(COLUMN_HABIT_ENGLISH_MODE);
            if (englishModeIndex >= 0 && !cursor.isNull(englishModeIndex)) {
                habit.setEnglishMode(cursor.getInt(englishModeIndex) == 1);
            }
            
            int codingModeIndex = cursor.getColumnIndex(COLUMN_HABIT_CODING_MODE);
            if (codingModeIndex >= 0 && !cursor.isNull(codingModeIndex)) {
                habit.setCodingMode(cursor.getInt(codingModeIndex) == 1);
            }
            
            int habitIconIndex = cursor.getColumnIndex(COLUMN_HABIT_ICON);
            if (habitIconIndex >= 0 && !cursor.isNull(habitIconIndex)) {
                habit.setHabitIcon(cursor.getString(habitIconIndex));
            }
        } catch (Exception e) {
            // Si las columnas no existen, ignorar el error
        }
    }

    private void insertDefaultHabits(SQLiteDatabase db) {
        String[] defaultHabits = {
                "Ejercicio", "Goal: movimiento detectado", "salud", "EXERCISE",
                "Caminar", "Goal: 150 metros", "salud", "WALK",
                "Leer", "Goal: detectar página de libro", "educación", "READ",
                "Demo", "Goal: tocar para completar", "general", "DEMO"
        };

        for (int i = 0; i < defaultHabits.length; i += 4) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_HABIT_TITLE, defaultHabits[i]);
            values.put(COLUMN_HABIT_GOAL, defaultHabits[i + 1]);
            values.put(COLUMN_HABIT_CATEGORY, defaultHabits[i + 2]);
            values.put(COLUMN_HABIT_TYPE, defaultHabits[i + 3]);
            values.put(COLUMN_HABIT_POINTS, 10);
            db.insert(TABLE_HABITS, null, values);
        }
    }

    // ========== CRUD HÁBITOS ==========

    public long insertHabit(String title, String goal, String category, String type, int points) {
        return insertHabit(title, goal, category, type, points, 0.0, null);
    }
    
    public long insertHabit(String title, String goal, String category, String type, int points, double targetValue, String targetUnit) {
        return insertHabitFull(title, goal, category, type, points, targetValue != 0.0 ? (Double) targetValue : null, targetUnit, null, null, null, null, null, null, null, null, null, null, null, null);
    }
    
    /**
     * Inserta un hábito con todos los campos opcionales
     */
    public long insertHabitFull(String title, String goal, String category, String type, int points,
                                Double targetValue, String targetUnit,
                                Integer pagesPerDay, String reminderTimes, Integer durationMinutes,
                                Boolean dndMode, Integer musicId, Boolean journalEnabled,
                                String gymDays, Integer waterGoalGlasses, Boolean oneClickComplete,
                                Boolean englishMode, Boolean codingMode, String habitIcon) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_HABIT_TITLE, title);
        values.put(COLUMN_HABIT_GOAL, goal);
        values.put(COLUMN_HABIT_CATEGORY, category);
        values.put(COLUMN_HABIT_TYPE, type);
        values.put(COLUMN_HABIT_POINTS, points);
        
        if (targetValue != null) values.put(COLUMN_HABIT_TARGET_VALUE, targetValue);
        if (targetUnit != null) values.put(COLUMN_HABIT_TARGET_UNIT, targetUnit);
        if (pagesPerDay != null) values.put(COLUMN_HABIT_PAGES_PER_DAY, pagesPerDay);
        if (reminderTimes != null) values.put(COLUMN_HABIT_REMINDER_TIMES, reminderTimes);
        if (durationMinutes != null) values.put(COLUMN_HABIT_DURATION_MINUTES, durationMinutes);
        if (dndMode != null) values.put(COLUMN_HABIT_DND_MODE, dndMode ? 1 : 0);
        if (musicId != null) values.put(COLUMN_HABIT_MUSIC_ID, musicId);
        if (journalEnabled != null) values.put(COLUMN_HABIT_JOURNAL_ENABLED, journalEnabled ? 1 : 0);
        if (gymDays != null) values.put(COLUMN_HABIT_GYM_DAYS, gymDays);
        if (waterGoalGlasses != null) values.put(COLUMN_HABIT_WATER_GOAL_GLASSES, waterGoalGlasses);
        if (oneClickComplete != null) values.put(COLUMN_HABIT_ONE_CLICK_COMPLETE, oneClickComplete ? 1 : 0);
        if (englishMode != null) values.put(COLUMN_HABIT_ENGLISH_MODE, englishMode ? 1 : 0);
        if (codingMode != null) values.put(COLUMN_HABIT_CODING_MODE, codingMode ? 1 : 0);
        if (habitIcon != null) values.put(COLUMN_HABIT_ICON, habitIcon);
        
        long id = db.insert(TABLE_HABITS, null, values);
        db.close();
        return id;
    }

    public List<Habit> getAllHabits() {
        List<Habit> habits = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_HABITS, null, null, null, null, null, COLUMN_HABIT_CREATED_AT + " DESC");

        if (cursor.moveToFirst()) {
            do {
                Habit habit = new Habit(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HABIT_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HABIT_GOAL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HABIT_CATEGORY)),
                        Habit.HabitType.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HABIT_TYPE)))
                );
                habit.setCompleted(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_HABIT_COMPLETED)) == 1);
                // Guardar ID del hábito para editar/eliminar
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_HABIT_ID));
                habit.setId(id);
                
                // Cargar campos adicionales si existen
                loadHabitExtraFields(cursor, habit);
                
                habits.add(habit);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return habits;
    }
    
    public long getHabitIdByTitle(String title) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_HABITS, new String[]{COLUMN_HABIT_ID}, 
                COLUMN_HABIT_TITLE + "=?", new String[]{title}, null, null, null);
        
        long id = -1;
        if (cursor.moveToFirst()) {
            id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_HABIT_ID));
        }
        cursor.close();
        db.close();
        return id;
    }

    public Habit getHabitById(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_HABITS, null, COLUMN_HABIT_ID + "=?", 
                new String[]{String.valueOf(id)}, null, null, null);

        Habit habit = null;
        if (cursor.moveToFirst()) {
            habit = new Habit(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HABIT_TITLE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HABIT_GOAL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HABIT_CATEGORY)),
                    Habit.HabitType.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HABIT_TYPE)))
            );
            habit.setId(id);
            habit.setCompleted(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_HABIT_COMPLETED)) == 1);
            
            // Cargar campos adicionales si existen
            loadHabitExtraFields(cursor, habit);
        }
        cursor.close();
        db.close();
        return habit;
    }

    public boolean updateHabit(long id, String title, String goal, String category, String type, int points) {
        return updateHabit(id, title, goal, category, type, points, 0.0, null);
    }
    
    public boolean updateHabit(long id, String title, String goal, String category, String type, int points, double targetValue, String targetUnit) {
        return updateHabitFull(id, title, goal, category, type, points, targetValue != 0.0 ? (Double) targetValue : null, targetUnit, null, null, null, null, null, null, null, null, null, null, null, null);
    }
    
    /**
     * Actualiza un hábito con todos los campos opcionales
     */
    public boolean updateHabitFull(long id, String title, String goal, String category, String type, int points,
                                   Double targetValue, String targetUnit,
                                   Integer pagesPerDay, String reminderTimes, Integer durationMinutes,
                                   Boolean dndMode, Integer musicId, Boolean journalEnabled,
                                   String gymDays, Integer waterGoalGlasses, Boolean oneClickComplete,
                                   Boolean englishMode, Boolean codingMode, String habitIcon) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_HABIT_TITLE, title);
        values.put(COLUMN_HABIT_GOAL, goal);
        values.put(COLUMN_HABIT_CATEGORY, category);
        values.put(COLUMN_HABIT_TYPE, type);
        values.put(COLUMN_HABIT_POINTS, points);
        
        if (targetValue != null) values.put(COLUMN_HABIT_TARGET_VALUE, targetValue);
        if (targetUnit != null) values.put(COLUMN_HABIT_TARGET_UNIT, targetUnit);
        if (pagesPerDay != null) values.put(COLUMN_HABIT_PAGES_PER_DAY, pagesPerDay);
        if (reminderTimes != null) values.put(COLUMN_HABIT_REMINDER_TIMES, reminderTimes);
        if (durationMinutes != null) values.put(COLUMN_HABIT_DURATION_MINUTES, durationMinutes);
        if (dndMode != null) values.put(COLUMN_HABIT_DND_MODE, dndMode ? 1 : 0);
        if (musicId != null) values.put(COLUMN_HABIT_MUSIC_ID, musicId);
        if (journalEnabled != null) values.put(COLUMN_HABIT_JOURNAL_ENABLED, journalEnabled ? 1 : 0);
        if (gymDays != null) values.put(COLUMN_HABIT_GYM_DAYS, gymDays);
        if (waterGoalGlasses != null) values.put(COLUMN_HABIT_WATER_GOAL_GLASSES, waterGoalGlasses);
        if (oneClickComplete != null) values.put(COLUMN_HABIT_ONE_CLICK_COMPLETE, oneClickComplete ? 1 : 0);
        if (englishMode != null) values.put(COLUMN_HABIT_ENGLISH_MODE, englishMode ? 1 : 0);
        if (codingMode != null) values.put(COLUMN_HABIT_CODING_MODE, codingMode ? 1 : 0);
        if (habitIcon != null) values.put(COLUMN_HABIT_ICON, habitIcon);
        
        int rowsAffected = db.update(TABLE_HABITS, values, COLUMN_HABIT_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return rowsAffected > 0;
    }

    public boolean deleteHabit(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_HABITS, COLUMN_HABIT_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return rowsAffected > 0;
    }

    public void updateHabitCompleted(String title, boolean completed) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_HABIT_COMPLETED, completed ? 1 : 0);
        db.update(TABLE_HABITS, values, COLUMN_HABIT_TITLE + "=?", new String[]{title});
        db.close();
    }

    public int getHabitPoints(String title) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_HABITS, new String[]{COLUMN_HABIT_POINTS}, 
                COLUMN_HABIT_TITLE + "=?", new String[]{title}, null, null, null);
        
        int points = 10; // default
        if (cursor.moveToFirst()) {
            points = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_HABIT_POINTS));
        }
        cursor.close();
        db.close();
        return points;
    }

    // ========== SISTEMA DE PUNTAJE ==========

    public long addScore(String habitTitle, int points) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SCORE_HABIT_TITLE, habitTitle);
        values.put(COLUMN_SCORE_POINTS, points);
        long id = db.insert(TABLE_SCORES, null, values);
        db.close();
        return id;
    }

    public int getTotalScore() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + COLUMN_SCORE_POINTS + ") as total FROM " + TABLE_SCORES, null);
        
        int total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getInt(cursor.getColumnIndexOrThrow("total"));
        }
        cursor.close();
        db.close();
        return total;
    }

    public List<ScoreEntry> getAllScores() {
        List<ScoreEntry> scores = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SCORES, null, null, null, null, null, COLUMN_SCORE_DATE + " DESC");

        if (cursor.moveToFirst()) {
            do {
                ScoreEntry entry = new ScoreEntry(
                        cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_SCORE_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SCORE_HABIT_TITLE)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SCORE_POINTS)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_SCORE_DATE))
                );
                scores.add(entry);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return scores;
    }

    public static class ScoreEntry {
        private long id;
        private String habitTitle;
        private int points;
        private long date;

        public ScoreEntry(long id, String habitTitle, int points, long date) {
            this.id = id;
            this.habitTitle = habitTitle;
            this.points = points;
            this.date = date;
        }

        public long getId() { return id; }
        public String getHabitTitle() { return habitTitle; }
        public int getPoints() { return points; }
        public long getDate() { return date; }
    }
}

