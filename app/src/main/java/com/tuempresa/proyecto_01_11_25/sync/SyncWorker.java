package com.tuempresa.proyecto_01_11_25.sync;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

/**
 * Worker de WorkManager para sincronización automática en segundo plano.
 */
public class SyncWorker extends Worker {
    private static final String TAG = "SyncWorker";

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Iniciando sincronización automática");
        
        try {
            SyncManager syncManager = SyncManager.getInstance(getApplicationContext());
            
            // Sincronizar de forma síncrona
            syncManager.syncAll(new SyncManager.SyncListener() {
                @Override
                public void onSyncStarted() {
                    Log.d(TAG, "Sincronización iniciada");
                }

                @Override
                public void onSyncCompleted(int syncedCount) {
                    Log.d(TAG, "Sincronización completada: " + syncedCount + " elementos");
                }

                @Override
                public void onSyncError(String error) {
                    Log.e(TAG, "Error en sincronización: " + error);
                }
            });
            
            // Esperar un poco para que la sincronización asíncrona termine
            Thread.sleep(5000);
            
            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Error en SyncWorker", e);
            return Result.retry(); // Reintentar si falla
        }
    }
}

