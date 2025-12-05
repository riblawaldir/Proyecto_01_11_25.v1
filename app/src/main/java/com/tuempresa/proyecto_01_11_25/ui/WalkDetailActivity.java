package com.tuempresa.proyecto_01_11_25.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.tuempresa.proyecto_01_11_25.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.tuempresa.proyecto_01_11_25.database.HabitDatabaseHelper;
import com.tuempresa.proyecto_01_11_25.model.Habit;
import com.tuempresa.proyecto_01_11_25.sensors.StepSensorManager;
import com.tuempresa.proyecto_01_11_25.utils.SessionManager;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WalkDetailActivity extends AppCompatActivity {

    private Habit habit;
    private HabitDatabaseHelper dbHelper;
    private SharedPreferences progressPrefs;
    private SessionManager sessionManager;
    private FusedLocationProviderClient fused;
    private TextView txtProgress;
    private TextView txtGoal;
    private TextView txtStats;
    private ProgressBar progressBar;
    private MaterialButton btnStartWalk;
    private StepSensorManager stepSensorManager;
    private android.os.Handler updateHandler;
    private Runnable updateRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walk_detail);

        long habitId = getIntent().getLongExtra("habit_id", -1);
        if (habitId <= 0) {
            finish();
            return;
        }

        dbHelper = new HabitDatabaseHelper(this);
        habit = dbHelper.getHabitById(habitId);
        sessionManager = new SessionManager(this);
        fused = LocationServices.getFusedLocationProviderClient(this);

        if (habit == null || habit.getType() != Habit.HabitType.WALK) {
            finish();
            return;
        }

        progressPrefs = getSharedPreferences("habit_progress", Context.MODE_PRIVATE);
        updateHandler = new android.os.Handler(android.os.Looper.getMainLooper());

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        txtTitle = findViewById(R.id.txtTitle);
        txtProgress = findViewById(R.id.txtProgress);
        txtGoal = findViewById(R.id.txtGoal);
        txtStats = findViewById(R.id.txtStats);
        progressBar = findViewById(R.id.progressBar);
        btnStartWalk = findViewById(R.id.btnStartWalk);

        txtTitle.setText(habit.getTitle());

        // Inicializar sensor de caminar
        stepSensorManager = new StepSensorManager(this, () -> {
            // Callback cuando se completa la caminata
            runOnUiThread(() -> {
                updateWalkProgress();
                btnStartWalk.setText("Caminata Completada ✓");
                btnStartWalk.setEnabled(false);
            });
        });

        btnStartWalk.setOnClickListener(v -> {
            if (stepSensorManager != null) {
                stepSensorManager.start();
                btnStartWalk.setText("Caminando...");
                btnStartWalk.setEnabled(false);
                txtStats.setText("🚶 Caminata en progreso. El sensor está registrando tu actividad...");
            }
        });

        updateWalkProgress();
        
        // Actualizar progreso cada 2 segundos mientras está activo
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                updateWalkProgress();
                if (stepSensorManager != null) {
                    updateHandler.postDelayed(this, 2000);
                }
            }
        };
        updateHandler.post(updateRunnable);
    }

    private TextView txtTitle;

    private void updateWalkProgress() {
        String todayKey = getTodayKey();
        int progress = 0;
        int goal = 0;
        String unit = "m";
        String progressKey = "";

        if (habit.getWalkGoalSteps() != null && habit.getWalkGoalSteps() > 0) {
            goal = habit.getWalkGoalSteps();
            unit = "pasos";
            progressKey = "walk_steps_" + habit.getId() + "_" + todayKey;
        } else if (habit.getWalkGoalMeters() != null && habit.getWalkGoalMeters() > 0) {
            goal = habit.getWalkGoalMeters();
            unit = "m";
            progressKey = "walk_meters_" + habit.getId() + "_" + todayKey;
        } else {
            // Valor por defecto
            goal = 500;
            unit = "m";
            progressKey = "walk_meters_" + habit.getId() + "_" + todayKey;
        }

        progress = progressPrefs.getInt(progressKey, 0);

        int progressPercent = goal > 0 ? (progress * 100 / goal) : 0;
        if (progressPercent > 100) progressPercent = 100;

        txtProgress.setText(progress + " / " + goal);
        txtGoal.setText(unit + " hoy");
        progressBar.setProgress(progressPercent);

        // Actualizar color de la barra según progreso
        if (progressPercent >= 100 && progress >= goal && goal > 0) {
            progressBar.setProgressDrawable(
                ContextCompat.getDrawable(this, R.drawable.progress_circle_drawable_green)
            );
            if (!habit.isCompleted()) {
                completeHabit();
            }
            txtStats.setText("✅ ¡Meta alcanzada! Has caminado " + progress + " " + unit + " hoy");
        } else if (progress > 0) {
            progressBar.setProgressDrawable(
                ContextCompat.getDrawable(this, R.drawable.progress_circle_drawable)
            );
            txtStats.setText("Progreso: " + progressPercent + "% completado");
        } else {
            txtStats.setText("Inicia una caminata para comenzar a registrar tu progreso");
        }
    }

    private void completeHabit() {
        habit.setCompleted(true);
        dbHelper.updateHabitCompleted(habit.getTitle(), true);
        
        // Guardar completado con ubicación GPS
        long currentUserId = sessionManager.getUserId();
        if (currentUserId > 0) {
            if (hasFineLocationPermission()) {
                fused.getLastLocation().addOnSuccessListener(location -> {
                    if (location != null) {
                        dbHelper.saveHabitCompletion(
                            habit.getId(),
                            currentUserId,
                            location.getLatitude(),
                            location.getLongitude()
                        );
                    } else {
                        dbHelper.saveHabitCompletion(habit.getId(), currentUserId, 0.0, 0.0);
                    }
                });
            } else {
                dbHelper.saveHabitCompletion(habit.getId(), currentUserId, 0.0, 0.0);
            }
        }
        
        // Actualizar hábito en API
        com.tuempresa.proyecto_01_11_25.repository.HabitRepository habitRepository = 
            com.tuempresa.proyecto_01_11_25.repository.HabitRepository.getInstance(this);
        habitRepository.updateHabit(habit, new com.tuempresa.proyecto_01_11_25.repository.HabitRepository.RepositoryCallback<Habit>() {
            @Override
            public void onSuccess(Habit updatedHabit) {
                android.util.Log.d("WalkDetail", "Hábito completado y actualizado en API");
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("WalkDetail", "Error al actualizar hábito: " + error);
            }
        });
    }

    private String getTodayKey() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (updateHandler != null && updateRunnable != null) {
            updateHandler.removeCallbacks(updateRunnable);
        }
        if (stepSensorManager != null) {
            stepSensorManager.stop();
        }
    }
    
    private boolean hasFineLocationPermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
}

