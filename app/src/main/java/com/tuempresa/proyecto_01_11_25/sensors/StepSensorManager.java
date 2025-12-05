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
import com.tuempresa.proyecto_01_11_25.repository.HabitRepository;
import com.tuempresa.proyecto_01_11_25.utils.SessionManager;

public class StepSensorManager {

    private static final float DEFAULT_TARGET_METERS = 500f; // Valor por defecto mejorado
    private static final int DEFAULT_TARGET_STEPS = 5000; // Valor por defecto en pasos

    private final Context ctx;
    private final FusedLocationProviderClient fused;
    private Location last;
    private float accMeters = 0f;
    private int accSteps = 0; // Contador de pasos acumulados
    private boolean done = false;
    private boolean useSteps = false; // Si true, usar pasos; si false, usar metros
    private float targetMeters = DEFAULT_TARGET_METERS;
    private int targetSteps = DEFAULT_TARGET_STEPS;

    private LocationCallback callback;
    private Runnable onWalkCompletedCallback;
    private android.hardware.SensorManager sensorManager;
    private android.hardware.Sensor stepSensor;
    private android.hardware.SensorEventListener stepListener;

    public StepSensorManager(Context ctx) {
        this(ctx, null);
    }

    public StepSensorManager(Context ctx, Runnable onWalkCompletedCallback) {
        this.ctx = ctx;
        this.onWalkCompletedCallback = onWalkCompletedCallback;
        this.fused = LocationServices.getFusedLocationProviderClient(ctx);
        this.sensorManager = (android.hardware.SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);
        this.stepSensor = sensorManager != null ? sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_STEP_COUNTER) : null;
        
        // Intentar obtener la meta del hábito WALK si existe
        loadWalkGoalFromHabit();
    }
    
    /**
     * Carga la meta del hábito WALK desde la base de datos
     */
    private void loadWalkGoalFromHabit() {
        try {
            HabitDatabaseHelper dbHelper = new HabitDatabaseHelper(ctx);
            java.util.List<Habit> habits = dbHelper.getAllHabits();
            for (Habit habit : habits) {
                if (habit.getType() == Habit.HabitType.WALK) {
                    if (habit.getWalkGoalSteps() != null && habit.getWalkGoalSteps() > 0) {
                        useSteps = true;
                        targetSteps = habit.getWalkGoalSteps();
                        android.util.Log.d("StepSensor", "Meta de caminar configurada: " + targetSteps + " pasos");
                    } else if (habit.getWalkGoalMeters() != null && habit.getWalkGoalMeters() > 0) {
                        useSteps = false;
                        targetMeters = habit.getWalkGoalMeters();
                        android.util.Log.d("StepSensor", "Meta de caminar configurada: " + targetMeters + " metros");
                    }
                    break;
                }
            }
        } catch (Exception e) {
            android.util.Log.e("StepSensor", "Error al cargar meta de caminar, usando valores por defecto", e);
        }
    }

    @SuppressLint("MissingPermission")
    public void start() {
        done = false;
        accMeters = 0f;
        accSteps = 0;
        
        // Recargar meta por si cambió
        loadWalkGoalFromHabit();
        
        if (useSteps && stepSensor != null) {
            // Usar sensor de pasos si está disponible
            startStepCounting();
        } else if (!hasPermission()) {
            // Toast eliminado - usuario no quiere mensajes constantes
            return;
        } else {
            // Usar GPS para medir distancia
            startLocationTracking();
        }
    }
    
    /**
     * Inicia el conteo de pasos usando el sensor del sistema
     */
    private void startStepCounting() {
        if (stepSensor == null) {
            android.util.Log.w("StepSensor", "Sensor de pasos no disponible, usando GPS");
            startLocationTracking();
            return;
        }
        
        // Obtener pasos iniciales
        int initialSteps = 0;
        try {
            android.hardware.SensorEvent event = null;
            // No podemos obtener el valor inicial directamente, así que usamos el contador incremental
            stepListener = new android.hardware.SensorEventListener() {
                private int lastStepCount = -1;
                
                @Override
                public void onSensorChanged(android.hardware.SensorEvent event) {
                    if (lastStepCount < 0) {
                        lastStepCount = (int) event.values[0];
                        return;
                    }
                    
                    int currentSteps = (int) event.values[0];
                    int stepsSinceLast = currentSteps - lastStepCount;
                    
                    if (stepsSinceLast > 0) {
                        accSteps += stepsSinceLast;
                        lastStepCount = currentSteps;
                        
                        android.util.Log.d("StepSensor", "Pasos acumulados: " + accSteps + " / " + targetSteps);
                        
                        // Guardar progreso en SharedPreferences para mostrar en Dashboard
                        saveWalkProgress(0, accSteps);
                        
                        if (!done && accSteps >= targetSteps) {
                            done = true;
                            completeWalk();
                        }
                    }
                }
                
                @Override
                public void onAccuracyChanged(android.hardware.Sensor sensor, int accuracy) {
                    // No se requiere manejo especial
                }
            };
            
            sensorManager.registerListener(stepListener, stepSensor, android.hardware.SensorManager.SENSOR_DELAY_NORMAL);
            android.util.Log.d("StepSensor", "Sensor de pasos iniciado. Meta: " + targetSteps + " pasos");
        } catch (Exception e) {
            android.util.Log.e("StepSensor", "Error al iniciar sensor de pasos", e);
            startLocationTracking(); // Fallback a GPS
        }
    }
    
    /**
     * Inicia el tracking de ubicación usando GPS
     */
    private void startLocationTracking() {
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

                    android.util.Log.d("StepSensor", "Distancia acumulada: " + (int)accMeters + " / " + (int)targetMeters + " m");
                    
                    // Guardar progreso en SharedPreferences para mostrar en Dashboard
                    saveWalkProgress((int)accMeters, 0);

                    if (!done && accMeters >= targetMeters) {
                        done = true;
                        completeWalk();
                        break;
                    }
                }
            }
        };

        fused.requestLocationUpdates(req, callback, ctx.getMainLooper());
    }
    
    /**
     * Guarda el progreso de caminar en SharedPreferences
     */
    private void saveWalkProgress(int meters, int steps) {
        try {
            HabitDatabaseHelper dbHelper = new HabitDatabaseHelper(ctx);
            java.util.List<Habit> habits = dbHelper.getAllHabits();
            for (Habit habit : habits) {
                if (habit.getType() == Habit.HabitType.WALK) {
                    android.content.SharedPreferences prefs = ctx.getSharedPreferences("habit_progress", Context.MODE_PRIVATE);
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                    String todayKey = sdf.format(new java.util.Date());
                    
                    if (steps > 0) {
                        String key = "walk_steps_" + habit.getId() + "_" + todayKey;
                        prefs.edit().putInt(key, steps).apply();
                    } else if (meters > 0) {
                        String key = "walk_meters_" + habit.getId() + "_" + todayKey;
                        prefs.edit().putInt(key, meters).apply();
                    }
                    break;
                }
            }
        } catch (Exception e) {
            android.util.Log.e("StepSensor", "Error al guardar progreso", e);
        }
    }
    
    /**
     * Completa el hábito de caminar y notifica
     */
    private void completeWalk() {
        try {
            HabitDatabaseHelper dbHelper = new HabitDatabaseHelper(ctx);
            HabitRepository habitRepository = HabitRepository.getInstance(ctx);
            
            // Guardar progreso final antes de completar
            if (useSteps) {
                saveWalkProgress(0, accSteps);
            } else {
                saveWalkProgress((int)accMeters, 0);
            }
            
            // Buscar hábito de tipo WALK
            java.util.List<Habit> habits = dbHelper.getAllHabits();
            for (Habit habit : habits) {
                if (habit.getType() == Habit.HabitType.WALK && !habit.isCompleted()) {
                    habit.setCompleted(true);
                    dbHelper.updateHabitCompleted(habit.getTitle(), true);
                    
                    // Guardar completado con ubicación GPS
                    SessionManager sessionManager = new SessionManager(ctx);
                    long currentUserId = sessionManager.getUserId();
                    if (currentUserId > 0) {
                        if (last != null && !useSteps) {
                            // Guardar con ubicación GPS
                            dbHelper.saveHabitCompletion(
                                habit.getId(),
                                currentUserId,
                                last.getLatitude(),
                                last.getLongitude()
                            );
                        } else {
                            // Guardar sin ubicación (pasos o sin GPS)
                            dbHelper.saveHabitCompletion(
                                habit.getId(),
                                currentUserId,
                                0.0,
                                0.0
                            );
                        }
                    }
                    
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
                            String message = useSteps ? 
                                "Caminar completado (" + accSteps + " pasos) 🚶" :
                                "Caminar completado (" + (int)accMeters + " m) 🚶";
                            android.util.Log.d("StepSensor", "Score WALK guardado: " + points + " puntos - " + message);
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
        
        // Notificar callback si existe
        if (onWalkCompletedCallback != null) {
            onWalkCompletedCallback.run();
        }
        
        stop();
    }

    public void stop() {
        if (callback != null) {
            fused.removeLocationUpdates(callback);
            callback = null;
        }
        if (stepListener != null && sensorManager != null) {
            sensorManager.unregisterListener(stepListener);
            stepListener = null;
        }
    }

    private boolean hasPermission() {
        return ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }
}
