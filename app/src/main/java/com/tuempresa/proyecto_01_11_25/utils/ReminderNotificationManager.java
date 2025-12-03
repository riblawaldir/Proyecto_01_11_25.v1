package com.tuempresa.proyecto_01_11_25.utils;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.tuempresa.proyecto_01_11_25.R;
import com.tuempresa.proyecto_01_11_25.ui.DashboardActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

/**
 * Gestor de notificaciones de recordatorios para hábitos.
 * Programa notificaciones basadas en los horarios configurados en reminderTimes.
 */
public class ReminderNotificationManager {
    private static final String TAG = "ReminderNotificationManager";
    private static final String CHANNEL_ID = "habit_reminders";
    private static final String CHANNEL_NAME = "Recordatorios de Hábitos";
    
    private final Context context;
    private final AlarmManager alarmManager;
    private final NotificationManager notificationManager;
    
    public ReminderNotificationManager(Context context) {
        this.context = context.getApplicationContext();
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }
    
    /**
     * Crea el canal de notificaciones (requerido para Android 8.0+)
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Notificaciones de recordatorios para tus hábitos");
            channel.enableVibration(true);
            channel.enableLights(true);
            notificationManager.createNotificationChannel(channel);
        }
    }
    
    /**
     * Programa notificaciones para un hábito basándose en sus horarios de recordatorio.
     * @param habitId ID del hábito
     * @param habitTitle Título del hábito
     * @param reminderTimesJson JSON con los horarios (formato: [{"hour":8,"minute":0}, {"hour":20,"minute":0}])
     */
    public void scheduleReminders(long habitId, String habitTitle, String reminderTimesJson) {
        // Primero cancelar notificaciones existentes para este hábito
        cancelReminders(habitId);
        
        if (reminderTimesJson == null || reminderTimesJson.trim().isEmpty() || reminderTimesJson.equals("[]")) {
            android.util.Log.d(TAG, "No hay horarios configurados para el hábito " + habitId);
            return;
        }
        
        try {
            JSONArray timesArray = new JSONArray(reminderTimesJson);
            
            for (int i = 0; i < timesArray.length(); i++) {
                JSONObject timeObj = timesArray.getJSONObject(i);
                int hour = timeObj.getInt("hour");
                int minute = timeObj.getInt("minute");
                
                scheduleDailyReminder(habitId, habitTitle, hour, minute, i);
            }
            
            android.util.Log.d(TAG, "Programadas " + timesArray.length() + " notificaciones para " + habitTitle);
        } catch (JSONException e) {
            android.util.Log.e(TAG, "Error al parsear reminderTimes: " + reminderTimesJson, e);
        }
    }
    
    /**
     * Programa una notificación diaria recurrente
     */
    private void scheduleDailyReminder(long habitId, String habitTitle, int hour, int minute, int requestCode) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        
        // Si la hora ya pasó hoy, programar para mañana
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("habit_id", habitId);
        intent.putExtra("habit_title", habitTitle);
        intent.putExtra("request_code", requestCode);
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            (int) (habitId * 1000 + requestCode), // ID único para cada notificación
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // Usar setRepeating para notificaciones diarias
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                pendingIntent
            );
        } else {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            );
        }
        
        android.util.Log.d(TAG, "Notificación programada para " + habitTitle + " a las " + 
            String.format("%02d:%02d", hour, minute));
    }
    
    /**
     * Cancela todas las notificaciones de un hábito
     */
    public void cancelReminders(long habitId) {
        // Cancelar hasta 10 notificaciones por hábito (máximo razonable)
        for (int i = 0; i < 10; i++) {
            Intent intent = new Intent(context, ReminderReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) (habitId * 1000 + i),
                intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
            );
            
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
            }
        }
        
        android.util.Log.d(TAG, "Notificaciones canceladas para hábito " + habitId);
    }
    
    /**
     * Muestra una notificación inmediata (para testing)
     */
    public void showNotification(long habitId, String habitTitle) {
        Intent intent = new Intent(context, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_fitness_center_24)
            .setContentTitle("Recordatorio: " + habitTitle)
            .setContentText("Es hora de completar tu hábito")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(new long[]{0, 500, 250, 500});
        
        notificationManager.notify((int) habitId, builder.build());
    }
}

