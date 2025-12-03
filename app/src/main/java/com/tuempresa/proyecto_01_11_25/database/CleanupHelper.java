package com.tuempresa.proyecto_01_11_25.database;

import android.content.Context;
import android.util.Log;

import com.tuempresa.proyecto_01_11_25.model.Habit;

import java.util.List;

/**
 * Helper para limpiar datos corruptos o inválidos de la base de datos.
 * Específicamente, elimina hábitos con userId: 0 (hábitos huérfanos).
 */
public class CleanupHelper {
    private static final String TAG = "CleanupHelper";
    private final HabitDatabaseHelper dbHelper;

    public CleanupHelper(Context context) {
        this.dbHelper = new HabitDatabaseHelper(context);
    }

    /**
     * Limpia todos los hábitos con userId: 0 (hábitos huérfanos).
     * También elimina scores y diary entries asociados.
     * 
     * @return Número de hábitos eliminados
     */
    public int cleanupHabitsWithUserIdZero() {
        try {
            // Obtener todos los hábitos
            List<Habit> allHabits = dbHelper.getAllHabits();
            
            int deletedCount = 0;
            for (Habit habit : allHabits) {
                // Verificar si el hábito tiene userId: 0 o userId inválido
                if (habit.getUserId() <= 0) {
                    Log.w(TAG, "Eliminando hábito huérfano: " + habit.getTitle() + " (userId: " + habit.getUserId() + ")");
                    boolean deleted = dbHelper.deleteHabit(habit.getId());
                    if (deleted) {
                        deletedCount++;
                    }
                }
            }
            
            Log.d(TAG, "Limpieza completada: " + deletedCount + " hábitos con userId <= 0 eliminados");
            return deletedCount;
        } catch (Exception e) {
            Log.e(TAG, "Error al limpiar hábitos con userId: 0", e);
            return 0;
        }
    }

    /**
     * Verifica si hay hábitos con userId: 0 en la base de datos.
     * 
     * @return true si hay hábitos con userId: 0, false en caso contrario
     */
    public boolean hasHabitsWithUserIdZero() {
        try {
            List<Habit> allHabits = dbHelper.getAllHabits();
            for (Habit habit : allHabits) {
                if (habit.getUserId() <= 0) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error al verificar hábitos con userId: 0", e);
            return false;
        }
    }

    /**
     * Cuenta cuántos hábitos tienen userId: 0.
     * 
     * @return Número de hábitos con userId <= 0
     */
    public int countHabitsWithUserIdZero() {
        try {
            List<Habit> allHabits = dbHelper.getAllHabits();
            int count = 0;
            for (Habit habit : allHabits) {
                if (habit.getUserId() <= 0) {
                    count++;
                }
            }
            return count;
        } catch (Exception e) {
            Log.e(TAG, "Error al contar hábitos con userId: 0", e);
            return 0;
        }
    }
}

