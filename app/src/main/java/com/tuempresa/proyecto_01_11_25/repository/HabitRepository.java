package com.tuempresa.proyecto_01_11_25.repository;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.tuempresa.proyecto_01_11_25.api.HabitApiHelper;
import com.tuempresa.proyecto_01_11_25.api.ScoreApiHelper;
import com.tuempresa.proyecto_01_11_25.database.HabitDatabaseHelperSync;
import com.tuempresa.proyecto_01_11_25.model.Habit;
import com.tuempresa.proyecto_01_11_25.model.Score;
import com.tuempresa.proyecto_01_11_25.network.ConnectionMonitor;
import com.tuempresa.proyecto_01_11_25.sync.SyncManager;
import com.tuempresa.proyecto_01_11_25.utils.SessionManager;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository que unifica el acceso a datos locales (Room) y remotos (API).
 * Implementa el patrón Repository para abstraer el origen de datos.
 */
public class  HabitRepository {
    private static final String TAG = "HabitRepository";
    private static HabitRepository instance;
    
    private final Context context;
    private final HabitDatabaseHelperSync dbHelper;
    private final HabitApiHelper apiHelper;
    private final ScoreApiHelper scoreApiHelper;
    private final ConnectionMonitor connectionMonitor;
    private final SyncManager syncManager;
    private final ExecutorService executorService;
    private final Gson gson;
    private final SessionManager sessionManager;

    public interface RepositoryCallback<T> {
        void onSuccess(T data);
        void onError(String error);
    }

    private HabitRepository(Context context) {
        this.context = context.getApplicationContext();
        this.dbHelper = new HabitDatabaseHelperSync(context);
        this.apiHelper = new HabitApiHelper(context);
        this.scoreApiHelper = new ScoreApiHelper(context);
        this.connectionMonitor = ConnectionMonitor.getInstance(context);
        this.syncManager = SyncManager.getInstance(context);
        this.executorService = Executors.newSingleThreadExecutor();
        this.gson = new Gson();
        this.sessionManager = new SessionManager(context);
        
        // Agregar listener para sincronización automática cuando se restaure la conexión
        this.connectionMonitor.addListener(new ConnectionMonitor.ConnectionListener() {
            private boolean wasConnected = connectionMonitor.isConnected();
            
            @Override
            public void onConnectionChanged(boolean isConnected) {
                // Si se restaura la conexión (pasó de desconectado a conectado), sincronizar
                if (!wasConnected && isConnected) {
                    Log.d(TAG, "Conexión restaurada, iniciando sincronización automática...");
                    forceSync();
                }
                wasConnected = isConnected;
            }
        });
    }

    public static synchronized HabitRepository getInstance(Context context) {
        if (instance == null) {
            instance = new HabitRepository(context);
        }
        return instance;
    }

    /**
     * Obtiene todos los hábitos (primero desde local, luego sincroniza si hay conexión).
     */
    public void getAllHabits(RepositoryCallback<List<Habit>> callback) {
        executorService.execute(() -> {
            try {
                // 1. Obtener de base de datos local inmediatamente (SQLite)
                List<Habit> habits = dbHelper.getAllHabits();
                
                // Notificar inmediatamente con datos locales
                callback.onSuccess(habits);
                
                // 2. Si hay conexión, sincronizar en segundo plano
                if (connectionMonitor.isConnected()) {
                    syncManager.syncAll(new SyncManager.SyncListener() {
                        @Override
                        public void onSyncStarted() {
                            Log.d(TAG, "Sincronización iniciada");
                        }

                        @Override
                        public void onSyncCompleted(int syncedCount) {
                            // Actualizar lista después de sincronizar
                            executorService.execute(() -> {
                                List<Habit> updatedHabits = dbHelper.getAllHabits();
                                Log.d(TAG, "Sincronización completada. Hábitos actualizados: " + updatedHabits.size());
                                // Notificar en el hilo principal para actualizar UI
                                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                                    callback.onSuccess(updatedHabits);
                                });
                            });
                        }

                        @Override
                        public void onSyncError(String error) {
                            Log.e(TAG, "Error en sincronización: " + error);
                        }
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error al obtener hábitos", e);
                callback.onError(e.getMessage());
            }
        });
    }

    /**
     * Obtiene un hábito por ID.
     */
    public void getHabitById(long id, RepositoryCallback<Habit> callback) {
        executorService.execute(() -> {
            try {
                Habit habit = dbHelper.getHabitById(id);
                if (habit != null) {
                    callback.onSuccess(habit);
                } else {
                    callback.onError("Hábito no encontrado");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error al obtener hábito", e);
                callback.onError(e.getMessage());
            }
        });
    }

    /**
     * Crea un nuevo hábito (local primero, luego sincroniza si hay conexión).
     */
    public void createHabit(Habit habit, RepositoryCallback<Habit> callback) {
        executorService.execute(() -> {
            try {
                // Agregar userId antes de guardar
                long userId = sessionManager.getUserId();
                if (userId > 0) {
                    habit.setUserId(userId);
                }
                
                // 1. Guardar en base de datos local (SQLite)
                long localId = dbHelper.insertHabitFull(
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
                
                habit.setId(localId);
                
                // 2. Si hay conexión, sincronizar con servidor PRIMERO
                // No notificar éxito hasta que se sincronice (o se marque como pendiente)
                if (connectionMonitor.isConnected()) {
                    // Sincronizar y notificar éxito cuando termine
                    syncHabitToServer(habit, localId, callback);
                } else {
                    // Marcar como no sincronizado y guardar como operación pendiente
                    dbHelper.markHabitAsUnsynced(localId);
                    String habitJson = gson.toJson(habit);
                    dbHelper.savePendingOperation("CREATE", "HABIT", localId, habitJson);
                    Log.d(TAG, "Hábito creado offline, marcado como no sincronizado: " + localId);
                    // Notificar éxito solo cuando está offline (no se sincronizará ahora)
                    callback.onSuccess(habit);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error al crear hábito", e);
                callback.onError(e.getMessage());
            }
        });
    }

    /**
     * Actualiza un hábito existente.
     */
    public void updateHabit(Habit habit, RepositoryCallback<Habit> callback) {
        executorService.execute(() -> {
            try {
                // Actualizar en base de datos local
                boolean updated = dbHelper.updateHabitFull(
                    habit.getId(),
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
                
                if (!updated) {
                    callback.onError("Hábito no encontrado");
                    return;
                }
                
                callback.onSuccess(habit);
                
                if (connectionMonitor.isConnected()) {
                    syncHabitToServer(habit, habit.getId(), callback);
                } else {
                    // Marcar como no sincronizado y guardar como operación pendiente
                    dbHelper.markHabitAsUnsynced(habit.getId());
                    String habitJson = gson.toJson(habit);
                    dbHelper.savePendingOperation("UPDATE", "HABIT", habit.getId(), habitJson);
                    Log.d(TAG, "Hábito actualizado offline, marcado como no sincronizado: " + habit.getId());
                }
            } catch (Exception e) {
                Log.e(TAG, "Error al actualizar hábito", e);
                callback.onError(e.getMessage());
            }
        });
    }

    /**
     * Elimina un hábito.
     */
    public void deleteHabit(long id, RepositoryCallback<Void> callback) {
        executorService.execute(() -> {
            try {
                Habit habit = dbHelper.getHabitById(id);
                if (habit != null) {
                    // Eliminar de base de datos local
                    dbHelper.deleteHabit(id);
                    
                    // Obtener serverId si existe
                    Long serverId = dbHelper.getServerId(id);
                    
                    if (connectionMonitor.isConnected() && serverId != null && serverId > 0) {
                        // Eliminar del servidor
                        apiHelper.deleteHabit(serverId, new HabitApiHelper.OnHabitDeletedListener() {
                            @Override
                            public void onSuccess() {
                                callback.onSuccess(null);
                            }

                            @Override
                            public void onError(String error) {
                                Log.e(TAG, "Error al eliminar hábito del servidor: " + error);
                                // Guardar como operación pendiente
                                String habitJson = gson.toJson(habit);
                                dbHelper.savePendingOperation("DELETE", "HABIT", id, habitJson);
                                callback.onSuccess(null);
                            }
                        });
                    } else {
                        // Guardar como operación pendiente
                        String habitJson = gson.toJson(habit);
                        dbHelper.savePendingOperation("DELETE", "HABIT", id, habitJson);
                        callback.onSuccess(null);
                    }
                } else {
                    callback.onError("Hábito no encontrado");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error al eliminar hábito", e);
                callback.onError(e.getMessage());
            }
        });
    }

    private void syncHabitToServer(Habit habit, long localId, RepositoryCallback<Habit> callback) {
        // CRÍTICO: Asegurar que el userId esté establecido antes de enviar al servidor
        long userId = sessionManager.getUserId();
        if (userId > 0) {
            habit.setUserId(userId);
            Log.d(TAG, "Sincronizando hábito con userId: " + userId);
        } else {
            Log.e(TAG, "⚠️ No se puede sincronizar: userId no válido (" + userId + ")");
            callback.onError("Usuario no autenticado");
            return;
        }
        
        Long serverId = dbHelper.getServerId(localId);
        
        if (serverId != null && serverId > 0) {
            // Actualizar
            apiHelper.updateHabit(serverId, habit, new HabitApiHelper.OnHabitSavedListener() {
                @Override
                public void onSuccess(Habit updatedHabit) {
                    executorService.execute(() -> {
                        // Marcar como sincronizado
                        dbHelper.markHabitAsSynced(localId, updatedHabit.getId());
                        callback.onSuccess(updatedHabit);
                    });
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "Error al sincronizar hábito: " + error);
                    executorService.execute(() -> {
                        // Marcar como no sincronizado y guardar como operación pendiente
                        dbHelper.markHabitAsUnsynced(localId);
                        String habitJson = gson.toJson(habit);
                        dbHelper.savePendingOperation("UPDATE", "HABIT", localId, habitJson);
                    });
                    callback.onError(error);
                }
            });
        } else {
            // Crear
            apiHelper.createHabit(habit, new HabitApiHelper.OnHabitSavedListener() {
                @Override
                public void onSuccess(Habit createdHabit) {
                    executorService.execute(() -> {
                        try {
                            // CRÍTICO: Marcar como sincronizado ANTES de notificar éxito
                            // Esto previene que refreshHabitsList() descargue el hábito y cree un duplicado
                            dbHelper.markHabitAsSynced(localId, createdHabit.getId());
                            Log.d(TAG, "Hábito sincronizado: localId=" + localId + ", serverId=" + createdHabit.getId());
                            
                            // Actualizar el hábito local con el serverId antes de notificar
                            createdHabit.setId(localId); // Mantener el localId para la app
                            
                            // Notificar éxito DESPUÉS de marcar como sincronizado
                            callback.onSuccess(createdHabit);
                        } catch (Exception e) {
                            Log.e(TAG, "Error al marcar hábito como sincronizado", e);
                            callback.onError("Error al sincronizar hábito: " + e.getMessage());
                        }
                    });
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "Error al crear hábito en servidor: " + error);
                    executorService.execute(() -> {
                        // Marcar como no sincronizado y guardar como operación pendiente
                        dbHelper.markHabitAsUnsynced(localId);
                        String habitJson = gson.toJson(habit);
                        dbHelper.savePendingOperation("CREATE", "HABIT", localId, habitJson);
                        // Notificar éxito de todas formas (está guardado localmente)
                        callback.onSuccess(habit);
                    });
                }
            });
        }
    }

    /**
     * Fuerza una sincronización inmediata con el servidor.
     * Útil para refrescar datos cuando el usuario lo solicita.
     */
    public void forceSync() {
        if (connectionMonitor.isConnected()) {
            Log.d(TAG, "Forzando sincronización...");
            syncManager.syncAll(new SyncManager.SyncListener() {
                @Override
                public void onSyncStarted() {
                    Log.d(TAG, "Sincronización forzada iniciada");
                }

                @Override
                public void onSyncCompleted(int syncedCount) {
                    Log.d(TAG, "Sincronización forzada completada: " + syncedCount + " elementos");
                }

                @Override
                public void onSyncError(String error) {
                    Log.e(TAG, "Error en sincronización forzada: " + error);
                }
            });
        } else {
            Log.d(TAG, "Sin conexión, no se puede sincronizar");
        }
    }

    /**
     * Guarda un score (progreso) cuando se completa un hábito.
     * Guarda localmente primero, luego sincroniza con la API si hay conexión.
     */
    public void addScore(long habitId, String habitTitle, int points, RepositoryCallback<Void> callback) {
        executorService.execute(() -> {
            try {
                // 1. Guardar en base de datos local (SQLite)
                long localScoreId = dbHelper.addScore(habitTitle, points);
                
                // Obtener serverId del hábito
                Long serverHabitId = dbHelper.getServerId(habitId);
                
                // Notificar éxito inmediatamente
                callback.onSuccess(null);
                
                // 2. Si hay conexión y el hábito tiene serverId, sincronizar con servidor
                if (connectionMonitor.isConnected() && serverHabitId != null && serverHabitId > 0) {
                    Score score = new Score(serverHabitId, habitTitle, points);
                    // Agregar userId al score
                    long userId = sessionManager.getUserId();
                    if (userId > 0) {
                        score.setUserId(userId);
                    }
                    scoreApiHelper.createScore(score, new ScoreApiHelper.OnScoreSavedListener() {
                        @Override
                        public void onSuccess(Score createdScore) {
                            Log.d(TAG, "Score guardado en servidor: " + createdScore.getId());
                            // El score local ya está guardado, no necesitamos actualizar nada
                        }

                        @Override
                        public void onError(String error) {
                            Log.e(TAG, "Error al guardar score en servidor: " + error);
                            // Guardar como operación pendiente
                            String scoreJson = gson.toJson(score);
                            dbHelper.savePendingOperation("CREATE", "SCORE", localScoreId, scoreJson);
                        }
                    });
                } else {
                    // Guardar como operación pendiente si no hay conexión o no tiene serverId
                    Score score = new Score(serverHabitId != null ? serverHabitId : habitId, habitTitle, points);
                    String scoreJson = gson.toJson(score);
                    dbHelper.savePendingOperation("CREATE", "SCORE", localScoreId, scoreJson);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error al guardar score", e);
                callback.onError(e.getMessage());
            }
        });
    }
}

