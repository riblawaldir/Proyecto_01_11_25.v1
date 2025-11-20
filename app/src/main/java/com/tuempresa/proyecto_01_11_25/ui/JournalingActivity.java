package com.tuempresa.proyecto_01_11_25.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.tuempresa.proyecto_01_11_25.R;
import com.tuempresa.proyecto_01_11_25.database.HabitDatabaseHelper;
import com.tuempresa.proyecto_01_11_25.model.Habit;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class JournalingActivity extends AppCompatActivity {

    private Habit habit;
    private HabitDatabaseHelper dbHelper;
    private SharedPreferences journalPrefs;
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
        habit = dbHelper.getHabitById(habitId);
        
        if (habit == null) {
            finish();
            return;
        }

        journalPrefs = getSharedPreferences("journal_entries", Context.MODE_PRIVATE);
        
        edtJournal = findViewById(R.id.edtJournal);
        
        // Cargar entrada del día si existe
        String todayKey = "journal_" + habit.getId() + "_" + getTodayKey();
        String savedText = journalPrefs.getString(todayKey, "");
        if (!savedText.isEmpty()) {
            edtJournal.setText(savedText);
        }
        
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        
        MaterialButton btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(v -> saveJournal());
    }

    private void saveJournal() {
        String text = edtJournal.getText() != null ? edtJournal.getText().toString().trim() : "";
        
        if (text.isEmpty()) {
            Toast.makeText(this, "Escribe algo sobre tu día", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String todayKey = "journal_" + habit.getId() + "_" + getTodayKey();
        journalPrefs.edit().putString(todayKey, text).apply();
        
        // Completar hábito
        habit.setCompleted(true);
        dbHelper.updateHabitCompleted(habit.getTitle(), true);
        
        int points = dbHelper.getHabitPoints(habit.getTitle());
        dbHelper.addScore(habit.getTitle(), points);
        
        // Notificar al Dashboard para actualizar la UI
        setResult(RESULT_OK);
        
        Toast.makeText(this, "✅ Entrada guardada (+" + points + " pts)", Toast.LENGTH_SHORT).show();
        finish();
    }

    private String getTodayKey() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }
}

