package com.tuempresa.proyecto_01_11_25.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tuempresa.proyecto_01_11_25.database.HabitDatabaseHelper;
import com.tuempresa.proyecto_01_11_25.model.Habit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.List;

public class BackupManager {

    private static final String TAG = "BackupManager";
    private final Context context;
    private final HabitDatabaseHelper dbHelper;
    private final Gson gson;

    public BackupManager(Context context) {
        this.context = context;
        this.dbHelper = new HabitDatabaseHelper(context);
        this.gson = new Gson();
    }

    public static class BackupData {
        public List<Habit> habits;
        public List<HabitDatabaseHelper.ScoreEntry> scores;
        public long timestamp;

        public BackupData(List<Habit> habits, List<HabitDatabaseHelper.ScoreEntry> scores) {
            this.habits = habits;
            this.scores = scores;
            this.timestamp = System.currentTimeMillis();
        }
    }

    public void exportData(Uri uri, OnBackupListener listener) {
        new Thread(() -> {
            try {
                List<Habit> habits = dbHelper.getAllHabits();
                List<HabitDatabaseHelper.ScoreEntry> scores = dbHelper.getAllScores();
                BackupData backupData = new BackupData(habits, scores);

                String json = gson.toJson(backupData);

                try (OutputStream outputStream = context.getContentResolver().openOutputStream(uri)) {
                    if (outputStream != null) {
                        outputStream.write(json.getBytes());
                        if (listener != null) {
                            listener.onSuccess("Copia de seguridad exportada correctamente.");
                        }
                    } else {
                        if (listener != null) {
                            listener.onError("No se pudo abrir el archivo para escribir.");
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error exportando datos", e);
                if (listener != null) {
                    listener.onError("Error al exportar: " + e.getMessage());
                }
            }
        }).start();
    }

    public void importData(Uri uri, OnBackupListener listener) {
        new Thread(() -> {
            try {
                StringBuilder stringBuilder = new StringBuilder();
                try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
                     BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                }

                String json = stringBuilder.toString();
                Type type = new TypeToken<BackupData>() {}.getType();
                BackupData backupData = gson.fromJson(json, type);

                if (backupData == null || backupData.habits == null) {
                    if (listener != null) {
                        listener.onError("Archivo de respaldo inválido o corrupto.");
                    }
                    return;
                }

                // Restaurar datos usando estrategia de "Merge Inteligente":
                // Si el hábito existe (por título), actualizamos. Si no, creamos.
                
                int habitsRestored = 0;
                for (Habit habit : backupData.habits) {
                    long existingId = dbHelper.getHabitIdByTitle(habit.getTitle());
                    if (existingId > 0) {
                        // Actualizar
                        dbHelper.updateHabitFull(existingId, habit.getTitle(), habit.getGoal(), habit.getCategory(), 
                                habit.getType().name(), habit.getPoints(), habit.getTargetValue(), habit.getTargetUnit(),
                                habit.getPagesPerDay(), habit.getReminderTimes(), habit.getDurationMinutes(),
                                habit.isDndMode(), habit.getMusicId(), habit.isJournalEnabled(),
                                habit.getGymDays(), habit.getWaterGoalGlasses(), habit.isOneClickComplete(),
                                habit.isEnglishMode(), habit.isCodingMode(), habit.getHabitIcon());
                        // También actualizar estado completado
                        dbHelper.updateHabitCompleted(habit.getTitle(), habit.isCompleted());
                    } else {
                        // Insertar
                        dbHelper.insertHabitFull(habit.getTitle(), habit.getGoal(), habit.getCategory(), 
                                habit.getType().name(), habit.getPoints(), habit.getTargetValue(), habit.getTargetUnit(),
                                habit.getPagesPerDay(), habit.getReminderTimes(), habit.getDurationMinutes(),
                                habit.isDndMode(), habit.getMusicId(), habit.isJournalEnabled(),
                                habit.getGymDays(), habit.getWaterGoalGlasses(), habit.isOneClickComplete(),
                                habit.isEnglishMode(), habit.isCodingMode(), habit.getHabitIcon());
                    }
                    habitsRestored++;
                }

                // Restaurar Scores
                // TODO: Mejorar HabitDatabaseHelper para permitir insertar scores con fecha original
                // Actualmente addScore usa fecha actual, perdiendo la fecha original del backup
                if (backupData.scores != null) {
                    for (HabitDatabaseHelper.ScoreEntry score : backupData.scores) {
                        dbHelper.addScore(score.getHabitTitle(), score.getPoints());
                    }
                }

                if (listener != null) {
                    listener.onSuccess("Restauración completada. " + habitsRestored + " hábitos procesados.");
                }

            } catch (Exception e) {
                Log.e(TAG, "Error importando datos", e);
                if (listener != null) {
                    listener.onError("Error al importar: " + e.getMessage());
                }
            }
        }).start();
    }

    public interface OnBackupListener {
        void onSuccess(String message);
        void onError(String error);
    }
}
