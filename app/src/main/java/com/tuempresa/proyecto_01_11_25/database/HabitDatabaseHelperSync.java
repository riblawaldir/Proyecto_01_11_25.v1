package com.tuempresa.proyecto_01_11_25.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.tuempresa.proyecto_01_11_25.model.Habit;

import java.util.ArrayList;
import java.util.List;

/**
 * Extensión de HabitDatabaseHelper para agregar funcionalidad de sincronización.
 * Agrega campos para tracking de sincronización y tabla de operaciones pendientes.
 */
public class HabitDatabaseHelperSync extends HabitDatabaseHelper {
    private static final String TAG = "HabitDatabaseHelperSync";
    private static final int DATABASE_VERSION_SYNC = 5; // Nueva versión con sincronización
    private static final String DATABASE_NAME = "habitus.db";

    // Campos adicionales para sincronización (deben coincidir con SQL Server)
    private static final String COLUMN_HABIT_SYNCED = "synced";
    private static final String COLUMN_HABIT_SERVER_ID = "server_id";
    private static final String COLUMN_HABIT_UPDATED_AT = "updated_at";
    
    // Nota: Los campos de la tabla habits deben coincidir con SQL Server:
    // SQL Server: Id, Title, Goal, Category, Type, Completed, Points, TargetValue, TargetUnit,
    //             PagesPerDay, ReminderTimes, DurationMinutes, DndMode, MusicId, JournalEnabled,
    //             GymDays, WaterGoalGlasses, OneClickComplete, EnglishMode, CodingMode, HabitIcon, CreatedAt
    // SQLite:     id, title, goal, category, type, completed, points, target_value, target_unit,
    //             pages_per_day, reminder_times, duration_minutes, dnd_mode, music_id, journal_enabled,
    //             gym_days, water_goal_glasses, one_click_complete, english_mode, coding_mode, habit_icon, created_at

    // Tabla de operaciones pendientes
    private static final String TABLE_PENDING_OPERATIONS = "pending_operations";
    private static final String COLUMN_PENDING_ID = "id";
    private static final String COLUMN_PENDING_OPERATION_TYPE = "operation_type";
    private static final String COLUMN_PENDING_ENTITY_TYPE = "entity_type";
    private static final String COLUMN_PENDING_ENTITY_ID = "entity_id";
    private static final String COLUMN_PENDING_ENTITY_DATA = "entity_data";
    private static final String COLUMN_PENDING_CREATED_AT = "created_at";
    private static final String COLUMN_PENDING_RETRY_COUNT = "retry_count";
    private static final String COLUMN_PENDING_LAST_ERROR = "last_error";
    private static final String COLUMN_PENDING_PRIORITY = "priority";

    public HabitDatabaseHelperSync(Context context) {
        // Llamar al constructor del padre que solo acepta Context
        super(context);
    }

    /**
     * Elimina la base de datos local completamente.
     * Útil para resolver conflictos y forzar una sincronización limpia desde la API.
     * @param context Contexto de la aplicación
     */
    public static void deleteLocalDatabase(Context context) {
        try {
            // Cerrar cualquier conexión abierta primero
            context.deleteDatabase(DATABASE_NAME);
            Log.d(TAG, "Base de datos local eliminada: " + DATABASE_NAME);
        } catch (Exception e) {
            Log.e(TAG, "Error al eliminar base de datos local", e);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Llamar al onCreate del padre para crear las tablas base
        super.onCreate(db);
        
        // Agregar columnas de sincronización a la tabla habits
        addColumnIfNotExists(db, TABLE_HABITS, COLUMN_HABIT_SYNCED, "INTEGER DEFAULT 0");
        addColumnIfNotExists(db, TABLE_HABITS, COLUMN_HABIT_SERVER_ID, "INTEGER");
        addColumnIfNotExists(db, TABLE_HABITS, COLUMN_HABIT_UPDATED_AT, "INTEGER DEFAULT 0");

        // Crear tabla de operaciones pendientes
        String createPendingOpsTable = "CREATE TABLE IF NOT EXISTS " + TABLE_PENDING_OPERATIONS + " (" +
                COLUMN_PENDING_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_PENDING_OPERATION_TYPE + " TEXT NOT NULL, " +
                COLUMN_PENDING_ENTITY_TYPE + " TEXT NOT NULL, " +
                COLUMN_PENDING_ENTITY_ID + " INTEGER NOT NULL, " +
                COLUMN_PENDING_ENTITY_DATA + " TEXT, " +
                COLUMN_PENDING_CREATED_AT + " INTEGER DEFAULT (strftime('%s', 'now')), " +
                COLUMN_PENDING_RETRY_COUNT + " INTEGER DEFAULT 0, " +
                COLUMN_PENDING_LAST_ERROR + " TEXT, " +
                COLUMN_PENDING_PRIORITY + " INTEGER DEFAULT 2" +
                ")";
        
        db.execSQL(createPendingOpsTable);
        Log.d(TAG, "Tabla de operaciones pendientes creada");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onUpgrade(db, oldVersion, newVersion);
        
        // Agregar columnas de sincronización si no existen (para bases de datos existentes)
        addColumnIfNotExists(db, TABLE_HABITS, COLUMN_HABIT_SYNCED, "INTEGER DEFAULT 0");
        addColumnIfNotExists(db, TABLE_HABITS, COLUMN_HABIT_SERVER_ID, "INTEGER");
        addColumnIfNotExists(db, TABLE_HABITS, COLUMN_HABIT_UPDATED_AT, "INTEGER DEFAULT 0");

        // Crear tabla de operaciones pendientes si no existe
        String createPendingOpsTable = "CREATE TABLE IF NOT EXISTS " + TABLE_PENDING_OPERATIONS + " (" +
                COLUMN_PENDING_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_PENDING_OPERATION_TYPE + " TEXT NOT NULL, " +
                COLUMN_PENDING_ENTITY_TYPE + " TEXT NOT NULL, " +
                COLUMN_PENDING_ENTITY_ID + " INTEGER NOT NULL, " +
                COLUMN_PENDING_ENTITY_DATA + " TEXT, " +
                COLUMN_PENDING_CREATED_AT + " INTEGER DEFAULT (strftime('%s', 'now')), " +
                COLUMN_PENDING_RETRY_COUNT + " INTEGER DEFAULT 0, " +
                COLUMN_PENDING_LAST_ERROR + " TEXT, " +
                COLUMN_PENDING_PRIORITY + " INTEGER DEFAULT 2" +
                ")";
        
        try {
            db.execSQL(createPendingOpsTable);
            Log.d(TAG, "Tabla de operaciones pendientes creada/verificada");
        } catch (Exception e) {
            // La tabla ya existe, ignorar
            Log.d(TAG, "Tabla de operaciones pendientes ya existe");
        }
    }

    // ========== MÉTODOS DE SINCRONIZACIÓN PARA HÁBITOS ==========

    /**
     * Verifica si una columna existe en la tabla
     */
    private boolean columnExists(SQLiteDatabase db, String table, String column) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("PRAGMA table_info(" + table + ")", null);
            if (cursor != null) {
                int columnIndex = cursor.getColumnIndex("name");
                while (cursor.moveToNext()) {
                    String name = cursor.getString(columnIndex);
                    if (column.equals(name)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al verificar columna", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return false;
    }

    /**
     * Asegura que las columnas de sincronización existan
     * Este método se ejecuta automáticamente antes de cualquier operación que use estas columnas
     */
    private void ensureSyncColumns(SQLiteDatabase db) {
        try {
            // Siempre intentar agregar las columnas (addColumnIfNotExists verifica internamente)
            // Esto es más seguro que confiar solo en columnExists
            addColumnIfNotExists(db, TABLE_HABITS, COLUMN_HABIT_SYNCED, "INTEGER DEFAULT 0");
            addColumnIfNotExists(db, TABLE_HABITS, COLUMN_HABIT_SERVER_ID, "INTEGER");
            addColumnIfNotExists(db, TABLE_HABITS, COLUMN_HABIT_UPDATED_AT, "INTEGER DEFAULT 0");
            Log.d(TAG, "Columnas de sincronización verificadas");
        } catch (Exception e) {
            Log.e(TAG, "Error al asegurar columnas de sincronización", e);
            // Intentar agregar las columnas de todas formas
            try {
                addColumnIfNotExists(db, TABLE_HABITS, COLUMN_HABIT_SYNCED, "INTEGER DEFAULT 0");
                addColumnIfNotExists(db, TABLE_HABITS, COLUMN_HABIT_SERVER_ID, "INTEGER");
                addColumnIfNotExists(db, TABLE_HABITS, COLUMN_HABIT_UPDATED_AT, "INTEGER DEFAULT 0");
            } catch (Exception e2) {
                Log.e(TAG, "Error crítico al agregar columnas de sincronización", e2);
            }
        }
    }
    
    /**
     * Obtiene la base de datos de escritura asegurando que las columnas de sincronización existan
     */
    @Override
    public SQLiteDatabase getWritableDatabase() {
        SQLiteDatabase db = super.getWritableDatabase();
        ensureSyncColumns(db);
        return db;
    }
    
    /**
     * Obtiene la base de datos de lectura asegurando que las columnas de sincronización existan
     */
    @Override
    public SQLiteDatabase getReadableDatabase() {
        SQLiteDatabase db = super.getReadableDatabase();
        ensureSyncColumns(db);
        return db;
    }

    /**
     * Verifica si una tabla existe
     */
    private boolean tableExists(SQLiteDatabase db, String tableName) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?", new String[]{tableName});
            return cursor != null && cursor.getCount() > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error al verificar tabla", e);
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Asegura que la tabla de operaciones pendientes exista
     */
    private void ensurePendingOperationsTable(SQLiteDatabase db) {
        if (!tableExists(db, TABLE_PENDING_OPERATIONS)) {
            String createPendingOpsTable = "CREATE TABLE IF NOT EXISTS " + TABLE_PENDING_OPERATIONS + " (" +
                    COLUMN_PENDING_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_PENDING_OPERATION_TYPE + " TEXT NOT NULL, " +
                    COLUMN_PENDING_ENTITY_TYPE + " TEXT NOT NULL, " +
                    COLUMN_PENDING_ENTITY_ID + " INTEGER NOT NULL, " +
                    COLUMN_PENDING_ENTITY_DATA + " TEXT, " +
                    COLUMN_PENDING_CREATED_AT + " INTEGER DEFAULT (strftime('%s', 'now')), " +
                    COLUMN_PENDING_RETRY_COUNT + " INTEGER DEFAULT 0, " +
                    COLUMN_PENDING_LAST_ERROR + " TEXT, " +
                    COLUMN_PENDING_PRIORITY + " INTEGER DEFAULT 2" +
                    ")";
            
            db.execSQL(createPendingOpsTable);
            Log.d(TAG, "Tabla 'pending_operations' creada");
        }
    }

    /**
     * Obtiene hábitos no sincronizados del usuario actual
     */
    public List<Habit> getUnsyncedHabits() {
        List<Habit> habits = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        // Asegurar que las columnas de sincronización existan
        ensureSyncColumns(db);
        
        // CRÍTICO: Filtrar por userId del usuario actual
        long currentUserId = getCurrentUserId();
        if (currentUserId <= 0) {
            Log.w(TAG, "⚠️ No se pueden obtener hábitos: userId inválido (" + currentUserId + ")");
            return habits;
        }
        
        Cursor cursor = db.query(TABLE_HABITS, null, 
                COLUMN_HABIT_SYNCED + "=0 AND " + COLUMN_HABIT_USER_ID + "=?",
                new String[]{String.valueOf(currentUserId)}, null, null, COLUMN_HABIT_CREATED_AT + " DESC");

        if (cursor.moveToFirst()) {
            do {
                Habit habit = cursorToHabit(cursor);
                habits.add(habit);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return habits;
    }

    /**
     * Obtiene hábitos sincronizados (que tienen serverId) del usuario actual
     */
    public List<Habit> getSyncedHabits() {
        List<Habit> habits = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        // Asegurar que las columnas de sincronización existan
        ensureSyncColumns(db);
        
        // CRÍTICO: Filtrar por userId del usuario actual
        long currentUserId = getCurrentUserId();
        if (currentUserId <= 0) {
            Log.w(TAG, "⚠️ No se pueden obtener hábitos sincronizados: userId inválido (" + currentUserId + ")");
            return habits;
        }
        
        // Obtener hábitos que tienen serverId (están sincronizados) Y pertenecen al usuario actual
        Cursor cursor = db.query(TABLE_HABITS, null, 
                COLUMN_HABIT_SYNCED + "=1 AND " + COLUMN_HABIT_SERVER_ID + " IS NOT NULL AND " + COLUMN_HABIT_USER_ID + "=?", 
                new String[]{String.valueOf(currentUserId)}, null, null, COLUMN_HABIT_CREATED_AT + " DESC");

        if (cursor.moveToFirst()) {
            do {
                Habit habit = cursorToHabit(cursor);
                habits.add(habit);
            } while (cursor.moveToNext());
        }
        cursor.close();
        // NO cerrar db aquí - HabitDatabaseHelper maneja la conexión automáticamente
        // db.close(); // ELIMINADO: causa "attempt to re-open an already-closed object"
        return habits;
    }

    /**
     * Marca un hábito como sincronizado
     */
    public void markHabitAsSynced(long localId, long serverId) {
        SQLiteDatabase db = this.getWritableDatabase();
        // ensureSyncColumns ya se ejecuta en getWritableDatabase(), pero lo llamamos de nuevo por seguridad
        ensureSyncColumns(db);
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_HABIT_SYNCED, 1);
            values.put(COLUMN_HABIT_SERVER_ID, serverId);
            values.put(COLUMN_HABIT_UPDATED_AT, System.currentTimeMillis() / 1000);
            db.update(TABLE_HABITS, values, COLUMN_HABIT_ID + "=?", new String[]{String.valueOf(localId)});
        } catch (Exception e) {
            Log.e(TAG, "Error al marcar hábito como sincronizado", e);
            // Si falla, intentar agregar las columnas y reintentar
            ensureSyncColumns(db);
            try {
                ContentValues values = new ContentValues();
                values.put(COLUMN_HABIT_SYNCED, 1);
                values.put(COLUMN_HABIT_SERVER_ID, serverId);
                values.put(COLUMN_HABIT_UPDATED_AT, System.currentTimeMillis() / 1000);
                db.update(TABLE_HABITS, values, COLUMN_HABIT_ID + "=?", new String[]{String.valueOf(localId)});
            } catch (Exception e2) {
                Log.e(TAG, "Error crítico al marcar hábito como sincronizado", e2);
            }
        } finally {
            db.close();
        }
    }

    /**
     * Marca un hábito como no sincronizado (para operaciones offline)
     */
    public void markHabitAsUnsynced(long localId) {
        SQLiteDatabase db = this.getWritableDatabase();
        // ensureSyncColumns ya se ejecuta en getWritableDatabase(), pero lo llamamos de nuevo por seguridad
        ensureSyncColumns(db);
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_HABIT_SYNCED, 0);
            values.put(COLUMN_HABIT_UPDATED_AT, System.currentTimeMillis() / 1000);
            db.update(TABLE_HABITS, values, COLUMN_HABIT_ID + "=?", new String[]{String.valueOf(localId)});
        } catch (Exception e) {
            Log.e(TAG, "Error al marcar hábito como no sincronizado", e);
            // Si falla, intentar agregar las columnas y reintentar
            ensureSyncColumns(db);
            try {
                ContentValues values = new ContentValues();
                values.put(COLUMN_HABIT_SYNCED, 0);
                values.put(COLUMN_HABIT_UPDATED_AT, System.currentTimeMillis() / 1000);
                db.update(TABLE_HABITS, values, COLUMN_HABIT_ID + "=?", new String[]{String.valueOf(localId)});
            } catch (Exception e2) {
                Log.e(TAG, "Error crítico al marcar hábito como no sincronizado", e2);
            }
        } finally {
            db.close();
        }
    }

    /**
     * Obtiene un hábito por serverId
     * @param serverId El ID del servidor
     * @param db Instancia opcional de SQLiteDatabase. Si se proporciona, no se cerrará.
     * @return El hábito encontrado o null
     */
    public Habit getHabitByServerId(long serverId) {
        return getHabitByServerId(serverId, null);
    }
    
    /**
     * Obtiene un hábito por serverId usando una instancia de base de datos existente
     * @param serverId El ID del servidor
     * @param db Instancia opcional de SQLiteDatabase. Si se proporciona, no se cerrará.
     * @return El hábito encontrado o null
     */
    private Habit getHabitByServerId(long serverId, SQLiteDatabase db) {
        boolean shouldClose = false;
        if (db == null) {
            db = this.getReadableDatabase();
            shouldClose = true;
        }
        ensureSyncColumns(db);
        
        // CRÍTICO: Filtrar también por userId para asegurar que solo se obtengan hábitos del usuario actual
        long currentUserId = getCurrentUserId();
        Cursor cursor = db.query(TABLE_HABITS, null, 
                COLUMN_HABIT_SERVER_ID + "=? AND " + COLUMN_HABIT_USER_ID + "=?",
                new String[]{String.valueOf(serverId), String.valueOf(currentUserId)}, null, null, null);

        Habit habit = null;
        if (cursor.moveToFirst()) {
            habit = cursorToHabit(cursor);
        }
        cursor.close();
        if (shouldClose) {
            db.close();
        }
        return habit;
    }

    /**
     * Actualiza o inserta un hábito desde el servidor
     * Verifica si existe por serverId, si existe actualiza, si no inserta
     * También busca hábitos locales sin serverId que coincidan para evitar duplicados
     */
    public long upsertHabitFromServer(Habit habit, long serverId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ensureSyncColumns(db);
        
        // Obtener el userId actual del usuario logueado
        long currentUserId = getCurrentUserId();
        
        // 1. Verificar si ya existe por serverId (pasar db para no cerrarla)
        Habit existing = getHabitByServerId(serverId, db);
        
        // 2. Si no existe por serverId, buscar hábitos locales sin serverId que coincidan
        // Esto evita crear duplicados cuando un hábito local se sube al servidor
        if (existing == null) {
            // Buscar hábitos locales sin serverId que tengan el mismo título y tipo
            // y pertenezcan al usuario actual
            String selection = COLUMN_HABIT_SERVER_ID + " IS NULL AND " +
                              COLUMN_HABIT_TITLE + "=? AND " +
                              COLUMN_HABIT_TYPE + "=? AND " +
                              COLUMN_HABIT_USER_ID + "=?";
            String[] selectionArgs = new String[]{
                habit.getTitle(),
                habit.getType().name(),
                String.valueOf(currentUserId)
            };
            
            Cursor cursor = db.query(TABLE_HABITS, null, selection, selectionArgs, null, null, null, "1");
            if (cursor != null && cursor.moveToFirst()) {
                // Encontrar un hábito local sin serverId que coincide
                long localId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_HABIT_ID));
                // Obtener el hábito completo usando cursorToHabit
                existing = cursorToHabit(cursor);
                cursor.close();
                Log.d(TAG, "Encontrado hábito local sin serverId que coincide: " + existing.getTitle() + " (localId: " + localId + ")");
            } else if (cursor != null) {
                cursor.close();
            }
        }
        
        ContentValues values = new ContentValues();
        values.put(COLUMN_HABIT_TITLE, habit.getTitle());
        values.put(COLUMN_HABIT_GOAL, habit.getGoal());
        values.put(COLUMN_HABIT_CATEGORY, habit.getCategory());
        values.put(COLUMN_HABIT_TYPE, habit.getType().name());
        values.put(COLUMN_HABIT_COMPLETED, habit.isCompleted() ? 1 : 0);
        values.put(COLUMN_HABIT_POINTS, habit.getPoints());
        values.put(COLUMN_HABIT_TARGET_VALUE, habit.getTargetValue());
        if (habit.getTargetUnit() != null) values.put(COLUMN_HABIT_TARGET_UNIT, habit.getTargetUnit());
        if (habit.getPagesPerDay() != null) values.put(COLUMN_HABIT_PAGES_PER_DAY, habit.getPagesPerDay());
        if (habit.getReminderTimes() != null) values.put(COLUMN_HABIT_REMINDER_TIMES, habit.getReminderTimes());
        if (habit.getDurationMinutes() != null) values.put(COLUMN_HABIT_DURATION_MINUTES, habit.getDurationMinutes());
        if (habit.getDndMode() != null) values.put(COLUMN_HABIT_DND_MODE, habit.getDndMode() ? 1 : 0);
        if (habit.getMusicId() != null) values.put(COLUMN_HABIT_MUSIC_ID, habit.getMusicId());
        if (habit.getJournalEnabled() != null) values.put(COLUMN_HABIT_JOURNAL_ENABLED, habit.getJournalEnabled() ? 1 : 0);
        if (habit.getGymDays() != null) values.put(COLUMN_HABIT_GYM_DAYS, habit.getGymDays());
        if (habit.getWaterGoalGlasses() != null) values.put(COLUMN_HABIT_WATER_GOAL_GLASSES, habit.getWaterGoalGlasses());
        if (habit.getOneClickComplete() != null) values.put(COLUMN_HABIT_ONE_CLICK_COMPLETE, habit.getOneClickComplete() ? 1 : 0);
        if (habit.getEnglishMode() != null) values.put(COLUMN_HABIT_ENGLISH_MODE, habit.getEnglishMode() ? 1 : 0);
        if (habit.getCodingMode() != null) values.put(COLUMN_HABIT_CODING_MODE, habit.getCodingMode() ? 1 : 0);
        if (habit.getHabitIcon() != null) values.put(COLUMN_HABIT_ICON, habit.getHabitIcon());
        
        // IMPORTANTE: Establecer el userId del usuario actual
        values.put(COLUMN_HABIT_USER_ID, currentUserId);
        
        values.put(COLUMN_HABIT_SYNCED, 1);
        values.put(COLUMN_HABIT_SERVER_ID, serverId);
        values.put(COLUMN_HABIT_UPDATED_AT, System.currentTimeMillis() / 1000);
        
        long id;
        if (existing != null) {
            // Actualizar existente (ya sea por serverId o por coincidencia local)
            id = existing.getId();
            db.update(TABLE_HABITS, values, COLUMN_HABIT_ID + "=?", new String[]{String.valueOf(id)});
            Log.d(TAG, "Hábito actualizado desde servidor: " + habit.getTitle() + " (localId: " + id + ", serverId: " + serverId + ")");
        } else {
            // Insertar nuevo usando insertHabitFull
            id = insertHabitFull(
                habit.getTitle(),
                habit.getGoal(),
                habit.getCategory(),
                habit.getType().name(),
                habit.getPoints(),
                habit.getTargetValue() != 0 ? habit.getTargetValue() : null,
                habit.getTargetUnit(),
                habit.getPagesPerDay(),
                habit.getReminderTimes(),
                habit.getDurationMinutes(),
                habit.getDndMode(),
                habit.getMusicId(),
                habit.getJournalEnabled(),
                habit.getGymDays(),
                habit.getWaterGoalGlasses(),
                habit.getOneClickComplete(),
                habit.getEnglishMode(),
                habit.getCodingMode(),
                habit.getHabitIcon()
            );
            // Marcar como sincronizado
            markHabitAsSynced(id, serverId);
            Log.d(TAG, "Hábito nuevo insertado desde servidor: " + habit.getTitle() + " (localId: " + id + ", serverId: " + serverId + ")");
        }
        
        db.close();
        return id;
    }

    /**
     * Obtiene el serverId de un hábito local
     */
    public Long getServerId(long localId) {
        SQLiteDatabase db = this.getReadableDatabase();
        ensureSyncColumns(db);
        Cursor cursor = db.query(TABLE_HABITS, new String[]{COLUMN_HABIT_SERVER_ID}, 
                COLUMN_HABIT_ID + "=?", new String[]{String.valueOf(localId)}, null, null, null);
        
        Long serverId = null;
        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            serverId = cursor.getLong(0);
        }
        cursor.close();
        db.close();
        return serverId;
    }

    /**
     * Verifica si un hábito está sincronizado
     */
    public boolean isHabitSynced(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        ensureSyncColumns(db);
        Cursor cursor = db.query(TABLE_HABITS, new String[]{COLUMN_HABIT_SYNCED}, 
                COLUMN_HABIT_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        
        boolean synced = false;
        if (cursor.moveToFirst()) {
            synced = cursor.getInt(0) == 1;
        }
        cursor.close();
        db.close();
        return synced;
    }

    // ========== MÉTODOS PARA OPERACIONES PENDIENTES ==========

    /**
     * Guarda una operación pendiente
     */
    public long savePendingOperation(String operationType, String entityType, long entityId, String entityData) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        // Asegurar que la tabla exista
        ensurePendingOperationsTable(db);
        
        ContentValues values = new ContentValues();
        values.put(COLUMN_PENDING_OPERATION_TYPE, operationType);
        values.put(COLUMN_PENDING_ENTITY_TYPE, entityType);
        values.put(COLUMN_PENDING_ENTITY_ID, entityId);
        values.put(COLUMN_PENDING_ENTITY_DATA, entityData);
        values.put(COLUMN_PENDING_CREATED_AT, System.currentTimeMillis() / 1000);
        values.put(COLUMN_PENDING_PRIORITY, 2); // Prioridad media por defecto
        
        long id = db.insert(TABLE_PENDING_OPERATIONS, null, values);
        db.close();
        return id;
    }

    /**
     * Obtiene todas las operaciones pendientes
     */
    public List<PendingOperation> getAllPendingOperations() {
        List<PendingOperation> operations = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        // Asegurar que la tabla exista
        ensurePendingOperationsTable(db);
        
        Cursor cursor = db.query(TABLE_PENDING_OPERATIONS, null, null, null, null, null, 
                COLUMN_PENDING_PRIORITY + " ASC, " + COLUMN_PENDING_CREATED_AT + " ASC");

        if (cursor.moveToFirst()) {
            do {
                PendingOperation op = new PendingOperation();
                op.id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_PENDING_ID));
                op.operationType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PENDING_OPERATION_TYPE));
                op.entityType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PENDING_ENTITY_TYPE));
                op.entityId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_PENDING_ENTITY_ID));
                op.entityData = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PENDING_ENTITY_DATA));
                op.createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_PENDING_CREATED_AT));
                op.retryCount = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PENDING_RETRY_COUNT));
                op.lastError = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PENDING_LAST_ERROR));
                op.priority = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PENDING_PRIORITY));
                operations.add(op);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return operations;
    }

    /**
     * Elimina una operación pendiente
     */
    public void deletePendingOperation(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ensurePendingOperationsTable(db);
        db.delete(TABLE_PENDING_OPERATIONS, COLUMN_PENDING_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    /**
     * Incrementa el contador de reintentos
     */
    public void incrementRetryCount(long id, String error) {
        SQLiteDatabase db = this.getWritableDatabase();
        ensurePendingOperationsTable(db);
        ContentValues values = new ContentValues();
        Cursor cursor = db.query(TABLE_PENDING_OPERATIONS, new String[]{COLUMN_PENDING_RETRY_COUNT}, 
                COLUMN_PENDING_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        
        int retryCount = 0;
        if (cursor.moveToFirst()) {
            retryCount = cursor.getInt(0);
        }
        cursor.close();
        
        values.put(COLUMN_PENDING_RETRY_COUNT, retryCount + 1);
        values.put(COLUMN_PENDING_LAST_ERROR, error);
        db.update(TABLE_PENDING_OPERATIONS, values, COLUMN_PENDING_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    // ========== MÉTODOS AUXILIARES ==========

    private Habit cursorToHabit(Cursor cursor) {
        Habit habit = new Habit(
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HABIT_TITLE)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HABIT_GOAL)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HABIT_CATEGORY)),
                Habit.HabitType.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HABIT_TYPE)))
        );
        habit.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_HABIT_ID)));
        habit.setCompleted(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_HABIT_COMPLETED)) == 1);
        habit.setPoints(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_HABIT_POINTS)));
        
        // Cargar campos adicionales
        loadHabitExtraFields(cursor, habit);
        
        return habit;
    }


    private void loadHabitExtraFields(Cursor cursor, Habit habit) {
        try {
            // CRÍTICO: Cargar userId desde la BD
            int userIdIndex = cursor.getColumnIndex(COLUMN_HABIT_USER_ID);
            if (userIdIndex >= 0 && !cursor.isNull(userIdIndex)) {
                long userId = cursor.getLong(userIdIndex);
                habit.setUserId(userId);
                Log.d(TAG, "✅ userId cargado desde BD: " + userId + " para hábito: " + habit.getTitle());
            } else {
                Log.w(TAG, "⚠️ No se encontró columna user_id en cursor para hábito: " + habit.getTitle());
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
            Log.e(TAG, "Error al cargar campos adicionales", e);
        }
    }

    /**
     * Clase para representar una operación pendiente
     */
    public static class PendingOperation {
        public long id;
        public String operationType; // CREATE, UPDATE, DELETE
        public String entityType; // HABIT, SCORE
        public long entityId;
        public String entityData; // JSON
        public long createdAt;
        public int retryCount;
        public String lastError;
        public int priority;
    }
}

