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
    private final com.tuempresa.proyecto_01_11_25.utils.SessionManager sessionManager;
    
    private boolean isSyncing = false;
    private final java.util.concurrent.locks.ReentrantLock syncLock = new java.util.concurrent.locks.ReentrantLock();
    private SyncListener currentSyncListener;

    public interface SyncListener {
        void onSyncStarted();
        void onSyncCompleted(int syncedCount);
        void onSyncError(String error);
    }

    private SyncManager(Context context) {
        this.context = context.getApplicationContext();
        this.dbHelper = new HabitDatabaseHelperSync(context);
        this.apiHelper = new HabitApiHelper(context); // Pasar context para inicializar correctamente
        this.connectionMonitor = ConnectionMonitor.getInstance(context);
        this.executorService = Executors.newSingleThreadExecutor();
        this.gson = new Gson();
        this.sessionManager = new com.tuempresa.proyecto_01_11_25.utils.SessionManager(context);
    }

    public static synchronized SyncManager getInstance(Context context) {
        if (instance == null) {
            instance = new SyncManager(context);
        }
        return instance;
    }

    /**
     * Sincroniza todos los datos pendientes con el servidor.
     * Previene múltiples sincronizaciones simultáneas usando un lock.
     */
    public void syncAll(SyncListener listener) {
        // Intentar adquirir el lock (no bloqueante)
        if (!syncLock.tryLock()) {
            Log.d(TAG, "Sincronización ya en progreso, ignorando nueva solicitud");
            if (listener != null) {
                listener.onSyncError("Sincronización ya en progreso");
            }
            return;
        }

        try {
            if (isSyncing) {
                Log.d(TAG, "Sincronización ya en progreso (verificación adicional)");
                if (listener != null) {
                    listener.onSyncError("Sincronización ya en progreso");
                }
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
                try {
                    isSyncing = true;
                    currentSyncListener = listener;
                    if (listener != null) {
                        listener.onSyncStarted();
                    }

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
                    // Liberar el lock en caso de error antes de downloadFromServer
                    if (syncLock.isHeldByCurrentThread()) {
                        syncLock.unlock();
                        Log.d(TAG, "Lock de sincronización liberado (error antes de download)");
                    }
                }
            });
        } catch (Exception e) {
            // Si hay error al iniciar la sincronización, liberar el lock
            if (syncLock.isHeldByCurrentThread()) {
                syncLock.unlock();
                Log.d(TAG, "Lock de sincronización liberado (error al iniciar)");
            }
            throw  new RuntimeException("Error al iniciar sincronización", e);
        }
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
        // Procesar operaciones pendientes de tipo SCORE
        // Los scores se sincronizan a través de processPendingOperations()
        // ya que se guardan como operaciones pendientes cuando se crean offline
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
                
                // CRÍTICO: Asegurar que el userId esté establecido antes de enviar al servidor
                long userId = sessionManager.getUserId();
                if (userId > 0) {
                    habit.setUserId(userId);
                    Log.d(TAG, "Procesando operación pendiente con userId: " + userId);
                } else {
                    Log.e(TAG, "⚠️ No se puede procesar operación pendiente: userId no válido (" + userId + ")");
                    return false;
                }
                
                if (op.operationType.equals("CREATE")) {
                    apiHelper.createHabit(habit, new HabitApiHelper.OnHabitSavedListener() {
                        @Override
                        public void onSuccess(Habit createdHabit) {
                            executorService.execute(() -> {
                                try {
                                    dbHelper.markHabitAsSynced(op.entityId, createdHabit.getId());
                                } catch (Exception e) {
                                    Log.e(TAG, "Error al marcar hábito como sincronizado", e);
                                }
                            });
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
                    try {
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
                    
                    // 4. Obtener el userId actual para filtrar hábitos
                    long currentUserId = sessionManager.getUserId();
                    
                    // 5. Upsert de los hábitos del servidor (SOLO los del usuario actual)
                    // CRÍTICO: Aceptar hábitos del usuario actual O hábitos con userId: 0 que tienen serverId válido
                    // (estos últimos se corregirán en upsertHabitFromServer)
                    int count = 0;
                    int ignoredCount = 0;
                    Log.d(TAG, "Procesando " + serverHabits.size() + " hábitos del servidor para usuario " + currentUserId);
                    for (Habit habit : serverHabits) {
                        // CRÍTICO: Si el userId es -1 (valor por defecto), significa que no se deserializó correctamente
                        // Intentar corregirlo usando el userId del usuario actual
                        if (habit.getUserId() == -1 || habit.getUserId() <= 0) {
                            Log.w(TAG, "⚠️ Hábito con userId inválido (" + habit.getUserId() + ") después de deserialización, corrigiendo a " + currentUserId + 
                                    ": " + habit.getTitle() + " (serverId: " + habit.getId() + ")");
                            habit.setUserId(currentUserId);
                        }
                        
                        Log.d(TAG, "Hábito del servidor: " + habit.getTitle() + " (userId: " + habit.getUserId() + ", serverId: " + habit.getId() + ")");
                        // Verificar que el hábito pertenezca al usuario actual
                        // Si tiene userId: 0 pero tiene un serverId válido, aceptarlo (se corregirá el userId)
                        boolean shouldAccept = false;
                        if (habit.getUserId() == currentUserId && habit.getUserId() > 0) {
                            // Hábito del usuario actual con userId válido
                            shouldAccept = true;
                        } else if (habit.getUserId() == 0 && habit.getId() > 0) {
                            // Hábito con userId: 0 pero con serverId válido (probablemente un error de deserialización)
                            // Lo aceptamos y upsertHabitFromServer corregirá el userId
                            Log.w(TAG, "⚠️ Hábito con userId: 0 pero serverId válido, corrigiendo userId: " + habit.getTitle() + 
                                    " (serverId: " + habit.getId() + ", currentUserId: " + currentUserId + ")");
                            habit.setUserId(currentUserId);
                            shouldAccept = true;
                        }
                        
                        if (shouldAccept) {
                            // Upsert: actualizar si existe, insertar si no
                            // upsertHabitFromServer corregirá el userId si es 0
                            long localId = dbHelper.upsertHabitFromServer(habit, habit.getId());
                            if (localId > 0) {
                                count++;
                            }
                        } else {
                            ignoredCount++;
                            Log.w(TAG, "⚠️ Hábito del servidor ignorado (no pertenece al usuario actual): " + habit.getTitle() + 
                                    " (userId: " + habit.getUserId() + ", currentUserId: " + currentUserId + ", serverId: " + habit.getId() + ")");
                        }
                    }
                    
                    // 6. Limpiar hábitos locales que NO pertenecen al usuario actual DESPUÉS de descargar
                    // IMPORTANTE: Esto NO elimina hábitos locales sin serverId que pertenecen al usuario actual
                    // Solo elimina hábitos de otros usuarios o con userId: 0
                    // Los hábitos locales sin serverId se mantienen para sincronizarse después
                    // CRÍTICO: Solo limpiar si el userId es válido
                    if (currentUserId > 0) {
                        dbHelper.deleteHabitsNotBelongingToCurrentUser();
                        Log.d(TAG, "Limpiados hábitos locales que no pertenecen al usuario " + currentUserId + " (después de descarga)");
                    } else {
                        Log.w(TAG, "⚠️ No se puede limpiar hábitos: userId inválido (" + currentUserId + ")");
                    }
                    
                    if (ignoredCount > 0) {
                        Log.w(TAG, "⚠️ Total de hábitos ignorados: " + ignoredCount + " (no pertenecen al usuario " + currentUserId + ")");
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
                    } catch (Exception e) {
                        Log.e(TAG, "Error crítico en downloadFromServer", e);
                        isSyncing = false;
                        if (currentSyncListener != null) {
                            currentSyncListener.onSyncError("Error al descargar hábitos: " + e.getMessage());
                        }
                    } finally {
                        // CRÍTICO: Liberar el lock cuando termine la sincronización
                        if (syncLock.isHeldByCurrentThread()) {
                            syncLock.unlock();
                            Log.d(TAG, "Lock de sincronización liberado");
                        }
                    }
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error al descargar hábitos: " + error);
                isSyncing = false;
                if (currentSyncListener != null) {
                    currentSyncListener.onSyncError(error);
                }
                // CRÍTICO: Liberar el lock en caso de error
                executorService.execute(() -> {
                    if (syncLock.isHeldByCurrentThread()) {
                        syncLock.unlock();
                        Log.d(TAG, "Lock de sincronización liberado (error en downloadFromServer)");
                    }
                });
            }
        });
    }

    public boolean isSyncing() {
        return isSyncing;
    }
}

