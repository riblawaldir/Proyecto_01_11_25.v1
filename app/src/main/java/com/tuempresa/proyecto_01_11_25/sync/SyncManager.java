package com.tuempresa.proyecto_01_11_25.sync;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.tuempresa.proyecto_01_11_25.api.HabitApiHelper;
import com.tuempresa.proyecto_01_11_25.database.HabitDatabaseHelperSync;
import com.tuempresa.proyecto_01_11_25.model.Habit;
import com.tuempresa.proyecto_01_11_25.network.ConnectionMonitor;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Gestor de sincronización entre la base de datos local y la API remota.
 * Maneja la sincronización bidireccional y resolución de conflictos.
 */
public class SyncManager {
    private static final String TAG = "SyncManager";
    private static SyncManager instance;
    
    private final Context context;
    private final HabitDatabaseHelperSync dbHelper;
    private final HabitApiHelper apiHelper;
    private final ConnectionMonitor connectionMonitor;
    private final ExecutorService executorService;
    private final Gson gson;
    
    private boolean isSyncing = false;

    public interface SyncListener {
        void onSyncStarted();
        void onSyncCompleted(int syncedCount);
        void onSyncError(String error);
    }

    private SyncManager(Context context) {
        this.context = context.getApplicationContext();
        this.dbHelper = new HabitDatabaseHelperSync(context);
        this.apiHelper = new HabitApiHelper();
        this.connectionMonitor = ConnectionMonitor.getInstance(context);
        this.executorService = Executors.newSingleThreadExecutor();
        this.gson = new Gson();
    }

    public static synchronized SyncManager getInstance(Context context) {
        if (instance == null) {
            instance = new SyncManager(context);
        }
        return instance;
    }

    /**
     * Sincroniza todos los datos pendientes con el servidor.
     */
    public void syncAll(SyncListener listener) {
        if (isSyncing) {
            Log.d(TAG, "Sincronización ya en progreso");
            return;
        }

        if (!connectionMonitor.isConnected()) {
            Log.d(TAG, "Sin conexión a la API, no se puede sincronizar");
            if (listener != null) {
                listener.onSyncError("Sin conexión a la API");
            }
            return;
        }

        executorService.execute(() -> {
            isSyncing = true;
            currentSyncListener = listener;
            if (listener != null) {
                listener.onSyncStarted();
            }

            try {
                int syncedCount = 0;

                // 1. Sincronizar hábitos no sincronizados
                syncedCount += syncHabits();

                // 2. Sincronizar scores no sincronizados
                syncedCount += syncScores();

                // 3. Procesar operaciones pendientes
                syncedCount += processPendingOperations();

                // 4. Descargar datos del servidor (esto es asíncrono, pero notificará cuando termine)
                downloadFromServer();

                // Nota: downloadFromServer es asíncrono, pero onSyncCompleted se llamará
                // después de que se complete la descarga en downloadFromServer
            } catch (Exception e) {
                isSyncing = false;
                Log.e(TAG, "Error en sincronización", e);
                if (listener != null) {
                    listener.onSyncError(e.getMessage());
                }
            }
        });
    }

    private int syncHabits() {
        int syncedCount = 0;
        List<Habit> unsyncedHabits = dbHelper.getUnsyncedHabits();
        
        for (Habit habit : unsyncedHabits) {
            try {
                Long serverId = dbHelper.getServerId(habit.getId());
                
                if (serverId != null && serverId > 0) {
                    // Actualizar hábito existente
                    apiHelper.updateHabit(serverId, habit, new HabitApiHelper.OnHabitSavedListener() {
                        @Override
                        public void onSuccess(Habit updatedHabit) {
                            // Marcar como sincronizado
                            dbHelper.markHabitAsSynced(habit.getId(), updatedHabit.getId());
                        }

                        @Override
                        public void onError(String error) {
                            Log.e(TAG, "Error al actualizar hábito: " + error);
                        }
                    });
                } else {
                    // Crear nuevo hábito
                    apiHelper.createHabit(habit, new HabitApiHelper.OnHabitSavedListener() {
                        @Override
                        public void onSuccess(Habit createdHabit) {
                            // Marcar como sincronizado y guardar serverId
                            dbHelper.markHabitAsSynced(habit.getId(), createdHabit.getId());
                        }

                        @Override
                        public void onError(String error) {
                            Log.e(TAG, "Error al crear hábito: " + error);
                        }
                    });
                }
                syncedCount++;
            } catch (Exception e) {
                Log.e(TAG, "Error al sincronizar hábito " + habit.getId(), e);
            }
        }
        
        return syncedCount;
    }

    private int syncScores() {
        // Similar a syncHabits pero para scores
        // Implementación simplificada
        return 0;
    }

    private int processPendingOperations() {
        int processedCount = 0;
        List<HabitDatabaseHelperSync.PendingOperation> pendingOps = dbHelper.getAllPendingOperations();
        
        for (HabitDatabaseHelperSync.PendingOperation op : pendingOps) {
            try {
                if (op.retryCount >= 3) {
                    // Demasiados reintentos, eliminar
                    dbHelper.deletePendingOperation(op.id);
                    continue;
                }

                // Procesar según el tipo de operación
                boolean success = processOperation(op);
                if (success) {
                    dbHelper.deletePendingOperation(op.id);
                    processedCount++;
                } else {
                    dbHelper.incrementRetryCount(op.id, "Error en procesamiento");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error al procesar operación pendiente", e);
                dbHelper.incrementRetryCount(op.id, e.getMessage());
            }
        }
        
        return processedCount;
    }

    private boolean processOperation(HabitDatabaseHelperSync.PendingOperation op) {
        try {
            if (op.entityType.equals("HABIT")) {
                Habit habit = gson.fromJson(op.entityData, Habit.class);
                
                if (op.operationType.equals("CREATE")) {
                    apiHelper.createHabit(habit, new HabitApiHelper.OnHabitSavedListener() {
                        @Override
                        public void onSuccess(Habit createdHabit) {
                            dbHelper.markHabitAsSynced(op.entityId, createdHabit.getId());
                        }

                        @Override
                        public void onError(String error) {
                            Log.e(TAG, "Error al procesar CREATE: " + error);
                        }
                    });
                    return true;
                } else if (op.operationType.equals("UPDATE")) {
                    Long serverId = dbHelper.getServerId(op.entityId);
                    if (serverId != null && serverId > 0) {
                        apiHelper.updateHabit(serverId, habit, new HabitApiHelper.OnHabitSavedListener() {
                            @Override
                            public void onSuccess(Habit updatedHabit) {
                                dbHelper.markHabitAsSynced(op.entityId, updatedHabit.getId());
                            }

                            @Override
                            public void onError(String error) {
                                Log.e(TAG, "Error al procesar UPDATE: " + error);
                            }
                        });
                        return true;
                    }
                } else if (op.operationType.equals("DELETE")) {
                    Long serverId = dbHelper.getServerId(op.entityId);
                    if (serverId != null && serverId > 0) {
                        apiHelper.deleteHabit(serverId, new HabitApiHelper.OnHabitDeletedListener() {
                            @Override
                            public void onSuccess() {
                                // Ya está eliminado localmente
                            }

                            @Override
                            public void onError(String error) {
                                Log.e(TAG, "Error al procesar DELETE: " + error);
                            }
                        });
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error al procesar operación", e);
            return false;
        }
    }

    private void downloadFromServer() {
        // Descargar hábitos del servidor y actualizar base de datos local (SQLite)
        apiHelper.getAllHabits(new HabitApiHelper.OnHabitsLoadedListener() {
            @Override
            public void onSuccess(List<Habit> serverHabits) {
                executorService.execute(() -> {
                    // 1. Obtener todos los hábitos locales que están sincronizados (tienen serverId)
                    List<Habit> localSyncedHabits = dbHelper.getSyncedHabits();
                    
                    // 2. Crear un conjunto de serverIds que vienen del servidor
                    java.util.Set<Long> serverIds = new java.util.HashSet<>();
                    for (Habit habit : serverHabits) {
                        serverIds.add(habit.getId());
                    }
                    
                    // 3. Eliminar hábitos locales que tienen serverId pero no están en el servidor
                    int deletedCount = 0;
                    for (Habit localHabit : localSyncedHabits) {
                        Long serverId = dbHelper.getServerId(localHabit.getId());
                        if (serverId != null && serverId > 0 && !serverIds.contains(serverId)) {
                            // Este hábito existe localmente con serverId, pero no está en el servidor
                            // Eliminarlo localmente
                            dbHelper.deleteHabit(localHabit.getId());
                            deletedCount++;
                            Log.d(TAG, "Eliminado hábito local que ya no existe en servidor: " + localHabit.getTitle() + " (serverId: " + serverId + ")");
                        }
                    }
                    
                    // 4. Upsert de los hábitos del servidor
                    int count = 0;
                    for (Habit habit : serverHabits) {
                        // Upsert: actualizar si existe, insertar si no
                        long localId = dbHelper.upsertHabitFromServer(habit, habit.getId());
                        if (localId > 0) {
                            count++;
                        }
                    }
                    
                    Log.d(TAG, "Descargados " + count + " hábitos del servidor (de " + serverHabits.size() + " totales)");
                    if (deletedCount > 0) {
                        Log.d(TAG, "Eliminados " + deletedCount + " hábitos locales que ya no existen en el servidor");
                    }
                    
                    // Obtener los hábitos actualizados de la base de datos local
                    List<Habit> updatedHabits = dbHelper.getAllHabits();
                    Log.d(TAG, "Hábitos en base de datos local después de descarga: " + updatedHabits.size());
                    
                    // Notificar que se completó la sincronización
                    isSyncing = false;
                    if (currentSyncListener != null) {
                        // Contar solo los hábitos descargados como elementos sincronizados
                        currentSyncListener.onSyncCompleted(count);
                    }
                    Log.d(TAG, "Sincronización completada: " + count + " hábitos descargados, " + deletedCount + " eliminados");
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error al descargar hábitos: " + error);
                isSyncing = false;
                if (currentSyncListener != null) {
                    currentSyncListener.onSyncError(error);
                }
            }
        });
    }
    
    private SyncListener currentSyncListener;

    public boolean isSyncing() {
        return isSyncing;
    }
}

