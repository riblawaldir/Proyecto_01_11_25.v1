package com.tuempresa.proyecto_01_11_25.api;

import android.util.Log;

import com.tuempresa.proyecto_01_11_25.model.Habit;

import java.util.List;

/**
 * EJEMPLO DE USO DE LA API DE HÁBITOS
 * 
 * Este archivo muestra cómo usar HabitApiHelper para interactuar con la API.
 * Puedes eliminar este archivo una vez que entiendas cómo funciona.
 */
public class EjemploUsoApi {
    private static final String TAG = "EjemploUsoApi";
    private HabitApiHelper apiHelper;

    public EjemploUsoApi() {
        // Inicializar el helper
        apiHelper = new HabitApiHelper();
        
        // Si necesitas cambiar la URL base (por ejemplo, para testing):
        // HabitApiClient.getInstance().setBaseUrl("https://api-test.tuempresa.com/api/v1/");
    }

    /**
     * Ejemplo: Obtener todos los hábitos
     */
    public void ejemploObtenerTodosLosHabitos() {
        apiHelper.getAllHabits(new HabitApiHelper.OnHabitsLoadedListener() {
            @Override
            public void onSuccess(List<Habit> habits) {
                Log.d(TAG, "Hábitos obtenidos: " + habits.size());
                for (Habit habit : habits) {
                    Log.d(TAG, "Hábito: " + habit.getTitle() + " - Tipo: " + habit.getType());
                }
                // Aquí puedes actualizar tu UI con la lista de hábitos
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error al obtener hábitos: " + error);
                // Manejar el error (mostrar mensaje al usuario, etc.)
            }
        });
    }

    /**
     * Ejemplo: Obtener un hábito por ID
     */
    public void ejemploObtenerHabitoPorId(long habitId) {
        apiHelper.getHabitById(habitId, new HabitApiHelper.OnHabitLoadedListener() {
            @Override
            public void onSuccess(Habit habit) {
                Log.d(TAG, "Hábito obtenido: " + habit.getTitle());
                // Usar el hábito obtenido
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error al obtener hábito: " + error);
            }
        });
    }

    /**
     * Ejemplo: Crear un nuevo hábito
     */
    public void ejemploCrearHabito() {
        Habit nuevoHabit = new Habit();
        nuevoHabit.setTitle("Leer 30 minutos");
        nuevoHabit.setGoal("Leer al menos 30 minutos al día");
        nuevoHabit.setCategory("educación");
        nuevoHabit.setType(Habit.HabitType.READ_BOOK);
        nuevoHabit.setPagesPerDay(10);
        nuevoHabit.setTargetValue(30.0);
        nuevoHabit.setTargetUnit("minutos");

        apiHelper.createHabit(nuevoHabit, new HabitApiHelper.OnHabitSavedListener() {
            @Override
            public void onSuccess(Habit habit) {
                Log.d(TAG, "Hábito creado con ID: " + habit.getId());
                // El hábito ahora tiene el ID asignado por el servidor
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error al crear hábito: " + error);
            }
        });
    }

    /**
     * Ejemplo: Actualizar un hábito existente
     */
    public void ejemploActualizarHabito(long habitId) {
        Habit habitActualizado = new Habit();
        habitActualizado.setTitle("Leer 45 minutos"); // Título actualizado
        habitActualizado.setGoal("Leer al menos 45 minutos al día");
        habitActualizado.setCategory("educación");
        habitActualizado.setType(Habit.HabitType.READ_BOOK);
        habitActualizado.setPagesPerDay(15);
        habitActualizado.setTargetValue(45.0);
        habitActualizado.setTargetUnit("minutos");

        apiHelper.updateHabit(habitId, habitActualizado, new HabitApiHelper.OnHabitSavedListener() {
            @Override
            public void onSuccess(Habit habit) {
                Log.d(TAG, "Hábito actualizado: " + habit.getTitle());
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error al actualizar hábito: " + error);
            }
        });
    }

    /**
     * Ejemplo: Eliminar un hábito
     */
    public void ejemploEliminarHabito(long habitId) {
        apiHelper.deleteHabit(habitId, new HabitApiHelper.OnHabitDeletedListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Hábito eliminado correctamente");
                // Actualizar UI para reflejar la eliminación
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error al eliminar hábito: " + error);
            }
        });
    }

    /**
     * Ejemplo: Sincronizar múltiples hábitos
     */
    public void ejemploSincronizarHabitos(List<Habit> habits) {
        apiHelper.syncHabits(habits, new HabitApiHelper.OnHabitsLoadedListener() {
            @Override
            public void onSuccess(List<Habit> habits) {
                Log.d(TAG, "Hábitos sincronizados: " + habits.size());
                // Los hábitos ahora están sincronizados con el servidor
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error al sincronizar hábitos: " + error);
            }
        });
    }

    /**
     * Ejemplo: Uso directo de Retrofit (sin helper)
     * Útil si necesitas más control sobre la llamada
     */
    public void ejemploUsoDirectoRetrofit() {
        HabitApiService apiService = HabitApiClient.getInstance().getApiService();
        
        retrofit2.Call<Habit> call = apiService.getHabitById(1);
        call.enqueue(new retrofit2.Callback<Habit>() {
            @Override
            public void onResponse(retrofit2.Call<Habit> call, retrofit2.Response<Habit> response) {
                if (response.isSuccessful()) {
                    Habit habit = response.body();
                    Log.d(TAG, "Hábito: " + habit.getTitle());
                }
            }

            @Override
            public void onFailure(retrofit2.Call<Habit> call, Throwable t) {
                Log.e(TAG, "Error: " + t.getMessage());
            }
        });
    }
}

