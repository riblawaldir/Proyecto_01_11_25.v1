package com.tuempresa.proyecto_01_11_25.api;

import android.content.Context;
import android.util.Log;

import com.tuempresa.proyecto_01_11_25.model.Habit;
import com.tuempresa.proyecto_01_11_25.model.HabitsResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Clase helper para facilitar el uso de la API de hábitos.
 * Proporciona métodos de alto nivel con callbacks para manejar las respuestas.
 */
public class HabitApiHelper {
    private static final String TAG = "HabitApiHelper";
    private HabitApiService apiService;

    public HabitApiHelper() {
        // Nota: HabitApiClient.getInstance() puede fallar si no se inicializó con contexto
        // En ese caso, se debe usar HabitApiClient.getInstance(context)
        try {
            this.apiService = HabitApiClient.getInstance().getApiService();
        } catch (IllegalStateException e) {
            Log.e(TAG, "HabitApiClient no inicializado. Usar constructor con Context.", e);
        }
    }

    public HabitApiHelper(Context context) {
        this.apiService = HabitApiClient.getInstance(context).getApiService();
    }

    /**
     * Interfaz para callback cuando se obtienen todos los hábitos.
     */
    public interface OnHabitsLoadedListener {
        void onSuccess(List<Habit> habits);
        void onError(String error);
    }

    /**
     * Interfaz para callback cuando se obtiene un hábito.
     */
    public interface OnHabitLoadedListener {
        void onSuccess(Habit habit);
        void onError(String error);
    }

    /**
     * Interfaz para callback cuando se crea/actualiza un hábito.
     */
    public interface OnHabitSavedListener {
        void onSuccess(Habit habit);
        void onError(String error);
    }

    /**
     * Interfaz para callback cuando se elimina un hábito.
     */
    public interface OnHabitDeletedListener {
        void onSuccess();
        void onError(String error);
    }

    /**
     * Obtiene todos los hábitos del servidor.
     * @param listener Callback para manejar la respuesta
     */
    public void getAllHabits(OnHabitsLoadedListener listener) {
        Call<HabitsResponse> call = apiService.getAllHabits();
        call.enqueue(new Callback<HabitsResponse>() {
            @Override
            public void onResponse(Call<HabitsResponse> call, Response<HabitsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    HabitsResponse habitsResponse = response.body();
                    if (habitsResponse.isSuccess()) {
                        listener.onSuccess(habitsResponse.getHabits());
                    } else {
                        listener.onError(habitsResponse.getMessage() != null ? 
                                habitsResponse.getMessage() : "Error desconocido");
                    }
                } else {
                    String error = "Error en la respuesta: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            error = response.errorBody().string();
                        } catch (Exception e) {
                            Log.e(TAG, "Error al leer errorBody", e);
                        }
                    }
                    listener.onError(error);
                }
            }

            @Override
            public void onFailure(Call<HabitsResponse> call, Throwable t) {
                Log.e(TAG, "Error al obtener hábitos", t);
                listener.onError("Error de conexión: " + t.getMessage());
            }
        });
    }

    /**
     * Obtiene un hábito específico por su ID.
     * @param id ID del hábito
     * @param listener Callback para manejar la respuesta
     */
    public void getHabitById(long id, OnHabitLoadedListener listener) {
        Call<Habit> call = apiService.getHabitById(id);
        call.enqueue(new Callback<Habit>() {
            @Override
            public void onResponse(Call<Habit> call, Response<Habit> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listener.onSuccess(response.body());
                } else {
                    String error = "Error al obtener hábito: " + response.code();
                    listener.onError(error);
                }
            }

            @Override
            public void onFailure(Call<Habit> call, Throwable t) {
                Log.e(TAG, "Error al obtener hábito", t);
                listener.onError("Error de conexión: " + t.getMessage());
            }
        });
    }

    /**
     * Crea un nuevo hábito en el servidor.
     * @param habit El hábito a crear
     * @param listener Callback para manejar la respuesta
     */
    public void createHabit(Habit habit, OnHabitSavedListener listener) {
        Call<Habit> call = apiService.createHabit(habit);
        call.enqueue(new Callback<Habit>() {
            @Override
            public void onResponse(Call<Habit> call, Response<Habit> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listener.onSuccess(response.body());
                } else {
                    String error = "Error al crear hábito: " + response.code();
                    listener.onError(error);
                }
            }

            @Override
            public void onFailure(Call<Habit> call, Throwable t) {
                Log.e(TAG, "Error al crear hábito", t);
                listener.onError("Error de conexión: " + t.getMessage());
            }
        });
    }

    /**
     * Actualiza un hábito existente.
     * @param id ID del hábito a actualizar
     * @param habit El hábito con los datos actualizados
     * @param listener Callback para manejar la respuesta
     */
    public void updateHabit(long id, Habit habit, OnHabitSavedListener listener) {
        Call<Habit> call = apiService.updateHabit(id, habit);
        call.enqueue(new Callback<Habit>() {
            @Override
            public void onResponse(Call<Habit> call, Response<Habit> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listener.onSuccess(response.body());
                } else {
                    String error = "Error al actualizar hábito: " + response.code();
                    listener.onError(error);
                }
            }

            @Override
            public void onFailure(Call<Habit> call, Throwable t) {
                Log.e(TAG, "Error al actualizar hábito", t);
                listener.onError("Error de conexión: " + t.getMessage());
            }
        });
    }

    /**
     * Elimina un hábito del servidor.
     * @param id ID del hábito a eliminar
     * @param listener Callback para manejar la respuesta
     */
    public void deleteHabit(long id, OnHabitDeletedListener listener) {
        Log.d(TAG, "🔄 Intentando eliminar hábito del servidor (id: " + id + ")");
        Call<HabitsResponse> call = apiService.deleteHabit(id);
        call.enqueue(new Callback<HabitsResponse>() {
            @Override
            public void onResponse(Call<HabitsResponse> call, Response<HabitsResponse> response) {
                if (response.isSuccessful()) {
                    HabitsResponse habitsResponse = response.body();
                    if (habitsResponse != null && habitsResponse.isSuccess()) {
                        Log.d(TAG, "✅ Hábito eliminado exitosamente del servidor (id: " + id + ")");
                        listener.onSuccess();
                    } else {
                        String errorMsg = "Error en respuesta del servidor";
                        if (habitsResponse != null && habitsResponse.getMessage() != null) {
                            errorMsg = habitsResponse.getMessage();
                        }
                        Log.e(TAG, "❌ Error al eliminar hábito: " + errorMsg);
                        listener.onError(errorMsg);
                    }
                } else {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error al leer errorBody", e);
                    }
                    String error = "Error al eliminar hábito: " + response.code() + " - " + errorBody;
                    Log.e(TAG, "❌ " + error);
                    listener.onError(error);
                }
            }

            @Override
            public void onFailure(Call<HabitsResponse> call, Throwable t) {
                Log.e(TAG, "❌ Error de conexión al eliminar hábito (id: " + id + ")", t);
                listener.onError("Error de conexión: " + t.getMessage());
            }
        });
    }

    /**
     * Sincroniza múltiples hábitos con el servidor.
     * @param habits Lista de hábitos a sincronizar
     * @param listener Callback para manejar la respuesta
     */
    public void syncHabits(List<Habit> habits, OnHabitsLoadedListener listener) {
        Call<HabitsResponse> call = apiService.syncHabits(habits);
        call.enqueue(new Callback<HabitsResponse>() {
            @Override
            public void onResponse(Call<HabitsResponse> call, Response<HabitsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    HabitsResponse habitsResponse = response.body();
                    if (habitsResponse.isSuccess()) {
                        listener.onSuccess(habitsResponse.getHabits());
                    } else {
                        listener.onError(habitsResponse.getMessage() != null ? 
                                habitsResponse.getMessage() : "Error desconocido");
                    }
                } else {
                    String error = "Error al sincronizar hábitos: " + response.code();
                    listener.onError(error);
                }
            }

            @Override
            public void onFailure(Call<HabitsResponse> call, Throwable t) {
                Log.e(TAG, "Error al sincronizar hábitos", t);
                listener.onError("Error de conexión: " + t.getMessage());
            }
        });
    }
}

