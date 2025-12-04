package com.tuempresa.proyecto_01_11_25.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * BroadcastReceiver que recibe las alarmas programadas y muestra las notificaciones.
 * También reprograma la notificación para el día siguiente.
 */
public class ReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        long habitId = intent.getLongExtra("habit_id", -1);
        String habitTitle = intent.getStringExtra("habit_title");
        
        if (habitId > 0 && habitTitle != null) {
            ReminderNotificationManager manager = new ReminderNotificationManager(context);
            manager.showNotification(habitId, habitTitle);
            
            // TODO: Mejorar para obtener el horario exacto desde la BD y reprogramar
            android.util.Log.d("ReminderReceiver", "Notificación mostrada para " + habitTitle);
        }
    }
}