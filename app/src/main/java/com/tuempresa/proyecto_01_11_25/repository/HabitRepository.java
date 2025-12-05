package com.tuempresa.proyecto_01_11_25.repository;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.tuempresa.proyecto_01_11_25.api.HabitApiHelper;
import com.tuempresa.proyecto_01_11_25.api.HabitCheckinApiHelper;
import com.tuempresa.proyecto_01_11_25.api.ScoreApiHelper;
import com.tuempresa.proyecto_01_11_25.database.HabitDatabaseHelperSync;
import com.tuempresa.proyecto_01_11_25.model.Habit;
import com.tuempresa.proyecto_01_11_25.model.HabitCheckinDto;
import com.tuempresa.proyecto_01_11_25.model.HabitCompletion;
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
    private final HabitCheckinApiHelper checkinApiHelper;
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
        this.checkinApiHelper = new HabitCheckinApiHelper(context);
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
                    habit.getWalkGoalMeters(),
                    habit.getWalkGoalSteps(),
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
                    habit.getWalkGoalMeters(),
                    habit.getWalkGoalSteps(),
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
                if (habit == null) {
                    callback.onError("Hábito no encontrado");
                    return;
                }
                
                // Obtener serverId ANTES de eliminar localmente
                Long serverId = dbHelper.getServerId(id);
                Log.d(TAG, "Eliminando hábito - localId: " + id + ", serverId: " + serverId);
                
                // Si tiene serverId y hay conexión, eliminar del servidor PRIMERO
                if (connectionMonitor.isConnected() && serverId != null && serverId > 0) {
                    Log.d(TAG, "Eliminando hábito del servidor primero (serverId: " + serverId + ")");
                    apiHelper.deleteHabit(serverId, new HabitApiHelper.OnHabitDeletedListener() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "✅ Hábito eliminado del servidor. Eliminando localmente...");
                            // Eliminar de base de datos local DESPUÉS de eliminar del servidor
                            boolean deleted = dbHelper.deleteHabit(id);
                            if (deleted) {
                                Log.d(TAG, "✅ Hábito eliminado localmente");
                                callback.onSuccess(null);
                            } else {
                                Log.e(TAG, "⚠️ Error al eliminar hábito localmente después de eliminarlo del servidor");
                                callback.onError("Error al eliminar hábito localmente");
                            }
                        }

                        @Override
                        public void onError(String error) {
                            Log.e(TAG, "❌ Error al eliminar hábito del servidor: " + error);
                            // Si falla la eliminación en el servidor, guardar como operación pendiente
                            // pero NO eliminar localmente todavía (para poder reintentar)
                            String habitJson = gson.toJson(habit);
                            dbHelper.savePendingOperation("DELETE", "HABIT", id, habitJson);
                            // Eliminar localmente de todas formas (el usuario ya lo pidió)
                            dbHelper.deleteHabit(id);
                            callback.onSuccess(null);
                        }
                    });
                } else {
                    // No hay conexión o no tiene serverId, eliminar localmente y guardar como pendiente
                    Log.d(TAG, "Eliminando hábito localmente (sin serverId o sin conexión)");
                    boolean deleted = dbHelper.deleteHabit(id);
                    if (deleted) {
                        // Guardar como operación pendiente para sincronizar después
                        String habitJson = gson.toJson(habit);
                        dbHelper.savePendingOperation("DELETE", "HABIT", id, habitJson);
                        Log.d(TAG, "✅ Hábito eliminado localmente y guardado como operación pendiente");
                        callback.onSuccess(null);
                    } else {
                        Log.e(TAG, "❌ Error al eliminar hábito localmente");
                        callback.onError("Error al eliminar hábito");
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "❌ Excepción al eliminar hábito", e);
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

    /**
     * Guarda un completado de hábito con ubicación GPS.
     * Guarda localmente primero, luego sincroniza con la API si hay conexión.
     * @param habitId ID del hábito (local)
     * @param latitude Latitud GPS
     * @param longitude Longitud GPS
     * @param note Nota opcional
     * @param callback Callback para manejar la respuesta
     */
    public void saveHabitCompletion(long habitId, double latitude, double longitude, String note, RepositoryCallback<Void> callback) {
        executorService.execute(() -> {
            try {
                long userId = sessionManager.getUserId();
                if (userId <= 0) {
                    callback.onError("Usuario no autenticado");
                    return;
                }

                // Obtener información del hábito
                Habit habit = dbHelper.getHabitById(habitId);
                if (habit == null) {
                    callback.onError("Hábito no encontrado");
                    return;
                }

                // Obtener serverId del hábito
                Long serverHabitId = dbHelper.getServerId(habitId);
                if (serverHabitId == null || serverHabitId <= 0) {
                    // Si no tiene serverId, guardar solo localmente
                    boolean saved = dbHelper.saveHabitCompletion(habitId, userId, latitude, longitude);
                    if (saved) {
                        callback.onSuccess(null);
                    } else {
                        callback.onError("Error al guardar completado localmente");
                    }
                    return;
                }

                // 1. Guardar en base de datos local (SQLite)
                boolean saved = dbHelper.saveHabitCompletion(habitId, userId, latitude, longitude);
                if (!saved) {
                    callback.onError("Error al guardar completado localmente");
                    return;
                }

                // Notificar éxito inmediatamente
                callback.onSuccess(null);

                // 2. Si hay conexión, sincronizar con servidor
                if (connectionMonitor.isConnected()) {
                    HabitCheckinDto checkin = new HabitCheckinDto();
                    checkin.setHabitId(serverHabitId);
                    // NO enviar userId - el backend lo obtiene del token JWT
                    checkin.setLatitude(latitude);
                    checkin.setLongitude(longitude);
                    checkin.setNote(note != null ? note : habit.getTitle() + " completado");
                    checkin.setPointsAwarded(habit.getPoints()); // Enviar puntos del hábito

                    checkinApiHelper.createCheckin(checkin, new HabitCheckinApiHelper.OnCheckinSavedListener() {
                        @Override
                        public void onSuccess(HabitCheckinDto createdCheckin) {
                            Log.d(TAG, "Completado sincronizado con servidor: " + createdCheckin.getId());
                        }

                        @Override
                        public void onError(String error) {
                            Log.e(TAG, "Error al sincronizar completado con servidor: " + error);
                            // Guardar como operación pendiente
                            String checkinJson = gson.toJson(checkin);
                            dbHelper.savePendingOperation("CREATE", "CHECKIN", habitId, checkinJson);
                        }
                    });
                } else {
                    // Guardar como operación pendiente si no hay conexión
                    HabitCheckinDto checkin = new HabitCheckinDto();
                    checkin.setHabitId(serverHabitId);
                    // NO enviar userId - el backend lo obtiene del token JWT
                    checkin.setLatitude(latitude);
                    checkin.setLongitude(longitude);
                    checkin.setNote(note != null ? note : habit.getTitle() + " completado");
                    checkin.setPointsAwarded(habit.getPoints()); // Enviar puntos del hábito
                    String checkinJson = gson.toJson(checkin);
                    dbHelper.savePendingOperation("CREATE", "CHECKIN", habitId, checkinJson);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error al guardar completado", e);
                callback.onError(e.getMessage());
            }
        });
    }

    /**
     * Elimina el completado de HOY para un hábito específico.
     * Elimina localmente primero, luego sincroniza con la API si hay conexión.
     * @param habitId ID del hábito (local)
     * @param callback Callback para manejar la respuesta
     */
    public void deleteHabitCompletion(long habitId, RepositoryCallback<Void> callback) {
        executorService.execute(() -> {
            try {
                long userId = sessionManager.getUserId();
                if (userId <= 0) {
                    callback.onError("Usuario no autenticado");
                    return;
                }

                // Obtener serverId del hábito
                Long serverHabitId = dbHelper.getServerId(habitId);

                // 1. Eliminar de base de datos local
                dbHelper.deleteCompletion(habitId, userId);

                // Notificar éxito inmediatamente
                callback.onSuccess(null);

                // 2. Si hay conexión y tiene serverId, eliminar del servidor
                if (connectionMonitor.isConnected() && serverHabitId != null && serverHabitId > 0) {
                    checkinApiHelper.deleteTodayCheckin(serverHabitId, new HabitCheckinApiHelper.OnCheckinDeletedListener() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "Completado eliminado del servidor");
                        }

                        @Override
                        public void onError(String error) {
                            Log.e(TAG, "Error al eliminar completado del servidor: " + error);
                            // Guardar como operación pendiente
                            HabitCheckinDto checkin = new HabitCheckinDto();
                            checkin.setHabitId(serverHabitId);
                            String checkinJson = gson.toJson(checkin);
                            dbHelper.savePendingOperation("DELETE", "CHECKIN", habitId, checkinJson);
                        }
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error al eliminar completado", e);
                callback.onError(e.getMessage());
            }
        });
    }

    /**
     * Obtiene todos los completados de HOY para el usuario actual.
     * Primero obtiene de local, luego sincroniza con el servidor si hay conexión.
     * @param callback Callback para manejar la respuesta
     */
    public void getTodayCompletions(RepositoryCallback<List<HabitCompletion>> callback) {
        executorService.execute(() -> {
            try {
                long userId = sessionManager.getUserId();
                if (userId <= 0) {
                    // Notificar error en el hilo principal
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        callback.onError("Usuario no autenticado");
                    });
                    return;
                }

                // 1. Obtener de base de datos local inmediatamente
                List<HabitCompletion> completions = dbHelper.getTodayCompletions(userId);

                // Notificar inmediatamente con datos locales (en el hilo principal)
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    callback.onSuccess(completions);
                });

                // 2. Si hay conexión, sincronizar con servidor en segundo plano
                if (connectionMonitor.isConnected()) {
                    checkinApiHelper.getTodayCheckins(new HabitCheckinApiHelper.OnCheckinsReceivedListener() {
                        @Override
                        public void onSuccess(List<HabitCheckinDto> serverCheckins) {
                            executorService.execute(() -> {
                                Log.d(TAG, "✅ Completados de hoy cargados del servidor: " + serverCheckins.size());
                                
                                // Convertir DTOs del servidor a HabitCompletion y guardar en local
                                for (HabitCheckinDto dto : serverCheckins) {
                                    // Buscar el habitId local correspondiente al serverHabitId
                                    Long localHabitId = dbHelper.getLocalHabitIdByServerId(dto.getHabitId());
                                    
                                    if (localHabitId == null || localHabitId <= 0) {
                                        // El hábito no existe localmente, intentar sincronizarlo primero
                                        Log.w(TAG, "⚠️ Hábito con serverId " + dto.getHabitId() + " no encontrado localmente. Intentando sincronizar hábitos...");
                                        
                                        // Intentar obtener el hábito del servidor
                                        apiHelper.getHabitById(dto.getHabitId(), new HabitApiHelper.OnHabitLoadedListener() {
                                            @Override
                                            public void onSuccess(Habit habit) {
                                                executorService.execute(() -> {
                                                    // Guardar el hábito localmente usando insertHabitFull
                                                    long newLocalHabitId = dbHelper.insertHabitFull(
                                                        habit.getTitle(),
                                                        habit.getGoal(),
                                                        habit.getCategory(),
                                                        habit.getType().name(),
                                                        habit.getPoints(),
                                                        habit.getTargetValue(),
                                                        habit.getTargetUnit(),
                                                        habit.getPagesPerDay(),
                                                        habit.getReminderTimes(),
                                                        habit.getDurationMinutes(),
                                                        habit.isDndMode(),
                                                        habit.getMusicId(),
                                                        habit.isJournalEnabled(),
                                                        habit.getGymDays(),
                                                        habit.getWaterGoalGlasses(),
                                                        habit.getWalkGoalMeters(),
                                                        habit.getWalkGoalSteps(),
                                                        habit.isOneClickComplete(),
                                                        habit.isEnglishMode(),
                                                        habit.isCodingMode(),
                                                        habit .getHabitIcon()
                                                    );
                                                    if (newLocalHabitId > 0) {
                                                        // Marcar como sincronizado
                                                        dbHelper.markHabitAsSynced(newLocalHabitId, dto.getHabitId());
                                                        
                                                        // Ahora guardar el completado
                                                        if (!dbHelper.hasCompletionToday(newLocalHabitId, userId)) {
                                                            dbHelper.saveHabitCompletion(
                                                                newLocalHabitId,
                                                                userId,
                                                                dto.getLatitude() != null ? dto.getLatitude() : 0.0,
                                                                dto.getLongitude() != null ? dto.getLongitude() : 0.0
                                                            );
                                                            Log.d(TAG, "✅ Completado guardado después de sincronizar hábito: " + habit.getTitle());
                                                        }
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onError(String error) {
                                                Log.e(TAG, "❌ Error al obtener hábito del servidor (serverId=" + dto.getHabitId() + "): " + error);
                                                // Continuar con el siguiente checkin
                                            }
                                        });
                                    } else {
                                        // El hábito existe localmente, guardar el completado si no existe
                                        if (!dbHelper.hasCompletionToday(localHabitId, userId)) {
                                            // Obtener información del hábito local
                                            Habit habit = dbHelper.getHabitById(localHabitId);
                                            if (habit != null) {
                                                dbHelper.saveHabitCompletion(
                                                    localHabitId,
                                                    userId,
                                                    dto.getLatitude() != null ? dto.getLatitude() : 0.0,
                                                    dto.getLongitude() != null ? dto.getLongitude() : 0.0
                                                );
                                                Log.d(TAG, "✅ Completado guardado: " + habit.getTitle());
                                            }
                                        }
                                    }
                                }

                                // Esperar un poco para que se completen las sincronizaciones de hábitos
                                try {
                                    Thread.sleep(500); // 500ms para dar tiempo a las llamadas asíncronas
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                }

                                // Obtener lista actualizada
                                List<HabitCompletion> updatedCompletions = dbHelper.getTodayCompletions(userId);
                                
                                // Notificar en el hilo principal para actualizar UI
                                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                                    callback.onSuccess(updatedCompletions);
                                });
                            });
                        }

                        @Override
                        public void onError(String error) {
                            Log.e(TAG, "Error al obtener completados del servidor: " + error);
                            // No notificar error, ya tenemos datos locales
                        }
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error al obtener completados", e);
                // Notificar error en el hilo principal
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    callback.onError(e.getMessage());
                });
            }
        });
    }
}

