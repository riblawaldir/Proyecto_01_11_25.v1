package com.tuempresa.proyecto_01_11_25.sensors;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.tuempresa.proyecto_01_11_25.database.HabitDatabaseHelper;
import com.tuempresa.proyecto_01_11_25.model.Habit;
import com.tuempresa.proyecto_01_11_25.model.HabitEvent;
import com.tuempresa.proyecto_01_11_25.model.HabitEventStore;
import com.tuempresa.proyecto_01_11_25.repository.HabitRepository;

public class StepSensorManager {

    private static final float TARGET_METERS = 150f;

    private final Context ctx;
    private final FusedLocationProviderClient fused;
    private Location last;
    private float accMeters = 0f;
    private boolean done = false;

    private LocationCallback callback;
    private Runnable onWalkCompletedCallback;

    public StepSensorManager(Context ctx) {
        this(ctx, null);
    }

    public StepSensorManager(Context ctx, Runnable onWalkCompletedCallback) {
        this.ctx = ctx;
        this.onWalkCompletedCallback = onWalkCompletedCallback;
        this.fused = LocationServices.getFusedLocationProviderClient(ctx);
    }

    @SuppressLint("MissingPermission")
    public void start() {
        done = false;
        accMeters = 0f;
        if (!hasPermission()) {
            // Toast eliminado - usuario no quiere mensajes constantes
            return;
        }

        LocationRequest req = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000)
                .setMinUpdateIntervalMillis(1500)
                .setMinUpdateDistanceMeters(2)
                .build();

        callback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {
                for (Location loc : result.getLocations()) {
                    if (last != null) {
                        accMeters += last.distanceTo(loc);
                    }
                    last = loc;

                    if (!done && accMeters >= TARGET_METERS) {
                        done = true;
                        HabitEventStore.add(new HabitEvent(
                                loc.getLatitude(),
                                loc.getLongitude(),
                                "Caminar completado (" + (int)accMeters + " m) 🚶",
                                HabitEvent.HabitType.WALK
                        ));
                        
                        // Agregar puntos a la base de datos y API
                        try {
                            HabitDatabaseHelper dbHelper = new HabitDatabaseHelper(ctx);
                            HabitRepository habitRepository = HabitRepository.getInstance(ctx);
                            
                            // Buscar hábito de tipo WALK
                            java.util.List<Habit> habits = dbHelper.getAllHabits();
                            for (Habit habit : habits) {
                                if (habit.getType() == Habit.HabitType.WALK && !habit.isCompleted()) {
                                    habit.setCompleted(true);
                                    dbHelper.updateHabitCompleted(habit.getTitle(), true);
                                    
                                    // Actualizar hábito en API
                                    habitRepository.updateHabit(habit, new HabitRepository.RepositoryCallback<Habit>() {
                                        @Override
                                        public void onSuccess(Habit updatedHabit) {
                                            android.util.Log.d("StepSensor", "Hábito WALK actualizado en API");
                                        }

                                        @Override
                                        public void onError(String error) {
                                            android.util.Log.e("StepSensor", "Error al actualizar hábito WALK en API: " + error);
                                        }
                                    });
                                    
                                    // Agregar puntos (guarda en SQLite + API)
                                    int points = habit.getPoints();
                                    habitRepository.addScore(habit.getId(), habit.getTitle(), points, new HabitRepository.RepositoryCallback<Void>() {
                                        @Override
                                        public void onSuccess(Void data) {
                                            android.util.Log.d("StepSensor", "Score WALK guardado: " + points + " puntos");
                                        }

                                        @Override
                                        public void onError(String error) {
                                            android.util.Log.e("StepSensor", "Error al guardar score WALK: " + error);
                                        }
                                    });
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            android.util.Log.e("StepSensor", "Error al agregar puntos", e);
                        }
                        
                        // Toast eliminado - usuario no quiere mensajes constantes
                        
                        // Notificar callback si existe
                        if (onWalkCompletedCallback != null) {
                            onWalkCompletedCallback.run();
                        }
                        
                        stop();
                        break;
                    }
                }
            }
        };

        fused.requestLocationUpdates(req, callback, ctx.getMainLooper());
    }

    public void stop() {
        if (callback != null) fused.removeLocationUpdates(callback);
        callback = null;
    }

    private boolean hasPermission() {
        return ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }
}
