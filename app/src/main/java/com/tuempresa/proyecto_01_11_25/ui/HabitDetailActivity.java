package com.tuempresa.proyecto_01_11_25.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.tuempresa.proyecto_01_11_25.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.tuempresa.proyecto_01_11_25.database.HabitDatabaseHelper;
import com.tuempresa.proyecto_01_11_25.model.Habit;
import com.tuempresa.proyecto_01_11_25.repository.HabitRepository;
import com.tuempresa.proyecto_01_11_25.utils.SessionManager;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HabitDetailActivity extends AppCompatActivity {

    private Habit habit;
    private HabitDatabaseHelper dbHelper;
    private HabitRepository habitRepository;
    private SharedPreferences progressPrefs;
    private SessionManager sessionManager;
    private FusedLocationProviderClient fused;
    private TextView txtProgress;
    private ProgressBar progressBar;
    private MaterialButton btnAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        long habitId = getIntent().getLongExtra("habit_id", -1);
        if (habitId <= 0) {
            finish();
            return;
        }

        dbHelper = new HabitDatabaseHelper(this);
        habitRepository = HabitRepository.getInstance(this);
        habit = dbHelper.getHabitById(habitId);
        sessionManager = new SessionManager(this);
        fused = LocationServices.getFusedLocationProviderClient(this);
        
        if (habit == null) {
            finish();
            return;
        }

        progressPrefs = getSharedPreferences("habit_progress", Context.MODE_PRIVATE);
        
        loadLayoutForType();
    }

    private void loadLayoutForType() {
        switch (habit.getType()) {
            case READ_BOOK:
                setContentView(R.layout.activity_habit_detail_read);
                setupReadBook();
                break;
            case WATER:
                setContentView(R.layout.activity_habit_detail_water);
                setupWater();
                break;
            case JOURNALING:
                startActivity(new Intent(this, JournalingActivity.class)
                    .putExtra("habit_id", habit.getId()));
                finish();
                return;
            case MEDITATE:
                startActivity(new Intent(this, MeditationActivity.class)
                    .putExtra("habit_id", habit.getId()));
                finish();
                return;
            case COLD_SHOWER:
                setContentView(R.layout.activity_habit_detail_cold_shower);
                setupColdShower();
                break;
            default:
                finish();
                return;
        }
        
        // Botón de volver (si existe)
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
        
        // Agregar botón para ver historial de notas (si existe el layout)
        setupJournalHistoryButton();
    }
    
    private void setupJournalHistoryButton() {
        // Buscar botón de historial en el layout (puede no existir en todos los layouts)
        View btnHistory = findViewById(R.id.btnViewJournalHistory);
        if (btnHistory != null) {
            btnHistory.setOnClickListener(v -> {
                Intent intent = new Intent(this, HabitJournalHistoryActivity.class);
                intent.putExtra("habit_id", habit.getId());
                intent.putExtra("habit_title", habit.getTitle());
                startActivity(intent);
            });
        }
    }

    private void setupReadBook() {
        txtProgress = findViewById(R.id.txtProgress);
        progressBar = findViewById(R.id.progressBar);
        btnAction = findViewById(R.id.btnAddPages);
        
        TextView txtTitle = findViewById(R.id.txtTitle);
        txtTitle.setText(habit.getTitle());
        
        TextView txtGoal = findViewById(R.id.txtGoal);
        int pagesGoal = habit.getPagesPerDay() != null ? habit.getPagesPerDay() : 10;
        txtGoal.setText("Meta: " + pagesGoal + " páginas al día");
        
        updateReadProgress();
        
        btnAction.setOnClickListener(v -> {
            // Opción 1: Agregar páginas manualmente
            showAddPagesDialog();
        });
        
        // Botón para detectar con cámara
        MaterialButton btnDetectCamera = findViewById(R.id.btnDetectCamera);
        if (btnDetectCamera != null) {
            btnDetectCamera.setOnClickListener(v -> {
                Intent cameraIntent = new Intent(this, CameraActivity.class);
                cameraIntent.putExtra("habit_id", habit.getId());
                cameraIntent.putExtra("habit_type", "READ_BOOK");
                startActivityForResult(cameraIntent, 100);
            });
        }
    }

    private void updateReadProgress() {
        int pagesGoal = habit.getPagesPerDay() != null ? habit.getPagesPerDay() : 10;
        String todayKey = "read_" + habit.getId() + "_" + getTodayKey();
        int pagesRead = progressPrefs.getInt(todayKey, 0);
        
        int progress = pagesGoal > 0 ? (pagesRead * 100 / pagesGoal) : 0;
        if (progress > 100) progress = 100;
        
        txtProgress.setText(pagesRead + " / " + pagesGoal + " páginas");
        progressBar.setProgress(progress);
        
        // Actualizar color de la barra según progreso
        if (pagesRead >= pagesGoal && pagesGoal > 0) {
            // Solo completar si realmente alcanzó la meta
            progressBar.setProgressDrawable(
                ContextCompat.getDrawable(this, R.drawable.progress_circle_drawable_green)
            );
            if (!habit.isCompleted()) {
                completeHabit();
            }
        } else if (progress > 0) {
            // Barra naranja mientras progresa (pero no completado aún)
            progressBar.setProgressDrawable(
                ContextCompat.getDrawable(this, R.drawable.progress_circle_drawable)
            );
            // Asegurar que no esté marcado como completado si no alcanzó la meta
            if (habit.isCompleted() && pagesRead < pagesGoal) {
                habit.setCompleted(false);
                dbHelper.updateHabitCompleted(habit.getTitle(), false);
            }
        }
    }

    private void showAddPagesDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_pages, null);
        TextInputEditText edtPages = dialogView.findViewById(R.id.edtPages);
        
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Agregar páginas leídas")
            .setView(dialogView)
            .setPositiveButton("Agregar", (dialog, which) -> {
                String pagesStr = edtPages.getText() != null ? edtPages.getText().toString().trim() : "";
                if (!pagesStr.isEmpty()) {
                    try {
                        int pages = Integer.parseInt(pagesStr);
                        if (pages > 0) {
                            addPagesRead(pages);
                        }
                    } catch (NumberFormatException e) {
                        // Toast eliminado - usuario no quiere mensajes constantes
                    }
                }
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }

    private void addPagesRead(int pages) {
        String todayKey = "read_" + habit.getId() + "_" + getTodayKey();
        int currentPages = progressPrefs.getInt(todayKey, 0);
        progressPrefs.edit().putInt(todayKey, currentPages + pages).apply();
        
        updateReadProgress();
        
        // Notificar al Dashboard para actualizar la UI
        setResult(RESULT_OK);
        
        // Toast eliminado - usuario no quiere mensajes constantes
    }

    private void setupWater() {
        txtProgress = findViewById(R.id.txtProgress);
        progressBar = findViewById(R.id.progressBar);
        btnAction = findViewById(R.id.btnAddGlass);
        
        TextView txtTitle = findViewById(R.id.txtTitle);
        txtTitle.setText(habit.getTitle());
        
        TextView txtGoal = findViewById(R.id.txtGoal);
        int glassesGoal = habit.getWaterGoalGlasses() != null ? habit.getWaterGoalGlasses() : 8;
        txtGoal.setText("Meta: " + glassesGoal + " vasos al día");
        
        updateWaterProgress();
        
        btnAction.setOnClickListener(v -> addGlass());
    }

    private void updateWaterProgress() {
        int glassesGoal = habit.getWaterGoalGlasses() != null ? habit.getWaterGoalGlasses() : 8;
        String todayKey = "water_" + habit.getId() + "_" + getTodayKey();
        int glassesDrunk = progressPrefs.getInt(todayKey, 0);
        
        int progress = glassesGoal > 0 ? (glassesDrunk * 100 / glassesGoal) : 0;
        if (progress > 100) progress = 100;
        
        txtProgress.setText(glassesDrunk + " / " + glassesGoal + " vasos");
        progressBar.setProgress(progress);
        
        // Actualizar color de la barra según progreso
        if (progress >= 100 && glassesDrunk >= glassesGoal && glassesGoal > 0) {
            // Solo completar si realmente alcanzó la meta
            progressBar.setProgressDrawable(
                ContextCompat.getDrawable(this, R.drawable.progress_circle_drawable_green)
            );
            if (!habit.isCompleted()) {
                completeHabit();
            }
        } else if (progress > 0) {
            // Barra naranja mientras progresa (pero no completado aún)
            progressBar.setProgressDrawable(
                ContextCompat.getDrawable(this, R.drawable.progress_circle_drawable)
            );
            // Asegurar que no esté marcado como completado si no alcanzó la meta
            if (habit.isCompleted() && glassesDrunk < glassesGoal) {
                habit.setCompleted(false);
                dbHelper.updateHabitCompleted(habit.getTitle(), false);
            }
        }
    }

    private void addGlass() {
        String todayKey = "water_" + habit.getId() + "_" + getTodayKey();
        int currentGlasses = progressPrefs.getInt(todayKey, 0);
        progressPrefs.edit().putInt(todayKey, currentGlasses + 1).apply();
        
        updateWaterProgress();
        
        // Notificar al Dashboard para actualizar la UI
        setResult(RESULT_OK);
        
        // Toast eliminado - usuario no quiere mensajes constantes
    }

    private void setupColdShower() {
        TextView txtTitle = findViewById(R.id.txtTitle);
        txtTitle.setText(habit.getTitle());
        
        MaterialButton btnComplete = findViewById(R.id.btnComplete);
        btnComplete.setOnClickListener(v -> completeHabit());
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
        habitRepository.updateHabit(habit, new HabitRepository.RepositoryCallback<Habit>() {
            @Override
            public void onSuccess(Habit updatedHabit) {
                android.util.Log.d("HabitDetail", "Hábito actualizado en API");
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("HabitDetail", "Error al actualizar hábito en API: " + error);
            }
        });
        
        // Agregar puntos (guarda en SQLite + API)
        int points = habit.getPoints();
        habitRepository.addScore(habit.getId(), habit.getTitle(), points, new HabitRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                runOnUiThread(() -> {
                    // Notificar al Dashboard para actualizar la UI
                    setResult(RESULT_OK);
                    // Toast eliminado - usuario no quiere mensajes constantes
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    android.util.Log.e("HabitDetail", "Error al guardar score: " + error);
                    // Aún así notificar éxito local
                    setResult(RESULT_OK);
                    // Toast eliminado - usuario no quiere mensajes constantes
                    finish();
                });
            }
        });
    }

    private String getTodayKey() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }
    
    private boolean hasFineLocationPermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            // Se detectó texto con la cámara, la página ya fue agregada en CameraActivity
            // Solo actualizar el progreso visual
            updateReadProgress();
            
            // Notificar al Dashboard para actualizar la UI
            setResult(RESULT_OK);
        }
    }
}

