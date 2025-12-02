package com.tuempresa.proyecto_01_11_25.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.tuempresa.proyecto_01_11_25.R;
import com.tuempresa.proyecto_01_11_25.database.HabitDatabaseHelper;
import com.tuempresa.proyecto_01_11_25.model.Habit;
import com.tuempresa.proyecto_01_11_25.repository.HabitRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class JournalingActivity extends AppCompatActivity {

    private Habit habit;
    private HabitDatabaseHelper dbHelper;
    private HabitRepository habitRepository;
    private TextInputEditText edtJournal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journaling);

        long habitId = getIntent().getLongExtra("habit_id", -1);
        if (habitId <= 0) {
            finish();
            return;
        }

        dbHelper = new HabitDatabaseHelper(this);
        habitRepository = HabitRepository.getInstance(this);
        habit = dbHelper.getHabitById(habitId);
        
        if (habit == null) {
            finish();
            return;
        }

        edtJournal = findViewById(R.id.edtJournal);
        
        // Cargar entrada del día si existe desde la base de datos
        loadTodayEntry();
        
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        
        MaterialButton btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(v -> saveJournal());
    }

    private void loadTodayEntry() {
        // Buscar entrada del día actual para este hábito
        List<HabitDatabaseHelper.DiaryEntry> entries = dbHelper.getDiaryEntriesByHabit(habit.getId());
        String todayKey = getTodayKey();
        
        for (HabitDatabaseHelper.DiaryEntry entry : entries) {
            String entryDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(new Date(entry.getDate() * 1000));
            if (entryDate.equals(todayKey)) {
                edtJournal.setText(entry.getContent());
                break;
            }
        }
    }

    private void saveJournal() {
        String text = edtJournal.getText() != null ? edtJournal.getText().toString().trim() : "";
        
        if (text.isEmpty()) {
            // Toast eliminado - usuario no quiere mensajes constantes
            return;
        }
        
        // Guardar en base de datos
        // Primero verificar si ya existe una entrada para hoy
        List<HabitDatabaseHelper.DiaryEntry> entries = dbHelper.getDiaryEntriesByHabit(habit.getId());
        String todayKey = getTodayKey();
        boolean found = false;
        
        for (HabitDatabaseHelper.DiaryEntry entry : entries) {
            String entryDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(new Date(entry.getDate() * 1000));
            if (entryDate.equals(todayKey)) {
                // Actualizar entrada existente
                dbHelper.updateDiaryEntry(entry.getId(), text);
                found = true;
                break;
            }
        }
        
        if (!found) {
            // Crear nueva entrada
            dbHelper.saveDiaryEntry(habit.getId(), text);
        }
        
        // Completar hábito
        habit.setCompleted(true);
        dbHelper.updateHabitCompleted(habit.getTitle(), true);
        
        // Actualizar hábito en API
        habitRepository.updateHabit(habit, new HabitRepository.RepositoryCallback<Habit>() {
            @Override
            public void onSuccess(Habit updatedHabit) {
                android.util.Log.d("Journaling", "Hábito actualizado en API");
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("Journaling", "Error al actualizar hábito en API: " + error);
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
                    android.util.Log.e("Journaling", "Error al guardar score: " + error);
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
}

