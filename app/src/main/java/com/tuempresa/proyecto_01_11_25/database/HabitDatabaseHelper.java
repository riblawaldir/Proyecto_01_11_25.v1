package com.tuempresa.proyecto_01_11_25.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.tuempresa.proyecto_01_11_25.model.Habit;
import com.tuempresa.proyecto_01_11_25.model.Friend;

import java.util.ArrayList;
import java.util.List;

public class HabitDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "habitus.db";
    private static final int DATABASE_VERSION = 13;
    private final Context context;

    // Tabla de hábitos (protected para que HabitDatabaseHelperSync pueda acceder)
    protected static final String TABLE_HABITS = "habits";
    protected static final String COLUMN_HABIT_ID = "id";
    protected static final String COLUMN_HABIT_TITLE = "title";
    protected static final String COLUMN_HABIT_GOAL = "goal";
    protected static final String COLUMN_HABIT_CATEGORY = "category";
    protected static final String COLUMN_HABIT_TYPE = "type";
    protected static final String COLUMN_HABIT_COMPLETED = "completed";
    protected static final String COLUMN_HABIT_POINTS = "points";
    protected static final String COLUMN_HABIT_CREATED_AT = "created_at";
    protected static final String COLUMN_HABIT_TARGET_VALUE = "target_value"; // Para kilómetros, minutos, etc.
    protected static final String COLUMN_HABIT_TARGET_UNIT = "target_unit"; // km, min, veces, etc.

    // Nuevos campos por tipo de hábito
    protected static final String COLUMN_HABIT_PAGES_PER_DAY = "pages_per_day";
    protected static final String COLUMN_HABIT_REMINDER_TIMES = "reminder_times";
    protected static final String COLUMN_HABIT_DURATION_MINUTES = "duration_minutes";
    protected static final String COLUMN_HABIT_DND_MODE = "dnd_mode";
    protected static final String COLUMN_HABIT_MUSIC_ID = "music_id";
    protected static final String COLUMN_HABIT_JOURNAL_ENABLED = "journal_enabled";
    protected static final String COLUMN_HABIT_GYM_DAYS = "gym_days";
    protected static final String COLUMN_HABIT_WATER_GOAL_GLASSES = "water_goal_glasses";
    protected static final String COLUMN_HABIT_WALK_GOAL_METERS = "walk_goal_meters";
    protected static final String COLUMN_HABIT_WALK_GOAL_STEPS = "walk_goal_steps";
    protected static final String COLUMN_HABIT_ONE_CLICK_COMPLETE = "one_click_complete";
    protected static final String COLUMN_HABIT_ENGLISH_MODE = "english_mode";
    protected static final String COLUMN_HABIT_CODING_MODE = "coding_mode";
    protected static final String COLUMN_HABIT_ICON = "habit_icon";

    // Tabla de puntaje
    protected static final String TABLE_SCORES = "scores";
    protected static final String COLUMN_SCORE_ID = "id";
    protected static final String COLUMN_SCORE_HABIT_ID = "habit_id";
    protected static final String COLUMN_SCORE_POINTS = "points";
    protected static final String COLUMN_SCORE_DATE = "date";
    protected static final String COLUMN_SCORE_HABIT_TITLE = "habit_title";

    // Tabla de usuarios
    protected static final String TABLE_USERS = "users";
    protected static final String COLUMN_USER_ID = "user_id";
    protected static final String COLUMN_USER_EMAIL = "email";
    protected static final String COLUMN_USER_PASSWORD_HASH = "password_hash";
    protected static final String COLUMN_USER_CREATED_AT = "created_at";
    protected static final String COLUMN_USER_IS_ACTIVE = "is_active";
    protected static final String COLUMN_USER_FIRST_NAME = "first_name";
    protected static final String COLUMN_USER_LAST_NAME = "last_name";
    protected static final String COLUMN_USER_PHONE = "phone";
    // Campos para racha diaria
    protected static final String COLUMN_USER_CURRENT_STREAK = "current_streak";
    protected static final String COLUMN_USER_LAST_STREAK_DATE = "last_streak_date";
    protected static final String COLUMN_USER_DAILY_HABITS_COMPLETED = "daily_habits_completed";
    protected static final String COLUMN_USER_LAST_ACTIVITY_DATE = "last_activity_date";

    // Nuevas columnas para Habits (según esquema)
    protected static final String COLUMN_HABIT_USER_ID = "user_id";
    protected static final String COLUMN_HABIT_IS_ACTIVE = "is_active";
    protected static final String COLUMN_HABIT_POINTS_PER_COMPLETION = "points_per_completion";
    protected static final String COLUMN_HABIT_STREAK_COUNT = "streak_count";
    protected static final String COLUMN_HABIT_LAST_COMPLETED_DATE = "last_completed_date";

    // Nuevas columnas para Scores (según esquema)
    protected static final String COLUMN_SCORE_USER_ID = "user_id";
    protected static final String COLUMN_SCORE_NOTE = "note";

    // Tabla de entradas del diario
    protected static final String TABLE_DIARY_ENTRIES = "diary_entries";
    protected static final String COLUMN_DIARY_ID = "id";
    protected static final String COLUMN_DIARY_HABIT_ID = "habit_id";
    protected static final String COLUMN_DIARY_USER_ID = "user_id";
    protected static final String COLUMN_DIARY_TITLE = "title";
    protected static final String COLUMN_DIARY_CONTENT = "content";
    protected static final String COLUMN_DIARY_DATE = "date";
    protected static final String COLUMN_DIARY_CREATED_AT = "created_at";

    // Tabla de amigos
    protected static final String TABLE_FRIENDS = "friends";
    protected static final String COLUMN_FRIEND_ID = "id";
    protected static final String COLUMN_FRIEND_USER_ID = "user_id";
    protected static final String COLUMN_FRIEND_FRIEND_USER_ID = "friend_user_id";
    protected static final String COLUMN_FRIEND_EMAIL = "friend_email";
    protected static final String COLUMN_FRIEND_NAME = "friend_name";
    protected static final String COLUMN_FRIEND_TOTAL_HABITS = "total_habits";
    protected static final String COLUMN_FRIEND_TOTAL_POINTS = "total_points";
    protected static final String COLUMN_FRIEND_CURRENT_STREAK = "current_streak";
    protected static final String COLUMN_FRIEND_ADDED_AT = "added_at";

    public HabitDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Crear tabla de usuarios
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USER_EMAIL + " TEXT UNIQUE, " +
                COLUMN_USER_PASSWORD_HASH + " TEXT, " +
                COLUMN_USER_FIRST_NAME + " TEXT, " +
                COLUMN_USER_LAST_NAME + " TEXT, " +
                COLUMN_USER_PHONE + " TEXT, " +
                COLUMN_USER_CREATED_AT + " INTEGER, " +
                COLUMN_USER_IS_ACTIVE + " INTEGER DEFAULT 1, " +
                COLUMN_USER_CURRENT_STREAK + " INTEGER DEFAULT 0, " +
                COLUMN_USER_LAST_STREAK_DATE + " INTEGER DEFAULT 0, " +
                COLUMN_USER_DAILY_HABITS_COMPLETED + " INTEGER DEFAULT 0, " +
                COLUMN_USER_LAST_ACTIVITY_DATE + " INTEGER DEFAULT 0" +
                ")";
        db.execSQL(createUsersTable);

        // Crear tabla de hábitos (Schema actualizado)
        String createHabitsTable = "CREATE TABLE " + TABLE_HABITS + " (" +
                COLUMN_HABIT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_HABIT_USER_ID + " INTEGER, " + // FK
                COLUMN_HABIT_TITLE + " TEXT NOT NULL, " +
                COLUMN_HABIT_GOAL + " TEXT, " +
                COLUMN_HABIT_CATEGORY + " TEXT, " +
                COLUMN_HABIT_TYPE + " TEXT NOT NULL, " +
                COLUMN_HABIT_COMPLETED + " INTEGER DEFAULT 0, " +
                COLUMN_HABIT_POINTS + " INTEGER DEFAULT 10, " + // Mantenemos por compatibilidad
                COLUMN_HABIT_POINTS_PER_COMPLETION + " INTEGER DEFAULT 10, " + // Nuevo campo del esquema
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
                COLUMN_HABIT_WALK_GOAL_METERS + " INTEGER, " +
                COLUMN_HABIT_WALK_GOAL_STEPS + " INTEGER, " +
                COLUMN_HABIT_ONE_CLICK_COMPLETE + " INTEGER DEFAULT 0, " +
                COLUMN_HABIT_ENGLISH_MODE + " INTEGER DEFAULT 0, " +
                COLUMN_HABIT_CODING_MODE + " INTEGER DEFAULT 0, " +
                COLUMN_HABIT_ICON + " TEXT, " +
                COLUMN_HABIT_CREATED_AT + " INTEGER DEFAULT (strftime('%s', 'now')), " +
                COLUMN_HABIT_IS_ACTIVE + " INTEGER DEFAULT 1, " +
                COLUMN_HABIT_STREAK_COUNT + " INTEGER DEFAULT 0, " +
                COLUMN_HABIT_LAST_COMPLETED_DATE + " INTEGER DEFAULT 0, " +
                "FOREIGN KEY(" + COLUMN_HABIT_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ")" +
                ")";
        db.execSQL(createHabitsTable);

        // Trigger para actualizar racha
        String createStreakTrigger = "CREATE TRIGGER update_streak AFTER UPDATE OF " + COLUMN_HABIT_COMPLETED + " ON "
                + TABLE_HABITS +
                " BEGIN " +
                "   UPDATE " + TABLE_HABITS + " SET " +
                "     " + COLUMN_HABIT_STREAK_COUNT + " = CASE " +
                "       WHEN NEW." + COLUMN_HABIT_COMPLETED + " = 1 THEN " +
                "         CASE " +
                "           WHEN (strftime('%s', 'now') - " + COLUMN_HABIT_LAST_COMPLETED_DATE + ") < 86400 THEN "
                + COLUMN_HABIT_STREAK_COUNT + " " + // Menos de 24h (mismo día aprox, simplificado) -> no cambia
                "           WHEN (strftime('%s', 'now') - " + COLUMN_HABIT_LAST_COMPLETED_DATE + ") < 172800 THEN "
                + COLUMN_HABIT_STREAK_COUNT + " + 1 " + // Menos de 48h (ayer) -> +1
                "           ELSE 1 " + // Más de 48h -> reinicia a 1
                "         END " +
                "       ELSE " + COLUMN_HABIT_STREAK_COUNT + " " + // Si se desmarca, no cambiamos la racha (o podríamos
                                                                   // restarla, pero simplifiquemos)
                "     END, " +
                "     " + COLUMN_HABIT_LAST_COMPLETED_DATE + " = CASE WHEN NEW." + COLUMN_HABIT_COMPLETED
                + " = 1 THEN strftime('%s', 'now') ELSE " + COLUMN_HABIT_LAST_COMPLETED_DATE + " END " +
                "   WHERE " + COLUMN_HABIT_ID + " = NEW." + COLUMN_HABIT_ID + "; " +
                " END;";
        // Nota: La lógica de tiempo en SQLite con timestamps UNIX puede ser tricky.
        // Para simplificar, asumiremos que la app maneja la lógica de "día" y el
        // trigger solo actualiza si last_completed != hoy.
        // Mejor enfoque: Actualizar streak desde código Java para tener control preciso
        // de "Días calendario" y usar DB solo para persistencia.
        // Pero el usuario pidió Triggers. Vamos a usar una versión simplificada del
        // trigger que actualiza last_completed_date.

        // Re-haciendo el trigger más simple para cumplir el requerimiento pero seguro:
        // Solo actualizamos last_completed_date. El cálculo de racha lo haremos en Java
        // o con un trigger más complejo si es estrictamente necesario.
        // El usuario pidió: "utiliza triggers para eso". Intentaremos un trigger
        // lógico.

        db.execSQL("DROP TRIGGER IF EXISTS update_streak");
        db.execSQL("CREATE TRIGGER update_streak AFTER UPDATE OF " + COLUMN_HABIT_COMPLETED + " ON " + TABLE_HABITS +
                " FOR EACH ROW WHEN NEW." + COLUMN_HABIT_COMPLETED + " = 1 " +
                " BEGIN " +
                "   UPDATE " + TABLE_HABITS + " SET " +
                "     " + COLUMN_HABIT_STREAK_COUNT + " = CASE " +
                "       WHEN (strftime('%J', 'now') - strftime('%J', datetime(" + COLUMN_HABIT_LAST_COMPLETED_DATE
                + ", 'unixepoch'))) >= 1.0 AND (strftime('%J', 'now') - strftime('%J', datetime("
                + COLUMN_HABIT_LAST_COMPLETED_DATE + ", 'unixepoch'))) < 2.0 THEN " + COLUMN_HABIT_STREAK_COUNT
                + " + 1 " + // Ayer
                "       WHEN (strftime('%J', 'now') - strftime('%J', datetime(" + COLUMN_HABIT_LAST_COMPLETED_DATE
                + ", 'unixepoch'))) >= 2.0 THEN 1 " + // Hace más de un día (perdió racha)
                "       WHEN " + COLUMN_HABIT_LAST_COMPLETED_DATE + " = 0 THEN 1 " + // Primera vez
                "       ELSE " + COLUMN_HABIT_STREAK_COUNT + " " + // Mismo día
                "     END, " +
                "     " + COLUMN_HABIT_LAST_COMPLETED_DATE + " = strftime('%s', 'now') " +
                "   WHERE " + COLUMN_HABIT_ID + " = NEW." + COLUMN_HABIT_ID + "; " +
                " END;");

        // Crear tabla de puntaje (Schema actualizado)
        String createScoresTable = "CREATE TABLE " + TABLE_SCORES + " (" +
                COLUMN_SCORE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_SCORE_USER_ID + " INTEGER, " + // FK
                COLUMN_SCORE_HABIT_ID + " INTEGER, " +
                COLUMN_SCORE_HABIT_TITLE + " TEXT NOT NULL, " +
                COLUMN_SCORE_POINTS + " INTEGER NOT NULL, " +
                COLUMN_SCORE_DATE + " INTEGER DEFAULT (strftime('%s', 'now')), " +
                COLUMN_SCORE_NOTE + " TEXT, " +
                "FOREIGN KEY(" + COLUMN_SCORE_HABIT_ID + ") REFERENCES " + TABLE_HABITS + "(" + COLUMN_HABIT_ID + "), "
                +
                "FOREIGN KEY(" + COLUMN_SCORE_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ")" +
                ")";
        db.execSQL(createScoresTable);

        // Crear tabla de entradas del diario
        String createDiaryTable = "CREATE TABLE " + TABLE_DIARY_ENTRIES + " (" +
                COLUMN_DIARY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_DIARY_HABIT_ID + " INTEGER NOT NULL, " +
                COLUMN_DIARY_USER_ID + " INTEGER, " +
                COLUMN_DIARY_TITLE + " TEXT NOT NULL DEFAULT 'Nota', " +
                COLUMN_DIARY_CONTENT + " TEXT NOT NULL, " +
                COLUMN_DIARY_DATE + " INTEGER DEFAULT (strftime('%s', 'now')), " +
                COLUMN_DIARY_CREATED_AT + " INTEGER DEFAULT (strftime('%s', 'now')), " +
                "FOREIGN KEY(" + COLUMN_DIARY_HABIT_ID + ") REFERENCES " + TABLE_HABITS + "(" + COLUMN_HABIT_ID + "), " +
                "FOREIGN KEY(" + COLUMN_DIARY_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ")" +
                ")";
        db.execSQL(createDiaryTable);

        // Crear tabla de amigos
        String createFriendsTable = "CREATE TABLE " + TABLE_FRIENDS + " (" +
                COLUMN_FRIEND_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_FRIEND_USER_ID + " INTEGER NOT NULL, " +
                COLUMN_FRIEND_FRIEND_USER_ID + " INTEGER, " +
                COLUMN_FRIEND_EMAIL + " TEXT NOT NULL, " +
                COLUMN_FRIEND_NAME + " TEXT, " +
                COLUMN_FRIEND_TOTAL_HABITS + " INTEGER DEFAULT 0, " +
                COLUMN_FRIEND_TOTAL_POINTS + " INTEGER DEFAULT 0, " +
                COLUMN_FRIEND_CURRENT_STREAK + " INTEGER DEFAULT 0, " +
                COLUMN_FRIEND_ADDED_AT + " INTEGER DEFAULT (strftime('%s', 'now')), " +
                "FOREIGN KEY(" + COLUMN_FRIEND_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + "), " +
                "UNIQUE(" + COLUMN_FRIEND_USER_ID + ", " + COLUMN_FRIEND_EMAIL + ")" +
                ")";
        db.execSQL(createFriendsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            try {
                db.execSQL(
                        "ALTER TABLE " + TABLE_HABITS + " ADD COLUMN " + COLUMN_HABIT_TARGET_VALUE + " REAL DEFAULT 0");
            } catch (Exception e) {
            }
            try {
                db.execSQL("ALTER TABLE " + TABLE_HABITS + " ADD COLUMN " + COLUMN_HABIT_TARGET_UNIT + " TEXT");
            } catch (Exception e) {
            }
        }

        if (oldVersion < 3) {
            addColumnIfNotExists(db, TABLE_HABITS, COLUMN_HABIT_PAGES_PER_DAY, "INTEGER");
            addColumnIfNotExists(db, TABLE_HABITS, COLUMN_HABIT_REMINDER_TIMES, "TEXT");
            addColumnIfNotExists(db, TABLE_HABITS, COLUMN_HABIT_DURATION_MINUTES, "INTEGER");
            addColumnIfNotExists(db, TABLE_HABITS, COLUMN_HABIT_DND_MODE, "INTEGER DEFAULT 0");
            addColumnIfNotExists(db, TABLE_HABITS, COLUMN_HABIT_MUSIC_ID, "INTEGER");
            addColumnIfNotExists(db, TABLE_HABITS, COLUMN_HABIT_JOURNAL_ENABLED, "INTEGER DEFAULT 0");
            addColumnIfNotExists(db, TABLE_HABITS, COLUMN_HABIT_GYM_DAYS, "TEXT");
            addColumnIfNotExists(db, TABLE_HABITS, COLUMN_HABIT_WATER_GOAL_GLASSES, "INTEGER");
            addColumnIfNotExists(db, TABLE_HABITS, COLUMN_HABIT_WALK_GOAL_METERS, "INTEGER");
            addColumnIfNotExists(db, TABLE_HABITS, COLUMN_HABIT_WALK_GOAL_STEPS, "INTEGER");
            addColumnIfNotExists(db, TABLE_HABITS, COLUMN_HABIT_ONE_CLICK_COMPLETE, "INTEGER DEFAULT 0");
            addColumnIfNotExists(db, TABLE_HABITS, COLUMN_HABIT_ENGLISH_MODE, "INTEGER DEFAULT 0");
            addColumnIfNotExists(db, TABLE_HABITS, COLUMN_HABIT_CODING_MODE, "INTEGER DEFAULT 0");
        }

        if (oldVersion < 4) {
            addColumnIfNotExists(db, TABLE_HABITS, COLUMN_HABIT_ICON, "TEXT");
        }

        if (oldVersion < 5) {
            // Migración a versión 5: Implementar esquema completo

            // 1. Crear tabla USERS
            String createUsersTable = "CREATE TABLE " + TABLE_USERS + " (" +
                    COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USER_EMAIL + " TEXT UNIQUE, " +
                    COLUMN_USER_PASSWORD_HASH + " TEXT, " +
                    COLUMN_USER_FIRST_NAME + " TEXT, " +
                    COLUMN_USER_LAST_NAME + " TEXT, " +
                    COLUMN_USER_PHONE + " TEXT, " +
                    COLUMN_USER_CREATED_AT + " INTEGER, " +
                    COLUMN_USER_IS_ACTIVE + " INTEGER DEFAULT 1" +
                    ")";
            db.execSQL(createUsersTable);

            // 2. Crear usuario por defecto (para migrar datos existentes)
            ContentValues defaultUser = new ContentValues();
            defaultUser.put(COLUMN_USER_EMAIL, "default@local.com");
            defaultUser.put(COLUMN_USER_IS_ACTIVE, 1);
            defaultUser.put(COLUMN_USER_CREATED_AT, System.currentTimeMillis());
            long defaultUserId = db.insert(TABLE_USERS, null, defaultUser);

            // 3. Actualizar tabla HABITS
            addColumnIfNotExists(db, TABLE_HABITS, COLUMN_HABIT_USER_ID, "INTEGER DEFAULT " + defaultUserId);
            addColumnIfNotExists(db, TABLE_HABITS, COLUMN_HABIT_IS_ACTIVE, "INTEGER DEFAULT 1");
            addColumnIfNotExists(db, TABLE_HABITS, COLUMN_HABIT_POINTS_PER_COMPLETION, "INTEGER DEFAULT 10");

            // Sincronizar points_per_completion con points existente
            db.execSQL("UPDATE " + TABLE_HABITS + " SET " + COLUMN_HABIT_POINTS_PER_COMPLETION + " = "
                    + COLUMN_HABIT_POINTS);

            // 4. Actualizar tabla SCORES
            addColumnIfNotExists(db, TABLE_SCORES, COLUMN_SCORE_USER_ID, "INTEGER DEFAULT " + defaultUserId);
            addColumnIfNotExists(db, TABLE_SCORES, COLUMN_SCORE_NOTE, "TEXT");
        }

        if (oldVersion < 6) {
            addColumnIfNotExists(db, TABLE_USERS, COLUMN_USER_FIRST_NAME, "TEXT");
            addColumnIfNotExists(db, TABLE_USERS, COLUMN_USER_LAST_NAME, "TEXT");
            addColumnIfNotExists(db, TABLE_USERS, COLUMN_USER_PHONE, "TEXT");
        }

        if (oldVersion < 7) {

            addColumnIfNotExists(db, TABLE_HABITS, COLUMN_HABIT_STREAK_COUNT, "INTEGER DEFAULT 0");
            addColumnIfNotExists(db, TABLE_HABITS, COLUMN_HABIT_LAST_COMPLETED_DATE, "INTEGER DEFAULT 0");

            // Crear trigger en actualización
            db.execSQL("DROP TRIGGER IF EXISTS update_streak");
            db.execSQL("CREATE TRIGGER update_streak AFTER UPDATE OF " + COLUMN_HABIT_COMPLETED + " ON " + TABLE_HABITS
                    +
                    " FOR EACH ROW WHEN NEW." + COLUMN_HABIT_COMPLETED + " = 1 " +
                    " BEGIN " +
                    "   UPDATE " + TABLE_HABITS + " SET " +
                    "     " + COLUMN_HABIT_STREAK_COUNT + " = CASE " +
                    "       WHEN (strftime('%J', 'now') - strftime('%J', datetime(" + COLUMN_HABIT_LAST_COMPLETED_DATE
                    + ", 'unixepoch'))) >= 1.0 AND (strftime('%J', 'now') - strftime('%J', datetime("
                    + COLUMN_HABIT_LAST_COMPLETED_DATE + ", 'unixepoch'))) < 2.0 THEN " + COLUMN_HABIT_STREAK_COUNT
                    + " + 1 " +
                    "       WHEN (strftime('%J', 'now') - strftime('%J', datetime(" + COLUMN_HABIT_LAST_COMPLETED_DATE
                    + ", 'unixepoch'))) >= 2.0 THEN 1 " +
                    "       WHEN " + COLUMN_HABIT_LAST_COMPLETED_DATE + " = 0 THEN 1 " +
                    "       ELSE " + COLUMN_HABIT_STREAK_COUNT + " " +
                    "     END, " +
                    "     " + COLUMN_HABIT_LAST_COMPLETED_DATE + " = strftime('%s', 'now') " +
                    "   WHERE " + COLUMN_HABIT_ID + " = NEW." + COLUMN_HABIT_ID + "; " +
                    " END;");
        }

        if (oldVersion < 8) {
            // Migración a versión 8: Agregar columnas de sincronización
            // Estas columnas son necesarias para HabitDatabaseHelperSync
            addColumnIfNotExists(db, TABLE_HABITS, "synced", "INTEGER DEFAULT 0");
            addColumnIfNotExists(db, TABLE_HABITS, "server_id", "INTEGER");
            addColumnIfNotExists(db, TABLE_HABITS, "updated_at", "INTEGER DEFAULT 0");
            // Actualizar valores existentes con timestamp actual
            db.execSQL("UPDATE " + TABLE_HABITS + " SET updated_at = " + (System.currentTimeMillis() / 1000) + " WHERE updated_at = 0 OR updated_at IS NULL");
        }

        if (oldVersion < 9) {
            // Migración a versión 9: Crear tabla de entradas del diario
            String createDiaryTable = "CREATE TABLE IF NOT EXISTS " + TABLE_DIARY_ENTRIES + " (" +
                    COLUMN_DIARY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_DIARY_HABIT_ID + " INTEGER NOT NULL, " +
                    COLUMN_DIARY_USER_ID + " INTEGER, " +
                    COLUMN_DIARY_CONTENT + " TEXT NOT NULL, " +
                    COLUMN_DIARY_DATE + " INTEGER DEFAULT (strftime('%s', 'now')), " +
                    COLUMN_DIARY_CREATED_AT + " INTEGER DEFAULT (strftime('%s', 'now')), " +
                    "FOREIGN KEY(" + COLUMN_DIARY_HABIT_ID + ") REFERENCES " + TABLE_HABITS + "(" + COLUMN_HABIT_ID + "), " +
                    "FOREIGN KEY(" + COLUMN_DIARY_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ")" +
                    ")";
            db.execSQL(createDiaryTable);
        }

        if (oldVersion < 10) {
            // Migración a versión 10: Agregar campos de racha diaria a usuarios
            addColumnIfNotExists(db, TABLE_USERS, COLUMN_USER_CURRENT_STREAK, "INTEGER DEFAULT 0");
            addColumnIfNotExists(db, TABLE_USERS, COLUMN_USER_LAST_STREAK_DATE, "INTEGER DEFAULT 0");
            addColumnIfNotExists(db, TABLE_USERS, COLUMN_USER_DAILY_HABITS_COMPLETED, "INTEGER DEFAULT 0");
            addColumnIfNotExists(db, TABLE_USERS, COLUMN_USER_LAST_ACTIVITY_DATE, "INTEGER DEFAULT 0");
        }

        if (oldVersion < 12) {
            // Migración a versión 12: Crear tabla de amigos
            String createFriendsTable = "CREATE TABLE IF NOT EXISTS " + TABLE_FRIENDS + " (" +
                    COLUMN_FRIEND_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_FRIEND_USER_ID + " INTEGER NOT NULL, " +
                    COLUMN_FRIEND_FRIEND_USER_ID + " INTEGER, " +
                    COLUMN_FRIEND_EMAIL + " TEXT NOT NULL, " +
                    COLUMN_FRIEND_NAME + " TEXT, " +
                    COLUMN_FRIEND_TOTAL_HABITS + " INTEGER DEFAULT 0, " +
                    COLUMN_FRIEND_TOTAL_POINTS + " INTEGER DEFAULT 0, " +
                    COLUMN_FRIEND_CURRENT_STREAK + " INTEGER DEFAULT 0, " +
                    COLUMN_FRIEND_ADDED_AT + " INTEGER DEFAULT (strftime('%s', 'now')), " +
                    "FOREIGN KEY(" + COLUMN_FRIEND_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + "), " +
                    "UNIQUE(" + COLUMN_FRIEND_USER_ID + ", " + COLUMN_FRIEND_EMAIL + ")" +
                    ")";
            db.execSQL(createFriendsTable);
        }
        if (oldVersion < 11) {
            // Migración a versión 11: Agregar columna title a diary_entries
            addColumnIfNotExists(db, TABLE_DIARY_ENTRIES, COLUMN_DIARY_TITLE, "TEXT NOT NULL DEFAULT 'Nota'");
        }
        
        // Asegurar que las columnas de racha existan (por si acaso se saltaron migraciones)
        // Esto es seguro porque addColumnIfNotExists verifica si la columna ya existe
        addColumnIfNotExists(db, TABLE_USERS, COLUMN_USER_CURRENT_STREAK, "INTEGER DEFAULT 0");
        addColumnIfNotExists(db, TABLE_USERS, COLUMN_USER_LAST_STREAK_DATE, "INTEGER DEFAULT 0");
        addColumnIfNotExists(db, TABLE_USERS, COLUMN_USER_DAILY_HABITS_COMPLETED, "INTEGER DEFAULT 0");
        addColumnIfNotExists(db, TABLE_USERS, COLUMN_USER_LAST_ACTIVITY_DATE, "INTEGER DEFAULT 0");
    }

    protected void addColumnIfNotExists(SQLiteDatabase db, String table, String column, String type) {
        try {
            // Verificar si la columna ya existe antes de intentar agregarla
            Cursor cursor = db.rawQuery("PRAGMA table_info(" + table + ")", null);
            if (cursor != null) {
                int columnIndex = cursor.getColumnIndex("name");
                if (columnIndex >= 0) {
                    while (cursor.moveToNext()) {
                        String existingColumn = cursor.getString(columnIndex);
                        if (column.equals(existingColumn)) {
                            cursor.close();
                            return; // La columna ya existe
                        }
                    }
                }
                cursor.close();
            }
            // Si llegamos aquí, la columna no existe, así que la agregamos
            db.execSQL("ALTER TABLE " + table + " ADD COLUMN " + column + " " + type);
        } catch (Exception e) {
            // Si la columna ya existe o hay otro error, registrar pero no fallar
            android.util.Log.w("HabitDatabaseHelper", "Error al agregar columna " + column + ": " + e.getMessage());
        }
    }

    // ========== CRUD USUARIOS ==========

    public long createUser(String email, String passwordHash, String firstName, String lastName, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_EMAIL, email);
        values.put(COLUMN_USER_PASSWORD_HASH, passwordHash);
        values.put(COLUMN_USER_FIRST_NAME, firstName);
        values.put(COLUMN_USER_LAST_NAME, lastName);
        values.put(COLUMN_USER_PHONE, phone);
        values.put(COLUMN_USER_CREATED_AT, System.currentTimeMillis());
        values.put(COLUMN_USER_IS_ACTIVE, 1);
        long id = db.insert(TABLE_USERS, null, values);
        db.close();
        return id;
    }

    public com.tuempresa.proyecto_01_11_25.model.User getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null, COLUMN_USER_EMAIL + "=?", new String[] { email }, null, null, null);
        com.tuempresa.proyecto_01_11_25.model.User user = null;
        if (cursor.moveToFirst()) {
            user = new com.tuempresa.proyecto_01_11_25.model.User();
            user.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)));
            user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_EMAIL)));
            user.setPasswordHash(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_PASSWORD_HASH)));

            int firstNameIndex = cursor.getColumnIndex(COLUMN_USER_FIRST_NAME);
            if (firstNameIndex >= 0 && !cursor.isNull(firstNameIndex))
                user.setFirstName(cursor.getString(firstNameIndex));

            int lastNameIndex = cursor.getColumnIndex(COLUMN_USER_LAST_NAME);
            if (lastNameIndex >= 0 && !cursor.isNull(lastNameIndex))
                user.setLastName(cursor.getString(lastNameIndex));

            int phoneIndex = cursor.getColumnIndex(COLUMN_USER_PHONE);
            if (phoneIndex >= 0 && !cursor.isNull(phoneIndex))
                user.setPhone(cursor.getString(phoneIndex));

            user.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_USER_CREATED_AT)));
            user.setActive(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_IS_ACTIVE)) == 1);
        }
        cursor.close();
        db.close();
        return user;
    }

    /**
     * Carga los campos adicionales de un hábito desde el cursor
     */
    private void loadHabitExtraFields(Cursor cursor, Habit habit) {
        try {
            // CRÍTICO: Cargar userId desde la BD
            int userIdIndex = cursor.getColumnIndex(COLUMN_HABIT_USER_ID);
            if (userIdIndex >= 0 && !cursor.isNull(userIdIndex)) {
                long userId = cursor.getLong(userIdIndex);
                habit.setUserId(userId);
                android.util.Log.d("HabitDatabaseHelper", "✅ userId cargado desde BD: " + userId + " para hábito: " + habit.getTitle());
            } else {
                android.util.Log.w("HabitDatabaseHelper", "⚠️ No se encontró columna user_id en cursor para hábito: " + habit.getTitle());
            }
            
            // Cargar points
            int pointsIndex = cursor.getColumnIndex(COLUMN_HABIT_POINTS);
            if (pointsIndex >= 0 && !cursor.isNull(pointsIndex)) {
                habit.setPoints(cursor.getInt(pointsIndex));
            }

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

            int walkGoalMetersIndex = cursor.getColumnIndex(COLUMN_HABIT_WALK_GOAL_METERS);
            if (walkGoalMetersIndex >= 0 && !cursor.isNull(walkGoalMetersIndex)) {
                habit.setWalkGoalMeters(cursor.getInt(walkGoalMetersIndex));
            }

            int walkGoalStepsIndex = cursor.getColumnIndex(COLUMN_HABIT_WALK_GOAL_STEPS);
            if (walkGoalStepsIndex >= 0 && !cursor.isNull(walkGoalStepsIndex)) {
                habit.setWalkGoalSteps(cursor.getInt(walkGoalStepsIndex));
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

            int streakIndex = cursor.getColumnIndex(COLUMN_HABIT_STREAK_COUNT);
            if (streakIndex >= 0 && !cursor.isNull(streakIndex)) {
                habit.setStreakCount(cursor.getInt(streakIndex));
            }

            int lastCompletedIndex = cursor.getColumnIndex(COLUMN_HABIT_LAST_COMPLETED_DATE);
            if (lastCompletedIndex >= 0 && !cursor.isNull(lastCompletedIndex)) {
                habit.setLastCompletedDate(cursor.getLong(lastCompletedIndex));
            }
        } catch (Exception e) {
            // Si las columnas no existen, ignorar el error
        }
    }

    // ========== CRUD HÁBITOS ==========

    public long insertHabit(String title, String goal, String category, String type, int points) {
        return insertHabit(title, goal, category, type, points, 0.0, null);
    }

    public long insertHabit(String title, String goal, String category, String type, int points, double targetValue,
            String targetUnit) {
        return insertHabitFull(title, goal, category, type, points, targetValue != 0.0 ? (Double) targetValue : null,
                targetUnit, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    /**
     * Inserta un hábito con todos los campos opcionales
     */
    // Helper para obtener el userId actual
    protected long getCurrentUserId() {
        com.tuempresa.proyecto_01_11_25.utils.SessionManager session = new com.tuempresa.proyecto_01_11_25.utils.SessionManager(
                context);
        return session.getUserId();
    }

    public List<Habit> getAllHabits() {
        long userId = getCurrentUserId();
        List<Habit> habits = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        // Filtrar por user_id
        Cursor cursor = db.query(TABLE_HABITS, null, COLUMN_HABIT_USER_ID + "=?",
                new String[] { String.valueOf(userId) }, null, null, COLUMN_HABIT_CREATED_AT + " DESC");

        if (cursor.moveToFirst()) {
            do {
                Habit habit = new Habit(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HABIT_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HABIT_GOAL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HABIT_CATEGORY)),
                        Habit.HabitType.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HABIT_TYPE))));
                habit.setCompleted(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_HABIT_COMPLETED)) == 1);
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_HABIT_ID));
                habit.setId(id);
                loadHabitExtraFields(cursor, habit);
                habits.add(habit);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return habits;
    }

    public long insertHabitFull(String title, String goal, String category, String type, int points,
            Double targetValue, String targetUnit,
            Integer pagesPerDay, String reminderTimes, Integer durationMinutes,
            Boolean dndMode, Integer musicId, Boolean journalEnabled,
            String gymDays, Integer waterGoalGlasses, Integer walkGoalMeters, Integer walkGoalSteps,
            Boolean oneClickComplete, Boolean englishMode, Boolean codingMode, String habitIcon) {
        long userId = getCurrentUserId();
        
        // CRÍTICO: Verificar que el userId sea válido antes de guardar
        if (userId <= 0) {
            android.util.Log.e("HabitDatabaseHelper", "⚠️ ERROR CRÍTICO: No se puede crear hábito con userId inválido: " + userId);
            throw new IllegalStateException("No se puede crear hábito: userId inválido (" + userId + ")");
        }
        
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_HABIT_USER_ID, userId); // Asignar al usuario actual
        android.util.Log.d("HabitDatabaseHelper", "Creando hábito '" + title + "' con userId: " + userId);
        values.put(COLUMN_HABIT_TITLE, title);
        values.put(COLUMN_HABIT_GOAL, goal);
        values.put(COLUMN_HABIT_CATEGORY, category);
        values.put(COLUMN_HABIT_TYPE, type);
        values.put(COLUMN_HABIT_POINTS, points);
        values.put(COLUMN_HABIT_POINTS_PER_COMPLETION, points);

        if (targetValue != null)
            values.put(COLUMN_HABIT_TARGET_VALUE, targetValue);
        if (targetUnit != null)
            values.put(COLUMN_HABIT_TARGET_UNIT, targetUnit);
        if (pagesPerDay != null)
            values.put(COLUMN_HABIT_PAGES_PER_DAY, pagesPerDay);
        if (reminderTimes != null)
            values.put(COLUMN_HABIT_REMINDER_TIMES, reminderTimes);
        if (durationMinutes != null)
            values.put(COLUMN_HABIT_DURATION_MINUTES, durationMinutes);
        if (dndMode != null)
            values.put(COLUMN_HABIT_DND_MODE, dndMode ? 1 : 0);
        if (musicId != null)
            values.put(COLUMN_HABIT_MUSIC_ID, musicId);
        if (journalEnabled != null)
            values.put(COLUMN_HABIT_JOURNAL_ENABLED, journalEnabled ? 1 : 0);
        if (gymDays != null)
            values.put(COLUMN_HABIT_GYM_DAYS, gymDays);
        if (waterGoalGlasses != null)
            values.put(COLUMN_HABIT_WATER_GOAL_GLASSES, waterGoalGlasses);
        if (walkGoalMeters != null)
            values.put(COLUMN_HABIT_WALK_GOAL_METERS, walkGoalMeters);
        if (walkGoalSteps != null)
            values.put(COLUMN_HABIT_WALK_GOAL_STEPS, walkGoalSteps);
        if (oneClickComplete != null)
            values.put(COLUMN_HABIT_ONE_CLICK_COMPLETE, oneClickComplete ? 1 : 0);
        if (englishMode != null)
            values.put(COLUMN_HABIT_ENGLISH_MODE, englishMode ? 1 : 0);
        if (codingMode != null)
            values.put(COLUMN_HABIT_CODING_MODE, codingMode ? 1 : 0);
        if (habitIcon != null)
            values.put(COLUMN_HABIT_ICON, habitIcon);

        long id = db.insert(TABLE_HABITS, null, values);
        db.close();
        return id;
    }

    public long addScore(String habitTitle, int points) {
        long userId = getCurrentUserId();
        if (userId <= 0) {
            android.util.Log.e("HabitDatabaseHelper", "⚠️ No se puede guardar score: userId inválido (" + userId + ")");
            return -1;
        }
        
        SQLiteDatabase db = this.getWritableDatabase();
        
        // Verificar si ya existe un score para este hábito hoy (evitar duplicados)
        long todayTimestamp = getTodayTimestamp();
        Cursor cursor = db.query(TABLE_SCORES, new String[]{COLUMN_SCORE_ID},
                COLUMN_SCORE_USER_ID + "=? AND " + COLUMN_SCORE_HABIT_TITLE + "=? AND " + COLUMN_SCORE_DATE + ">=?",
                new String[]{String.valueOf(userId), habitTitle, String.valueOf(todayTimestamp)},
                null, null, null);
        
        boolean scoreExistsToday = cursor.getCount() > 0;
        cursor.close();
        
        if (scoreExistsToday) {
            android.util.Log.d("HabitDatabaseHelper", "ℹ️ Score para hábito '" + habitTitle + "' ya existe hoy. No se guarda duplicado.");
            db.close();
            return -1;
        }
        
        ContentValues values = new ContentValues();
        values.put(COLUMN_SCORE_USER_ID, userId); // Asignar al usuario actual
        values.put(COLUMN_SCORE_HABIT_TITLE, habitTitle);
        values.put(COLUMN_SCORE_POINTS, points);
        values.put(COLUMN_SCORE_DATE, System.currentTimeMillis() / 1000); // Fecha actual
        long id = db.insert(TABLE_SCORES, null, values);
        db.close();
        
        if (id > 0) {
            android.util.Log.d("HabitDatabaseHelper", "✅ Score guardado: " + points + " puntos para hábito '" + habitTitle + "' (usuario " + userId + ", scoreId: " + id + ")");
        } else {
            android.util.Log.e("HabitDatabaseHelper", "⚠️ Error al guardar score para hábito '" + habitTitle + "'");
        }
        
        return id;
    }

    public int getTotalScore() {
        long userId = getCurrentUserId();
        SQLiteDatabase db = this.getReadableDatabase();
        // Filtrar suma por usuario
        Cursor cursor = db.rawQuery("SELECT SUM(" + COLUMN_SCORE_POINTS + ") as total FROM " + TABLE_SCORES + " WHERE "
                + COLUMN_SCORE_USER_ID + "=?", new String[] { String.valueOf(userId) });

        int total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getInt(cursor.getColumnIndexOrThrow("total"));
        }
        cursor.close();
        db.close();
        return total;
    }

    public List<ScoreEntry> getAllScores() {
        long userId = getCurrentUserId();
        List<ScoreEntry> scores = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        // Filtrar por usuario
        Cursor cursor = db.query(TABLE_SCORES, null, COLUMN_SCORE_USER_ID + "=?",
                new String[] { String.valueOf(userId) }, null, null, COLUMN_SCORE_DATE + " DESC");

        if (cursor.moveToFirst()) {
            do {
                ScoreEntry entry = new ScoreEntry(
                        cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_SCORE_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SCORE_HABIT_TITLE)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SCORE_POINTS)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_SCORE_DATE)));
                scores.add(entry);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return scores;
    }

    public long getHabitIdByTitle(String title) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_HABITS, new String[] { COLUMN_HABIT_ID },
                COLUMN_HABIT_TITLE + "=?", new String[] { title }, null, null, null);

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
                new String[] { String.valueOf(id) }, null, null, null);

        Habit habit = null;
        if (cursor.moveToFirst()) {
            habit = new Habit(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HABIT_TITLE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HABIT_GOAL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HABIT_CATEGORY)),
                    Habit.HabitType.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HABIT_TYPE))));
            habit.setId(id);
            habit.setCompleted(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_HABIT_COMPLETED)) == 1);
            loadHabitExtraFields(cursor, habit);
        }
        cursor.close();
        db.close();
        return habit;
    }

    public boolean updateHabit(long id, String title, String goal, String category, String type, int points) {
        return updateHabit(id, title, goal, category, type, points, 0.0, null);
    }

    public boolean updateHabit(long id, String title, String goal, String category, String type, int points,
            double targetValue, String targetUnit) {
        return updateHabitFull(id, title, goal, category, type, points,
                targetValue != 0.0 ? (Double) targetValue : null, targetUnit, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null);
    }

    public boolean updateHabitFull(long id, String title, String goal, String category, String type, int points,
            Double targetValue, String targetUnit,
            Integer pagesPerDay, String reminderTimes, Integer durationMinutes,
            Boolean dndMode, Integer musicId, Boolean journalEnabled,
            String gymDays, Integer waterGoalGlasses, Integer walkGoalMeters, Integer walkGoalSteps,
            Boolean oneClickComplete, Boolean englishMode, Boolean codingMode, String habitIcon) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_HABIT_TITLE, title);
        values.put(COLUMN_HABIT_GOAL, goal);
        values.put(COLUMN_HABIT_CATEGORY, category);
        values.put(COLUMN_HABIT_TYPE, type);
        values.put(COLUMN_HABIT_POINTS, points);

        if (targetValue != null)
            values.put(COLUMN_HABIT_TARGET_VALUE, targetValue);
        if (targetUnit != null)
            values.put(COLUMN_HABIT_TARGET_UNIT, targetUnit);
        if (pagesPerDay != null)
            values.put(COLUMN_HABIT_PAGES_PER_DAY, pagesPerDay);
        if (reminderTimes != null)
            values.put(COLUMN_HABIT_REMINDER_TIMES, reminderTimes);
        if (durationMinutes != null)
            values.put(COLUMN_HABIT_DURATION_MINUTES, durationMinutes);
        if (dndMode != null)
            values.put(COLUMN_HABIT_DND_MODE, dndMode ? 1 : 0);
        if (musicId != null)
            values.put(COLUMN_HABIT_MUSIC_ID, musicId);
        if (journalEnabled != null)
            values.put(COLUMN_HABIT_JOURNAL_ENABLED, journalEnabled ? 1 : 0);
        if (gymDays != null)
            values.put(COLUMN_HABIT_GYM_DAYS, gymDays);
        if (waterGoalGlasses != null)
            values.put(COLUMN_HABIT_WATER_GOAL_GLASSES, waterGoalGlasses);
        if (walkGoalMeters != null)
            values.put(COLUMN_HABIT_WALK_GOAL_METERS, walkGoalMeters);
        if (walkGoalSteps != null)
            values.put(COLUMN_HABIT_WALK_GOAL_STEPS, walkGoalSteps);
        if (oneClickComplete != null)
            values.put(COLUMN_HABIT_ONE_CLICK_COMPLETE, oneClickComplete ? 1 : 0);
        if (englishMode != null)
            values.put(COLUMN_HABIT_ENGLISH_MODE, englishMode ? 1 : 0);
        if (codingMode != null)
            values.put(COLUMN_HABIT_CODING_MODE, codingMode ? 1 : 0);
        if (habitIcon != null)
            values.put(COLUMN_HABIT_ICON, habitIcon);

        int rowsAffected = db.update(TABLE_HABITS, values, COLUMN_HABIT_ID + "=?", new String[] { String.valueOf(id) });
        db.close();
        return rowsAffected > 0;
    }

    public boolean deleteHabit(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        // Verificar si el hábito existe antes de eliminarlo
        Cursor cursor = db.query(TABLE_HABITS, new String[]{COLUMN_HABIT_TITLE, COLUMN_HABIT_USER_ID},
                COLUMN_HABIT_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        
        String habitTitle = "desconocido";
        long userId = 0;
        if (cursor.moveToFirst()) {
            habitTitle = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HABIT_TITLE));
            userId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_HABIT_USER_ID));
        }
        cursor.close();
        
        int rowsAffected = db.delete(TABLE_HABITS, COLUMN_HABIT_ID + "=?", new String[] { String.valueOf(id) });
        db.close();
        
        if (rowsAffected > 0) {
            android.util.Log.d("HabitDatabaseHelper", "✅ Hábito eliminado localmente: '" + habitTitle + "' (id: " + id + ", userId: " + userId + ")");
        } else {
            android.util.Log.w("HabitDatabaseHelper", "⚠️ No se pudo eliminar hábito (id: " + id + "). Puede que no exista.");
        }
        
        return rowsAffected > 0;
    }

    public void updateHabitCompleted(String title, boolean completed) {
        long currentUserId = getCurrentUserId();
        if (currentUserId <= 0) {
            android.util.Log.e("HabitDatabaseHelper", "⚠️ No se puede actualizar hábito: userId inválido (" + currentUserId + ")");
            return;
        }
        
        SQLiteDatabase db = this.getWritableDatabase();
        
        // Verificar el estado actual del hábito antes de actualizar
        // IMPORTANTE: Filtrar por userId para evitar problemas al cambiar de cuenta
        Cursor cursor = db.query(TABLE_HABITS, new String[]{COLUMN_HABIT_COMPLETED, COLUMN_HABIT_USER_ID, COLUMN_HABIT_ID},
                COLUMN_HABIT_TITLE + "=? AND " + COLUMN_HABIT_USER_ID + "=?", 
                new String[]{title, String.valueOf(currentUserId)}, null, null, null);
        
        boolean wasAlreadyCompleted = false;
        long userId = 0;
        long habitId = 0;
        
        if (cursor.moveToFirst()) {
            wasAlreadyCompleted = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_HABIT_COMPLETED)) == 1;
            userId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_HABIT_USER_ID));
            habitId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_HABIT_ID));
        } else {
            // Si no se encontró el hábito, puede ser que pertenezca a otro usuario
            // o que no exista. En este caso, no hacemos nada.
            android.util.Log.w("HabitDatabaseHelper", "⚠️ Hábito '" + title + "' no encontrado para usuario " + currentUserId);
            cursor.close();
            db.close();
            return;
        }
        cursor.close();
        
        // Actualizar el estado del hábito (solo para el usuario actual)
        ContentValues values = new ContentValues();
        values.put(COLUMN_HABIT_COMPLETED, completed ? 1 : 0);
        int rowsUpdated = db.update(TABLE_HABITS, values, 
                COLUMN_HABIT_TITLE + "=? AND " + COLUMN_HABIT_USER_ID + "=?", 
                new String[] { title, String.valueOf(currentUserId) });
        db.close();
        
        if (rowsUpdated == 0) {
            android.util.Log.w("HabitDatabaseHelper", "⚠️ No se pudo actualizar hábito '" + title + "' para usuario " + currentUserId);
            return;
        }
        
        // CRÍTICO: Si se completó un hábito Y no estaba ya completado antes, incrementar contador diario para la racha
        // IMPORTANTE: Incrementamos el contador cuando se marca como completado, independientemente de si ya existe un score
        // El score se guarda después, y addScore() previene duplicados. Esto asegura que el contador se incremente correctamente.
        if (completed && !wasAlreadyCompleted) {
            // Usar el userId del hábito o el usuario actual
            if (userId <= 0) {
                userId = currentUserId;
            }
            
            if (userId > 0 && userId == currentUserId) {
                android.util.Log.d("HabitDatabaseHelper", "✅ Hábito '" + title + "' completado (nuevo). Incrementando contador diario para usuario " + userId);
                incrementDailyHabitCompleted(userId);
            } else {
                android.util.Log.e("HabitDatabaseHelper", "⚠️ No se pudo obtener userId válido para incrementar contador diario. Hábito: " + title + ", userId: " + userId + ", currentUserId: " + currentUserId);
            }
        } else if (completed && wasAlreadyCompleted) {
            android.util.Log.d("HabitDatabaseHelper", "ℹ️ Hábito '" + title + "' ya estaba completado. No se incrementa el contador (evita duplicados).");
        }
    }
    
    /**
     * Verifica si ya existe un score para un hábito hoy
     */
    private boolean checkScoreExistsToday(long habitId, String habitTitle, long userId) {
        long todayTimestamp = getTodayTimestamp();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SCORES, new String[]{COLUMN_SCORE_ID},
                COLUMN_SCORE_USER_ID + "=? AND " + COLUMN_SCORE_HABIT_TITLE + "=? AND " + COLUMN_SCORE_DATE + ">=?",
                new String[]{String.valueOf(userId), habitTitle, String.valueOf(todayTimestamp)},
                null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    public int getHabitPoints(String title) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_HABITS, new String[] { COLUMN_HABIT_POINTS },
                COLUMN_HABIT_TITLE + "=?", new String[] { title }, null, null, null);

        int points = 10; // default
        if (cursor.moveToFirst()) {
            points = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_HABIT_POINTS));
        }
        cursor.close();
        db.close();
        return points;
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

        public long getId() {
            return id;
        }

        public String getHabitTitle() {
            return habitTitle;
        }

        public int getPoints() {
            return points;
        }

        public long getDate() {
            return date;
        }
    }

    /**
     * Elimina un usuario y todos sus datos asociados (hábitos y puntajes)
     * 
     * @param userId ID del usuario a eliminar
     * @return true si se eliminó correctamente
     */
    /**
     * Elimina todos los hábitos que NO pertenecen al usuario actual.
     * Útil cuando un usuario nuevo inicia sesión para limpiar datos de pruebas anteriores.
     * 
     * CRÍTICO: NO elimina hábitos con serverId válido aunque tengan userId: 0,
     * porque estos se corregirán después en upsertHabitFromServer.
     */
    public void deleteHabitsNotBelongingToCurrentUser() {
        long currentUserId = getCurrentUserId();
        
        // CRÍTICO: No eliminar si no hay usuario logueado o userId inválido
        if (currentUserId <= 0) {
            android.util.Log.w("HabitDatabaseHelper", "⚠️ No se puede eliminar hábitos: userId inválido (" + currentUserId + ")");
            return;
        }
        
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            // CRÍTICO: NO eliminar hábitos con serverId válido aunque tengan userId: 0
            // Estos hábitos se corregirán después en upsertHabitFromServer
            // Solo eliminar hábitos que:
            // 1. No tienen serverId Y (userId != currentUserId OR userId IS NULL OR userId = 0)
            // 2. Tienen serverId Y userId != currentUserId Y userId > 0 (hábitos de otros usuarios con serverId)
            
            // Primero, verificar si existe la columna server_id (para compatibilidad con versiones antiguas)
            boolean hasServerIdColumn = false;
            try {
                android.database.Cursor cursor = db.rawQuery("PRAGMA table_info(" + TABLE_HABITS + ")", null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        String columnName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                        if ("server_id".equals(columnName)) {
                            hasServerIdColumn = true;
                            break;
                        }
                    }
                    cursor.close();
                }
            } catch (Exception e) {
                android.util.Log.w("HabitDatabaseHelper", "No se pudo verificar columna server_id, asumiendo que no existe", e);
            }
            
            if (hasServerIdColumn) {
                // Eliminar hábitos que NO tienen serverId Y no pertenecen al usuario actual
                // O hábitos que tienen serverId Y pertenecen a otro usuario (userId > 0 y != currentUserId)
                String deleteQuery = "DELETE FROM " + TABLE_HABITS + " WHERE " +
                    "((server_id IS NULL OR server_id = 0) AND " +
                    "(user_id != ? OR user_id IS NULL OR user_id = 0)) OR " +
                    "(server_id IS NOT NULL AND server_id > 0 AND user_id != ? AND user_id > 0)";
                db.execSQL(deleteQuery, new String[]{String.valueOf(currentUserId), String.valueOf(currentUserId)});
                android.util.Log.d("HabitDatabaseHelper", "Eliminados hábitos que no pertenecen al usuario " + currentUserId + 
                    " (preservando hábitos con serverId válido aunque tengan userId: 0)");
            } else {
                // Si no existe la columna server_id, usar la lógica antigua
                int deletedCount = db.delete(TABLE_HABITS, 
                        COLUMN_HABIT_USER_ID + "!=? OR " + COLUMN_HABIT_USER_ID + " IS NULL OR " + COLUMN_HABIT_USER_ID + "=0", 
                        new String[] { String.valueOf(currentUserId) });
                android.util.Log.d("HabitDatabaseHelper", "Eliminados " + deletedCount + " hábitos que no pertenecen al usuario " + currentUserId);
            }
            
            // También eliminar scores que no pertenecen al usuario actual
            int deletedScores = db.delete(TABLE_SCORES, 
                    COLUMN_SCORE_USER_ID + "!=? OR " + COLUMN_SCORE_USER_ID + " IS NULL OR " + COLUMN_SCORE_USER_ID + "=0", 
                    new String[] { String.valueOf(currentUserId) });
            android.util.Log.d("HabitDatabaseHelper", "Eliminados " + deletedScores + " scores que no pertenecen al usuario " + currentUserId);
            
            // También eliminar entradas de diario que no pertenecen al usuario actual
            int deletedDiaryEntries = db.delete(TABLE_DIARY_ENTRIES,
                    COLUMN_DIARY_USER_ID + "!=? OR " + COLUMN_DIARY_USER_ID + " IS NULL OR " + COLUMN_DIARY_USER_ID + "=0",
                    new String[] { String.valueOf(currentUserId) });
            android.util.Log.d("HabitDatabaseHelper", "Eliminadas " + deletedDiaryEntries + " entradas de diario que no pertenecen al usuario " + currentUserId);
        } catch (Exception e) {
            android.util.Log.e("HabitDatabaseHelper", "Error al eliminar hábitos de otros usuarios", e);
        } finally {
            db.close();
        }
    }

    public boolean deleteUser(long userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.beginTransaction();

            // Eliminar todos los amigos del usuario (relaciones donde el usuario es el propietario)
            db.delete(TABLE_FRIENDS, COLUMN_FRIEND_USER_ID + "=?", new String[] { String.valueOf(userId) });

            // Eliminar todos los hábitos del usuario
            db.delete(TABLE_HABITS, COLUMN_HABIT_USER_ID + "=?", new String[] { String.valueOf(userId) });

            // Eliminar todos los puntajes del usuario
            db.delete(TABLE_SCORES, COLUMN_SCORE_USER_ID + "=?", new String[] { String.valueOf(userId) });

            // Eliminar entradas de diario del usuario
            db.delete(TABLE_DIARY_ENTRIES, COLUMN_DIARY_USER_ID + "=?", new String[] { String.valueOf(userId) });

            // Eliminar el usuario
            int rowsAffected = db.delete(TABLE_USERS, COLUMN_USER_ID + "=?", new String[] { String.valueOf(userId) });

            db.setTransactionSuccessful();
            android.util.Log.d("HabitDatabaseHelper", "Usuario " + userId + " eliminado correctamente con todos sus datos relacionados");
            return rowsAffected > 0;
        } catch (Exception e) {
            android.util.Log.e("HabitDatabaseHelper", "Error al eliminar usuario " + userId, e);
            e.printStackTrace();
            return false;
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public List<com.tuempresa.proyecto_01_11_25.model.UserRanking> getUsersRanking() {
        List<com.tuempresa.proyecto_01_11_25.model.UserRanking> ranking = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Query compleja para obtener ranking
        String query = "SELECT " +
                "u." + COLUMN_USER_EMAIL + ", " +
                "u." + COLUMN_USER_FIRST_NAME + ", " +
                "IFNULL(SUM(s." + COLUMN_SCORE_POINTS + "), 0) as total_score, " +
                "(SELECT COUNT(*) FROM " + TABLE_HABITS + " h WHERE h." + COLUMN_HABIT_USER_ID + " = u."
                + COLUMN_USER_ID + " AND h." + COLUMN_HABIT_COMPLETED + " = 1) as habits_completed " +
                "FROM " + TABLE_USERS + " u " +
                "LEFT JOIN " + TABLE_SCORES + " s ON u." + COLUMN_USER_ID + " = s." + COLUMN_SCORE_USER_ID + " " +
                "GROUP BY u." + COLUMN_USER_ID + " " +
                "ORDER BY total_score DESC";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                String email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_EMAIL));
                String firstName = "";
                int firstNameIndex = cursor.getColumnIndex(COLUMN_USER_FIRST_NAME);
                if (firstNameIndex >= 0 && !cursor.isNull(firstNameIndex)) {
                    firstName = cursor.getString(firstNameIndex);
                }

                String displayName = (firstName != null && !firstName.isEmpty()) ? firstName : email;

                int totalScore = cursor.getInt(cursor.getColumnIndexOrThrow("total_score"));
                int habitsCompleted = cursor.getInt(cursor.getColumnIndexOrThrow("habits_completed"));

                ranking.add(new com.tuempresa.proyecto_01_11_25.model.UserRanking(displayName, totalScore,
                        habitsCompleted));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return ranking;
    }

    // ========== CRUD ENTRADAS DEL DIARIO ==========

    /**
     * Guarda una entrada del diario asociada a un hábito
     */
    public long saveDiaryEntry(long habitId, String content) {
        return saveDiaryEntry(habitId, content, null);
    }

    /**
     * Guarda una entrada del diario con título.
     * Si title es null, genera uno automático basado en la fecha.
     */
    public long saveDiaryEntry(long habitId, String content, String title) {
        long userId = getCurrentUserId();
        
        // Generar título automático si no se proporciona
        if (title == null || title.trim().isEmpty()) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
            title = "Nota - " + sdf.format(new java.util.Date());
        }
        
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DIARY_HABIT_ID, habitId);
        values.put(COLUMN_DIARY_USER_ID, userId);
        values.put(COLUMN_DIARY_TITLE, title);
        values.put(COLUMN_DIARY_CONTENT, content);
        values.put(COLUMN_DIARY_DATE, System.currentTimeMillis() / 1000);
        values.put(COLUMN_DIARY_CREATED_AT, System.currentTimeMillis() / 1000);
        
        long id = db.insert(TABLE_DIARY_ENTRIES, null, values);
        db.close();
        return id;
    }

    /**
     * Obtiene todas las entradas del diario para un hábito específico
     */
    public List<DiaryEntry> getDiaryEntriesByHabit(long habitId) {
        long userId = getCurrentUserId();
        List<DiaryEntry> entries = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_DIARY_ENTRIES, null,
                COLUMN_DIARY_HABIT_ID + "=? AND " + COLUMN_DIARY_USER_ID + "=?",
                new String[] { String.valueOf(habitId), String.valueOf(userId) },
                null, null, COLUMN_DIARY_DATE + " DESC");

        if (cursor.moveToFirst()) {
            do {
                DiaryEntry entry = new DiaryEntry();
                entry.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_DIARY_ID)));
                entry.setHabitId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_DIARY_HABIT_ID)));
                int userIdIndex = cursor.getColumnIndex(COLUMN_DIARY_USER_ID);
                if (userIdIndex >= 0) {
                    entry.setUserId(cursor.getLong(userIdIndex));
                }
                int titleIndex = cursor.getColumnIndex(COLUMN_DIARY_TITLE);
                if (titleIndex >= 0 && !cursor.isNull(titleIndex)) {
                    entry.setTitle(cursor.getString(titleIndex));
                } else {
                    // Generar título por defecto si no existe
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
                    entry.setTitle("Nota - " + sdf.format(new java.util.Date(entry.getDate() * 1000)));
                }
                entry.setContent(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DIARY_CONTENT)));
                entry.setDate(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_DIARY_DATE)));
                entry.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_DIARY_CREATED_AT)));
                entries.add(entry);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return entries;
    }

    /**
     * Obtiene todas las entradas del diario del usuario
     */
    public List<DiaryEntry> getAllDiaryEntries() {
        long userId = getCurrentUserId();
        List<DiaryEntry> entries = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_DIARY_ENTRIES, null,
                COLUMN_DIARY_USER_ID + "=?",
                new String[] { String.valueOf(userId) },
                null, null, COLUMN_DIARY_DATE + " DESC");

        if (cursor.moveToFirst()) {
            do {
                DiaryEntry entry = new DiaryEntry();
                entry.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_DIARY_ID)));
                entry.setHabitId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_DIARY_HABIT_ID)));
                int userIdIndex = cursor.getColumnIndex(COLUMN_DIARY_USER_ID);
                if (userIdIndex >= 0) {
                    entry.setUserId(cursor.getLong(userIdIndex));
                }
                int titleIndex = cursor.getColumnIndex(COLUMN_DIARY_TITLE);
                if (titleIndex >= 0 && !cursor.isNull(titleIndex)) {
                    entry.setTitle(cursor.getString(titleIndex));
                } else {
                    // Generar título por defecto si no existe
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
                    entry.setTitle("Nota - " + sdf.format(new java.util.Date(entry.getDate() * 1000)));
                }
                entry.setContent(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DIARY_CONTENT)));
                entry.setDate(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_DIARY_DATE)));
                entry.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_DIARY_CREATED_AT)));
                entries.add(entry);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return entries;
    }

    /**
     * Actualiza una entrada del diario
     */
    public boolean updateDiaryEntry(long entryId, String content) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DIARY_CONTENT, content);
        int rowsAffected = db.update(TABLE_DIARY_ENTRIES, values, COLUMN_DIARY_ID + "=?",
                new String[] { String.valueOf(entryId) });
        db.close();
        return rowsAffected > 0;
    }

    /**
     * Elimina una entrada del diario
     */
    public boolean deleteDiaryEntry(long entryId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_DIARY_ENTRIES, COLUMN_DIARY_ID + "=?",
                new String[] { String.valueOf(entryId) });
        db.close();
        return rowsAffected > 0;
    }

    /**
     * Clase para representar una entrada del diario
     */
    public static class DiaryEntry {
        private long id;
        private long userId = -1; // ID del usuario (requerido por API)
        private long habitId;
        private String title = ""; // Título (requerido por API)
        private String content;
        private long date;
        private long createdAt;

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

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public long getDate() {
            return date;
        }

        public void setDate(long date) {
            this.date = date;
        }

        public long getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(long createdAt) {
            this.createdAt = createdAt;
        }
    }

    // ========== GESTIÓN DE RACHA DIARIA ==========

    /**
     * Obtiene la fecha actual sin hora (solo día) en timestamp
     */
    private long getTodayTimestamp() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis() / 1000;
    }

    /**
     * Resetea todos los hábitos completados al inicio del día
     * Esto permite que los usuarios completen sus hábitos nuevamente cada día
     * Solo resetea si es un nuevo día (después de las 00:00)
     */
    public void resetDailyCompletedHabits() {
        long currentUserId = getCurrentUserId();
        if (currentUserId <= 0) {
            return;
        }
        
        // Verificar si es un nuevo día
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_USER_LAST_ACTIVITY_DATE},
                COLUMN_USER_ID + "=?", new String[]{String.valueOf(currentUserId)}, null, null, null);
        
        long todayTimestamp = getTodayTimestamp();
        boolean shouldReset = false;
        
        if (cursor.moveToFirst()) {
            long lastActivityDate = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_USER_LAST_ACTIVITY_DATE));
            // Si la última actividad fue antes de hoy, resetear hábitos
            if (lastActivityDate < todayTimestamp) {
                shouldReset = true;
            }
        } else {
            // Si no hay registro de actividad, resetear (primera vez)
            shouldReset = true;
        }
        cursor.close();
        db.close();
        
        if (!shouldReset) {
            android.util.Log.d("HabitDatabaseHelper", "ℹ️ No es necesario resetear hábitos (mismo día)");
            return;
        }
        
        // Resetear hábitos completados
        db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_HABIT_COMPLETED, 0);
        
        // Resetear solo hábitos del usuario actual
        int rowsUpdated = db.update(TABLE_HABITS, values, 
                COLUMN_HABIT_USER_ID + "=?", 
                new String[]{String.valueOf(currentUserId)});
        db.close();
        
        if (rowsUpdated > 0) {
            android.util.Log.d("HabitDatabaseHelper", "✅ " + rowsUpdated + " hábitos reseteados para el nuevo día (usuario " + currentUserId + ")");
        }
    }

    /**
     * Verifica si es un nuevo día (después de las 00:00)
     * y resetea el contador diario si es necesario
     */
    private void checkAndResetDailyCounter(long userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        // Asegurar que las columnas de racha existan antes de usarlas
        // Esto es necesario porque bases de datos antiguas pueden no tenerlas
        addColumnIfNotExists(db, TABLE_USERS, COLUMN_USER_CURRENT_STREAK, "INTEGER DEFAULT 0");
        addColumnIfNotExists(db, TABLE_USERS, COLUMN_USER_LAST_STREAK_DATE, "INTEGER DEFAULT 0");
        addColumnIfNotExists(db, TABLE_USERS, COLUMN_USER_DAILY_HABITS_COMPLETED, "INTEGER DEFAULT 0");
        addColumnIfNotExists(db, TABLE_USERS, COLUMN_USER_LAST_ACTIVITY_DATE, "INTEGER DEFAULT 0");
        
        long todayTimestamp = getTodayTimestamp();
        
        // CRÍTICO: Verificar si el usuario existe, si no, crearlo
        Cursor userCheck = db.query(TABLE_USERS, new String[]{COLUMN_USER_ID},
                COLUMN_USER_ID + "=?", new String[]{String.valueOf(userId)}, null, null, null);
        boolean userExists = userCheck.moveToFirst();
        userCheck.close();
        
        if (!userExists) {
            // Crear usuario si no existe (usando datos de SessionManager)
            com.tuempresa.proyecto_01_11_25.utils.SessionManager session = new com.tuempresa.proyecto_01_11_25.utils.SessionManager(context);
            String userEmail = session.getUserEmail();
            if (userEmail == null || userEmail.isEmpty()) {
                userEmail = "user" + userId + "@local.com";
            }
            
            ContentValues userValues = new ContentValues();
            userValues.put(COLUMN_USER_ID, userId);
            userValues.put(COLUMN_USER_EMAIL, userEmail);
            userValues.put(COLUMN_USER_IS_ACTIVE, 1);
            userValues.put(COLUMN_USER_CREATED_AT, System.currentTimeMillis());
            userValues.put(COLUMN_USER_CURRENT_STREAK, 0);
            userValues.put(COLUMN_USER_LAST_STREAK_DATE, 0);
            userValues.put(COLUMN_USER_DAILY_HABITS_COMPLETED, 0);
            userValues.put(COLUMN_USER_LAST_ACTIVITY_DATE, 0);
            
            long insertedId = db.insert(TABLE_USERS, null, userValues);
            android.util.Log.d("HabitDatabaseHelper", "✅ Usuario " + userId + " creado en tabla users local (insertedId: " + insertedId + ")");
        }
        
        Cursor cursor = db.query(TABLE_USERS, 
                new String[]{COLUMN_USER_LAST_ACTIVITY_DATE, COLUMN_USER_DAILY_HABITS_COMPLETED, COLUMN_USER_CURRENT_STREAK, COLUMN_USER_LAST_STREAK_DATE},
                COLUMN_USER_ID + "=?", 
                new String[]{String.valueOf(userId)}, 
                null, null, null);
        
        if (cursor.moveToFirst()) {
            long lastActivityDate = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_USER_LAST_ACTIVITY_DATE));
            
            // Si la última actividad fue antes de hoy (00:00), resetear contador
            // IMPORTANTE: lastActivityDate puede ser 0 (nunca inicializado), en cuyo caso también debemos resetear
            // Pero si lastActivityDate == todayTimestamp, significa que ya hubo actividad hoy, NO resetear
            if (lastActivityDate == 0 || lastActivityDate < todayTimestamp) {
                ContentValues values = new ContentValues();
                
                // Si la última actividad fue ayer (mismo día calendario pero diferente timestamp)
                // O si nunca hubo actividad (lastActivityDate == 0), simplemente resetear
                if (lastActivityDate > 0) {
                    long yesterdayTimestamp = todayTimestamp - 86400; // 24 horas en segundos
                    
                    if (lastActivityDate >= yesterdayTimestamp && lastActivityDate < todayTimestamp) {
                        // Fue ayer, mantener la racha si se completaron 3+ hábitos
                        int dailyHabitsCompleted = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_DAILY_HABITS_COMPLETED));
                        int currentStreak = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_CURRENT_STREAK));
                        
                        if (dailyHabitsCompleted >= 3) {
                            // Se mantuvo la racha, incrementar
                            values.put(COLUMN_USER_CURRENT_STREAK, currentStreak + 1);
                            values.put(COLUMN_USER_LAST_STREAK_DATE, todayTimestamp);
                            android.util.Log.d("HabitDatabaseHelper", "✅ Racha incrementada de " + currentStreak + " a " + (currentStreak + 1) + " días (usuario completó 3+ hábitos ayer)");
                        } else {
                            // No se completaron 3 hábitos, resetear racha
                            values.put(COLUMN_USER_CURRENT_STREAK, 0);
                            android.util.Log.d("HabitDatabaseHelper", "⚠️ Racha reseteada a 0 (usuario no completó 3+ hábitos ayer)");
                        }
                    } else {
                        // Fue hace más de un día, resetear racha
                        values.put(COLUMN_USER_CURRENT_STREAK, 0);
                        android.util.Log.d("HabitDatabaseHelper", "⚠️ Racha reseteada a 0 (última actividad fue hace más de un día)");
                    }
                } else {
                    // Nunca hubo actividad, resetear racha
                    values.put(COLUMN_USER_CURRENT_STREAK, 0);
                    android.util.Log.d("HabitDatabaseHelper", "ℹ️ Inicializando racha (primera vez)");
                }
                
                // Resetear contador diario
                values.put(COLUMN_USER_DAILY_HABITS_COMPLETED, 0);
                values.put(COLUMN_USER_LAST_ACTIVITY_DATE, todayTimestamp);
                
                db.update(TABLE_USERS, values, COLUMN_USER_ID + "=?", new String[]{String.valueOf(userId)});
                android.util.Log.d("HabitDatabaseHelper", "✅ Contador diario reseteado para nuevo día (usuario " + userId + ")");
            } else {
                // lastActivityDate >= todayTimestamp, significa que ya hubo actividad hoy
                // No resetear, solo verificar que todo esté correcto
                android.util.Log.d("HabitDatabaseHelper", "ℹ️ No es necesario resetear contador (última actividad fue hoy)");
            }
        } else {
            android.util.Log.w("HabitDatabaseHelper", "⚠️ Usuario " + userId + " no encontrado en la base de datos");
        }
        cursor.close();
        db.close();
    }

    /**
     * Incrementa el contador de hábitos completados hoy
     * y actualiza la racha si se alcanzan 3 hábitos
     */
    public void incrementDailyHabitCompleted(long userId) {
        checkAndResetDailyCounter(userId);
        
        SQLiteDatabase db = this.getWritableDatabase();
        
        // Asegurar que las columnas de racha existan antes de usarlas
        addColumnIfNotExists(db, TABLE_USERS, COLUMN_USER_CURRENT_STREAK, "INTEGER DEFAULT 0");
        addColumnIfNotExists(db, TABLE_USERS, COLUMN_USER_LAST_STREAK_DATE, "INTEGER DEFAULT 0");
        addColumnIfNotExists(db, TABLE_USERS, COLUMN_USER_DAILY_HABITS_COMPLETED, "INTEGER DEFAULT 0");
        addColumnIfNotExists(db, TABLE_USERS, COLUMN_USER_LAST_ACTIVITY_DATE, "INTEGER DEFAULT 0");
        
        long todayTimestamp = getTodayTimestamp();
        
        // CRÍTICO: Verificar si el usuario existe, si no, crearlo
        Cursor userCheck = db.query(TABLE_USERS, new String[]{COLUMN_USER_ID},
                COLUMN_USER_ID + "=?", new String[]{String.valueOf(userId)}, null, null, null);
        boolean userExists = userCheck.moveToFirst();
        userCheck.close();
        
        if (!userExists) {
            // Crear usuario si no existe (usando datos de SessionManager)
            com.tuempresa.proyecto_01_11_25.utils.SessionManager session = new com.tuempresa.proyecto_01_11_25.utils.SessionManager(context);
            String userEmail = session.getUserEmail();
            if (userEmail == null || userEmail.isEmpty()) {
                userEmail = "user" + userId + "@local.com";
            }
            
            ContentValues userValues = new ContentValues();
            userValues.put(COLUMN_USER_ID, userId);
            userValues.put(COLUMN_USER_EMAIL, userEmail);
            userValues.put(COLUMN_USER_IS_ACTIVE, 1);
            userValues.put(COLUMN_USER_CREATED_AT, System.currentTimeMillis());
            userValues.put(COLUMN_USER_CURRENT_STREAK, 0);
            userValues.put(COLUMN_USER_LAST_STREAK_DATE, 0);
            userValues.put(COLUMN_USER_DAILY_HABITS_COMPLETED, 0);
            userValues.put(COLUMN_USER_LAST_ACTIVITY_DATE, 0);
            
            long insertedId = db.insert(TABLE_USERS, null, userValues);
            android.util.Log.d("HabitDatabaseHelper", "✅ Usuario " + userId + " creado en tabla users local (insertedId: " + insertedId + ")");
        }
        
        // Obtener valores actuales
        Cursor cursor = db.query(TABLE_USERS, 
                new String[]{COLUMN_USER_DAILY_HABITS_COMPLETED, COLUMN_USER_CURRENT_STREAK},
                COLUMN_USER_ID + "=?", 
                new String[]{String.valueOf(userId)}, 
                null, null, null);
        
        int dailyHabitsCompleted = 0;
        int currentStreak = 0;
        
        if (cursor.moveToFirst()) {
            dailyHabitsCompleted = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_DAILY_HABITS_COMPLETED));
            currentStreak = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_CURRENT_STREAK));
        } else {
            android.util.Log.e("HabitDatabaseHelper", "❌ ERROR CRÍTICO: Usuario " + userId + " no encontrado después de intentar crearlo");
            cursor.close();
            db.close();
            return;
        }
        cursor.close();
        
        // Incrementar contador
        dailyHabitsCompleted++;
        
        android.util.Log.d("HabitDatabaseHelper", "Incrementando contador diario. Antes: " + (dailyHabitsCompleted - 1) + ", Después: " + dailyHabitsCompleted + ", Racha actual: " + currentStreak);
        
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_DAILY_HABITS_COMPLETED, dailyHabitsCompleted);
        values.put(COLUMN_USER_LAST_ACTIVITY_DATE, todayTimestamp);
        
        // Si se alcanzan 3 hábitos completados, activar o mantener la racha
        if (dailyHabitsCompleted >= 3) {
            if (currentStreak == 0) {
                // Iniciar racha si no tiene una activa
                values.put(COLUMN_USER_CURRENT_STREAK, 1);
                values.put(COLUMN_USER_LAST_STREAK_DATE, todayTimestamp);
                android.util.Log.d("HabitDatabaseHelper", "✅ RACHA INICIADA: usuario completó " + dailyHabitsCompleted + " hábitos hoy. Racha establecida en 1 día.");
            } else {
                // Si ya tiene racha, asegurar que la fecha de última racha sea hoy
                // (esto es importante para mantener la racha activa)
                values.put(COLUMN_USER_LAST_STREAK_DATE, todayTimestamp);
                android.util.Log.d("HabitDatabaseHelper", "✅ RACHA MANTENIDA: usuario ya tiene racha de " + currentStreak + " días. Completó " + dailyHabitsCompleted + " hábitos hoy.");
            }
        } else {
            android.util.Log.d("HabitDatabaseHelper", "⚠️ Racha no activada aún. Hábitos completados hoy: " + dailyHabitsCompleted + " (se necesitan 3)");
        }
        
        db.update(TABLE_USERS, values, COLUMN_USER_ID + "=?", new String[]{String.valueOf(userId)});
        db.close();
        
        // Verificar que la actualización se guardó correctamente
        android.util.Log.d("HabitDatabaseHelper", "Actualización guardada en BD. Verificando...");
        SQLiteDatabase verifyDb = this.getReadableDatabase();
        Cursor verifyCursor = verifyDb.query(TABLE_USERS, 
                new String[]{COLUMN_USER_DAILY_HABITS_COMPLETED, COLUMN_USER_CURRENT_STREAK},
                COLUMN_USER_ID + "=?", 
                new String[]{String.valueOf(userId)}, 
                null, null, null);
        if (verifyCursor.moveToFirst()) {
            int verifyDaily = verifyCursor.getInt(verifyCursor.getColumnIndexOrThrow(COLUMN_USER_DAILY_HABITS_COMPLETED));
            int verifyStreak = verifyCursor.getInt(verifyCursor.getColumnIndexOrThrow(COLUMN_USER_CURRENT_STREAK));
            android.util.Log.d("HabitDatabaseHelper", "✅ Verificación: Hábitos completados hoy: " + verifyDaily + ", Racha actual: " + verifyStreak);
        }
        verifyCursor.close();
        verifyDb.close();
    }

    /**
     * Obtiene la racha actual del usuario
     * @return La racha actual (0 si no tiene racha todavía)
     */
    public int getCurrentStreak(long userId) {
        // NO llamar a checkAndResetDailyCounter aquí porque puede resetear el contador
        // justo después de que se recalcule. En su lugar, solo leer el valor.
        
        SQLiteDatabase db = this.getReadableDatabase();
        addColumnIfNotExists(db, TABLE_USERS, COLUMN_USER_CURRENT_STREAK, "INTEGER DEFAULT 0");
        addColumnIfNotExists(db, TABLE_USERS, COLUMN_USER_DAILY_HABITS_COMPLETED, "INTEGER DEFAULT 0");
        
        Cursor cursor = db.query(TABLE_USERS, 
                new String[]{COLUMN_USER_CURRENT_STREAK, COLUMN_USER_DAILY_HABITS_COMPLETED},
                COLUMN_USER_ID + "=?", 
                new String[]{String.valueOf(userId)}, 
                null, null, null);
        
        int streak = 0;
        int dailyHabitsCompleted = 0;
        if (cursor.moveToFirst()) {
            streak = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_CURRENT_STREAK));
            dailyHabitsCompleted = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_DAILY_HABITS_COMPLETED));
        }
        cursor.close();
        db.close();
        
        // CRÍTICO: Si se completaron 3 o más hábitos hoy pero la racha es 0, forzar la activación
        // Esto maneja casos donde el incremento no se guardó correctamente o hubo un error
        if (dailyHabitsCompleted >= 3 && streak == 0) {
            android.util.Log.w("HabitDatabaseHelper", "❌ PROBLEMA CRÍTICO: Usuario completó " + dailyHabitsCompleted + " hábitos pero la racha es 0. Forzando activación de racha.");
            SQLiteDatabase writeDb = this.getWritableDatabase();
            long todayTimestamp = getTodayTimestamp();
            ContentValues values = new ContentValues();
            values.put(COLUMN_USER_CURRENT_STREAK, 1);
            values.put(COLUMN_USER_LAST_STREAK_DATE, todayTimestamp);
            int rowsUpdated = writeDb.update(TABLE_USERS, values, COLUMN_USER_ID + "=?", new String[]{String.valueOf(userId)});
            writeDb.close();
            android.util.Log.d("HabitDatabaseHelper", "✅ Racha forzada a 1 día. Usuario: " + userId + ", Filas actualizadas: " + rowsUpdated);
            return 1; // Devolver 1 ya que la racha acaba de ser activada
        }
        
        android.util.Log.d("HabitDatabaseHelper", "📊 getCurrentStreak() - Usuario: " + userId + ", Racha: " + streak + ", Hábitos completados: " + dailyHabitsCompleted);
        return streak;
    }

    /**
     * Obtiene la racha actual del usuario logueado
     */
    public int getCurrentStreak() {
        long userId = getCurrentUserId();
        return getCurrentStreak(userId);
    }
    
    /**
     * Recalcula el contador de hábitos completados hoy basándose en los hábitos actualmente completados
     * Útil cuando los hábitos se completaron antes de que se implementara el sistema de rachas
     * o cuando hay inconsistencias en el contador
     */
    public void recalculateDailyHabitsCompleted(long userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        
        // Asegurar que las columnas de racha existan
        addColumnIfNotExists(db, TABLE_USERS, COLUMN_USER_CURRENT_STREAK, "INTEGER DEFAULT 0");
        addColumnIfNotExists(db, TABLE_USERS, COLUMN_USER_LAST_STREAK_DATE, "INTEGER DEFAULT 0");
        addColumnIfNotExists(db, TABLE_USERS, COLUMN_USER_DAILY_HABITS_COMPLETED, "INTEGER DEFAULT 0");
        addColumnIfNotExists(db, TABLE_USERS, COLUMN_USER_LAST_ACTIVITY_DATE, "INTEGER DEFAULT 0");
        
        // Contar cuántos hábitos están completados hoy para este usuario
        Cursor cursor = db.query(TABLE_HABITS, 
                new String[]{COLUMN_HABIT_ID},
                COLUMN_HABIT_USER_ID + "=? AND " + COLUMN_HABIT_COMPLETED + "=1",
                new String[]{String.valueOf(userId)}, 
                null, null, null);
        
        int completedHabitsCount = cursor.getCount();
        cursor.close();
        db.close();
        
        android.util.Log.d("HabitDatabaseHelper", "🔄 Recalculando hábitos completados: " + completedHabitsCount + " hábitos completados para usuario " + userId);
        
        // CRÍTICO: Verificar si el usuario existe por user_id, si no, crearlo
        db = this.getWritableDatabase();
        Cursor userCheck = db.query(TABLE_USERS, new String[]{COLUMN_USER_ID},
                COLUMN_USER_ID + "=?", new String[]{String.valueOf(userId)}, null, null, null);
        boolean userExists = userCheck.moveToFirst();
        userCheck.close();
        
        if (!userExists) {
            // Crear usuario si no existe con este user_id específico
            com.tuempresa.proyecto_01_11_25.utils.SessionManager session = new com.tuempresa.proyecto_01_11_25.utils.SessionManager(context);
            String userEmail = session.getUserEmail();
            if (userEmail == null || userEmail.isEmpty()) {
                userEmail = "user" + userId + "@local.com";
            }
            
            // Verificar si el usuario existe por email (puede haber sido creado con otro userId)
            Cursor emailCheck = db.query(TABLE_USERS, new String[]{COLUMN_USER_ID, COLUMN_USER_EMAIL},
                    COLUMN_USER_EMAIL + "=?", new String[]{userEmail}, null, null, null);
            boolean emailExists = emailCheck.moveToFirst();
            long existingUserId = -1;
            if (emailExists) {
                existingUserId = emailCheck.getLong(emailCheck.getColumnIndexOrThrow(COLUMN_USER_ID));
            }
            emailCheck.close();
            
            if (emailExists && existingUserId != userId) {
                // El usuario existe pero con otro user_id, actualizar el user_id
                android.util.Log.w("HabitDatabaseHelper", "⚠️ Usuario con email " + userEmail + " existe con user_id " + existingUserId + ", pero necesitamos " + userId + ". Actualizando...");
                ContentValues updateValues = new ContentValues();
                updateValues.put(COLUMN_USER_ID, userId);
                int updated = db.update(TABLE_USERS, updateValues, COLUMN_USER_EMAIL + "=?", new String[]{userEmail});
                if (updated > 0) {
                    android.util.Log.d("HabitDatabaseHelper", "✅ Usuario actualizado: user_id cambiado de " + existingUserId + " a " + userId);
                } else {
                    android.util.Log.e("HabitDatabaseHelper", "❌ No se pudo actualizar user_id del usuario con email " + userEmail);
                }
            } else if (!emailExists) {
                // El usuario no existe, crearlo
                ContentValues userValues = new ContentValues();
                userValues.put(COLUMN_USER_ID, userId);
                userValues.put(COLUMN_USER_EMAIL, userEmail);
                userValues.put(COLUMN_USER_IS_ACTIVE, 1);
                userValues.put(COLUMN_USER_CREATED_AT, System.currentTimeMillis());
                userValues.put(COLUMN_USER_CURRENT_STREAK, 0);
                userValues.put(COLUMN_USER_LAST_STREAK_DATE, 0);
                userValues.put(COLUMN_USER_DAILY_HABITS_COMPLETED, 0);
                userValues.put(COLUMN_USER_LAST_ACTIVITY_DATE, 0);
                
                long insertedId = db.insert(TABLE_USERS, null, userValues);
                if (insertedId != -1) {
                    android.util.Log.d("HabitDatabaseHelper", "✅ Usuario " + userId + " creado en recalculateDailyHabitsCompleted (insertedId: " + insertedId + ")");
                } else {
                    android.util.Log.e("HabitDatabaseHelper", "❌ No se pudo crear usuario " + userId + " (puede que ya exista)");
                }
            }
        }
        
        long todayTimestamp = getTodayTimestamp();
        
        // Obtener valores actuales del usuario
        Cursor userCursor = db.query(TABLE_USERS, 
                new String[]{COLUMN_USER_DAILY_HABITS_COMPLETED, COLUMN_USER_CURRENT_STREAK, COLUMN_USER_LAST_ACTIVITY_DATE},
                COLUMN_USER_ID + "=?", 
                new String[]{String.valueOf(userId)}, 
                null, null, null);
        
        int currentStreak = 0;
        long lastActivityDate = 0;
        if (userCursor.moveToFirst()) {
            currentStreak = userCursor.getInt(userCursor.getColumnIndexOrThrow(COLUMN_USER_CURRENT_STREAK));
            lastActivityDate = userCursor.getLong(userCursor.getColumnIndexOrThrow(COLUMN_USER_LAST_ACTIVITY_DATE));
        }
        userCursor.close();
        
        // CRÍTICO: Si la última actividad fue antes de hoy Y hay hábitos completados hoy,
        // significa que es un nuevo día pero el usuario ya completó hábitos hoy.
        // En este caso, NO resetear el contador, solo actualizarlo con los hábitos completados.
        // Si la última actividad fue hoy, también actualizar normalmente.
        
        // Actualizar contador con el valor recalculado
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_DAILY_HABITS_COMPLETED, completedHabitsCount);
        values.put(COLUMN_USER_LAST_ACTIVITY_DATE, todayTimestamp);
        
        // Si la última actividad fue antes de hoy y hay hábitos completados, 
        // verificar si debemos mantener o resetear la racha
        if (lastActivityDate > 0 && lastActivityDate < todayTimestamp && completedHabitsCount > 0) {
            // Es un nuevo día pero hay hábitos completados hoy
            // Verificar si ayer se completaron 3+ hábitos para mantener la racha
            long yesterdayTimestamp = todayTimestamp - 86400;
            if (lastActivityDate >= yesterdayTimestamp && lastActivityDate < todayTimestamp) {
                // Fue ayer, verificar si se completaron 3+ hábitos ayer
                // (esto se maneja en checkAndResetDailyCounter, pero aquí solo actualizamos el contador de hoy)
                android.util.Log.d("HabitDatabaseHelper", "ℹ️ Nuevo día detectado. Hábitos completados hoy: " + completedHabitsCount);
            }
        }
        
        // Si se alcanzan 3 hábitos completados, activar o mantener la racha
        if (completedHabitsCount >= 3) {
            if (currentStreak == 0) {
                // Iniciar racha si no tiene una activa
                values.put(COLUMN_USER_CURRENT_STREAK, 1);
                values.put(COLUMN_USER_LAST_STREAK_DATE, todayTimestamp);
                android.util.Log.d("HabitDatabaseHelper", "✅ RACHA INICIADA (recalculada): usuario tiene " + completedHabitsCount + " hábitos completados. Racha establecida en 1 día.");
            } else {
                // Si ya tiene racha, asegurar que la fecha de última racha sea hoy
                values.put(COLUMN_USER_LAST_STREAK_DATE, todayTimestamp);
                android.util.Log.d("HabitDatabaseHelper", "✅ RACHA MANTENIDA (recalculada): usuario ya tiene racha de " + currentStreak + " días. Tiene " + completedHabitsCount + " hábitos completados hoy.");
            }
        } else {
            android.util.Log.d("HabitDatabaseHelper", "⚠️ Racha no activada (recalculada). Hábitos completados: " + completedHabitsCount + " (se necesitan 3)");
        }
        
        int rowsUpdated = db.update(TABLE_USERS, values, COLUMN_USER_ID + "=?", new String[]{String.valueOf(userId)});
        
        if (rowsUpdated == 0) {
            android.util.Log.w("HabitDatabaseHelper", "⚠️ No se actualizó ninguna fila. Verificando si el usuario existe...");
            // Verificar si el usuario existe
            Cursor verifyUser = db.query(TABLE_USERS, new String[]{COLUMN_USER_ID},
                    COLUMN_USER_ID + "=?", new String[]{String.valueOf(userId)}, null, null, null);
            boolean userExistsInDb = verifyUser.moveToFirst();
            verifyUser.close();
            
            if (!userExistsInDb) {
                android.util.Log.e("HabitDatabaseHelper", "❌ Usuario " + userId + " no existe en la tabla users. No se puede actualizar la racha.");
            } else {
                android.util.Log.e("HabitDatabaseHelper", "❌ Usuario " + userId + " existe pero la actualización falló. Valores: " + values.toString());
            }
        } else {
            android.util.Log.d("HabitDatabaseHelper", "✅ Recalculación completada. Filas actualizadas: " + rowsUpdated + ", Hábitos completados: " + completedHabitsCount + ", Racha: " + (completedHabitsCount >= 3 ? "1" : "0"));
        }
        
        db.close();
    }

    /**
     * Obtiene el número de hábitos completados hoy
     */
    public int getDailyHabitsCompleted(long userId) {
        // NO llamar a checkAndResetDailyCounter aquí porque puede resetear el contador
        // justo después de que se recalcule. En su lugar, recalcular si es necesario.
        
        SQLiteDatabase db = this.getReadableDatabase();
        addColumnIfNotExists(db, TABLE_USERS, COLUMN_USER_DAILY_HABITS_COMPLETED, "INTEGER DEFAULT 0");
        
        Cursor cursor = db.query(TABLE_USERS, 
                new String[]{COLUMN_USER_DAILY_HABITS_COMPLETED},
                COLUMN_USER_ID + "=?", 
                new String[]{String.valueOf(userId)}, 
                null, null, null);
        
        int count = 0;
        boolean userExists = cursor.moveToFirst();
        if (userExists) {
            count = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_DAILY_HABITS_COMPLETED));
        }
        cursor.close();
        
        // Si el usuario no existe o el contador es 0, verificar si hay hábitos completados
        if (!userExists || count == 0) {
            Cursor habitsCursor = db.query(TABLE_HABITS, 
                    new String[]{COLUMN_HABIT_ID},
                    COLUMN_HABIT_USER_ID + "=? AND " + COLUMN_HABIT_COMPLETED + "=1",
                    new String[]{String.valueOf(userId)}, 
                    null, null, null);
            int completedHabits = habitsCursor.getCount();
            habitsCursor.close();
            
            if (completedHabits > 0) {
                android.util.Log.w("HabitDatabaseHelper", "⚠️ Contador es " + count + " pero hay " + completedHabits + " hábitos completados. Recalculando...");
                db.close(); // Cerrar antes de recalcular
                recalculateDailyHabitsCompleted(userId);
                // Leer de nuevo
                db = this.getReadableDatabase();
                cursor = db.query(TABLE_USERS, 
                        new String[]{COLUMN_USER_DAILY_HABITS_COMPLETED},
                        COLUMN_USER_ID + "=?", 
                        new String[]{String.valueOf(userId)}, 
                        null, null, null);
                if (cursor.moveToFirst()) {
                    count = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_DAILY_HABITS_COMPLETED));
                }
                cursor.close();
            }
        }
        
        db.close();
        return count;
    }

    // ========== GESTIÓN DE AMIGOS ==========

    /**
     * Agrega un amigo a la lista del usuario actual
     */
    public long addFriend(long userId, long friendUserId, String friendEmail, String friendName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FRIEND_USER_ID, userId);
        values.put(COLUMN_FRIEND_FRIEND_USER_ID, friendUserId);
        values.put(COLUMN_FRIEND_EMAIL, friendEmail);
        values.put(COLUMN_FRIEND_NAME, friendName);
        values.put(COLUMN_FRIEND_ADDED_AT, System.currentTimeMillis() / 1000);
        
        long id = db.insertWithOnConflict(TABLE_FRIENDS, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        db.close();
        return id;
    }

    /**
     * Obtiene todos los amigos del usuario actual
     */
    public List<Friend> getAllFriends(long userId) {
        List<Friend> friends = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_FRIENDS, null,
                COLUMN_FRIEND_USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null, COLUMN_FRIEND_ADDED_AT + " DESC");

        if (cursor.moveToFirst()) {
            do {
                Friend friend = new Friend();
                friend.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_FRIEND_ID)));
                friend.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_FRIEND_USER_ID)));
                friend.setFriendUserId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_FRIEND_FRIEND_USER_ID)));
                friend.setFriendEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FRIEND_EMAIL)));
                friend.setFriendName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FRIEND_NAME)));
                friend.setTotalHabits(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FRIEND_TOTAL_HABITS)));
                friend.setTotalPoints(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FRIEND_TOTAL_POINTS)));
                friend.setCurrentStreak(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FRIEND_CURRENT_STREAK)));
                friend.setAddedAt(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_FRIEND_ADDED_AT)) * 1000);
                friends.add(friend);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return friends;
    }

    /**
     * Elimina un amigo de la lista
     */
    public boolean deleteFriend(long friendId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_FRIENDS, COLUMN_FRIEND_ID + "=?", new String[]{String.valueOf(friendId)});
        db.close();
        return rowsAffected > 0;
    }

    /**
     * Actualiza las estadísticas de un amigo
     */
    public void updateFriendStats(long friendId, int totalHabits, int totalPoints, int currentStreak) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FRIEND_TOTAL_HABITS, totalHabits);
        values.put(COLUMN_FRIEND_TOTAL_POINTS, totalPoints);
        values.put(COLUMN_FRIEND_CURRENT_STREAK, currentStreak);
        db.update(TABLE_FRIENDS, values, COLUMN_FRIEND_ID + "=?", new String[]{String.valueOf(friendId)});
        db.close();
    }

    /**
     * Verifica si un amigo ya existe
     */
    public boolean friendExists(long userId, String friendEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_FRIENDS, new String[]{COLUMN_FRIEND_ID},
                COLUMN_FRIEND_USER_ID + "=? AND " + COLUMN_FRIEND_EMAIL + "=?",
                new String[]{String.valueOf(userId), friendEmail},
                null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }
}
