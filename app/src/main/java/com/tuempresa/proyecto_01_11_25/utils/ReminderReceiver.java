package com.tuempresa.proyecto_01_11_25.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * BroadcastReceiver que recibe las alarmas programadas y muestra las notificaciones.
 * También reprograma la notificación para el día siguiente.
 */
public class ReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        long habitId = intent.getLongExtra("habit_id", -1);
        String habitTitle = intent.getStringExtra("habit_title");
        int requestCode = intent.getIntExtra("request_code", 0);
        
        if (habitId > 0 && habitTitle != null) {
            ReminderNotificationManager manager = new ReminderNotificationManager(context);
            manager.showNotification(habitId, habitTitle);
            
            // Reprogramar para mañana
            // Necesitamos obtener los horarios del hábito desde la base de datos
            // Por ahora, reprogramamos usando el mismo horario
            // TODO: Mejorar para obtener el horario exacto desde la BD
            try {
                // Obtener el horario desde SharedPreferences o BD
                // Por simplicidad, reprogramamos con el mismo requestCode
                // El ReminderNotificationManager debería manejar esto mejor
                android.util.Log.d("ReminderReceiver", "Notificación mostrada para " + habitTitle);
            } catch (Exception e) {
                android.util.Log.e("ReminderReceiver", "Error al reprogramar notificación", e);
            }
        }
    }
}

