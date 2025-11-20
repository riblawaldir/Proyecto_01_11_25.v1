package com.tuempresa.proyecto_01_11_25.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.tuempresa.proyecto_01_11_25.R;
import com.tuempresa.proyecto_01_11_25.database.HabitDatabaseHelper;
import com.tuempresa.proyecto_01_11_25.model.Habit;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MeditationActivity extends AppCompatActivity {

    private Habit habit;
    private HabitDatabaseHelper dbHelper;
    private TextView txtTimer;
    private MaterialButton btnStart, btnPause, btnReset;
    private CountDownTimer timer;
    private long timeLeftInMillis = 0;
    private boolean isRunning = false;
    private int durationMinutes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meditation);

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

        durationMinutes = habit.getDurationMinutes() != null ? habit.getDurationMinutes() : 10;
        timeLeftInMillis = durationMinutes * 60 * 1000L;

        txtTimer = findViewById(R.id.txtTimer);
        btnStart = findViewById(R.id.btnStart);
        btnPause = findViewById(R.id.btnPause);
        btnReset = findViewById(R.id.btnReset);

        updateTimerDisplay();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        
        btnStart.setOnClickListener(v -> startTimer());
        btnPause.setOnClickListener(v -> pauseTimer());
        btnReset.setOnClickListener(v -> resetTimer());
    }

    private void startTimer() {
        if (!isRunning) {
            timer = new CountDownTimer(timeLeftInMillis, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    timeLeftInMillis = millisUntilFinished;
                    updateTimerDisplay();
                }

                @Override
                public void onFinish() {
                    completeMeditation();
                }
            }.start();
            
            isRunning = true;
            btnStart.setVisibility(View.GONE);
            btnPause.setVisibility(View.VISIBLE);
        }
    }

    private void pauseTimer() {
        if (timer != null) {
            timer.cancel();
        }
        isRunning = false;
        btnStart.setVisibility(View.VISIBLE);
        btnPause.setVisibility(View.GONE);
    }

    private void resetTimer() {
        if (timer != null) {
            timer.cancel();
        }
        timeLeftInMillis = durationMinutes * 60 * 1000L;
        isRunning = false;
        updateTimerDisplay();
        btnStart.setVisibility(View.VISIBLE);
        btnPause.setVisibility(View.GONE);
    }

    private void updateTimerDisplay() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        txtTimer.setText(timeFormatted);
    }

    private void completeMeditation() {
        // Sonido de finalización
        try {
            ToneGenerator toneGen = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
            toneGen.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
        } catch (Exception e) {
            // Ignorar si no se puede reproducir sonido
        }

        // Completar hábito
        habit.setCompleted(true);
        dbHelper.updateHabitCompleted(habit.getTitle(), true);
        
        int points = dbHelper.getHabitPoints(habit.getTitle());
        dbHelper.addScore(habit.getTitle(), points);
        
        // Notificar al Dashboard para actualizar la UI
        setResult(RESULT_OK);
        
        Toast.makeText(this, "✅ Meditación completada (+" + points + " pts)", Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }
}

