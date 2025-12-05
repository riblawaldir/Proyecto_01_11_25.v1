package com.tuempresa.proyecto_01_11_25.api;

import android.content.Context;
import android.util.Log;

import com.tuempresa.proyecto_01_11_25.model.HabitCheckinDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Clase helper para facilitar el uso de la API de HabitCheckins.
 * Proporciona métodos de alto nivel con callbacks para manejar las respuestas.
 */
public class HabitCheckinApiHelper {
    private static final String TAG = "HabitCheckinApiHelper";
    private HabitCheckinApiService apiService;

    public HabitCheckinApiHelper() {
        try {
            this.apiService = HabitApiClient.getInstance().getHabitCheckinApiService();
        } catch (IllegalStateException e) {
            Log.e(TAG, "HabitApiClient no inicializado. Usar constructor con Context.", e);
        }
    }

    public HabitCheckinApiHelper(Context context) {
        this.apiService = HabitApiClient.getInstance(context).getHabitCheckinApiService();
    }

    /**
     * Interfaz para callback cuando se obtienen checkins.
     */
    public interface OnCheckinsReceivedListener {
        void onSuccess(List<HabitCheckinDto> checkins);
        void onError(String error);
    }

    /**
     * Interfaz para callback cuando se crea un checkin.
     */
    public interface OnCheckinSavedListener {
        void onSuccess(HabitCheckinDto checkin);
        void onError(String error);
    }

    /**
     * Interfaz para callback cuando se elimina un checkin.
     */
    public interface OnCheckinDeletedListener {
        void onSuccess();
        void onError(String error);
    }

    /**
     * Obtiene todos los checkins de HOY para el usuario autenticado.
     * @param listener Callback para manejar la respuesta
     */
    public void getTodayCheckins(OnCheckinsReceivedListener listener) {
        Call<List<HabitCheckinDto>> call = apiService.getTodayCheckins();
        call.enqueue(new Callback<List<HabitCheckinDto>>() {
            @Override
            public void onResponse(Call<List<HabitCheckinDto>> call, Response<List<HabitCheckinDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listener.onSuccess(response.body());
                } else {
                    String error = "Error al obtener checkins: " + response.code();
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
            public void onFailure(Call<List<HabitCheckinDto>> call, Throwable t) {
                Log.e(TAG, "Error al obtener checkins", t);
                listener.onError("Error de conexión: " + t.getMessage());
            }
        });
    }

    /**
     * Crea un nuevo checkin en el servidor.
     * @param checkin El checkin a crear
     * @param listener Callback para manejar la respuesta
     */
    public void createCheckin(HabitCheckinDto checkin, OnCheckinSavedListener listener) {
        Call<HabitCheckinDto> call = apiService.createCheckin(checkin);
        call.enqueue(new Callback<HabitCheckinDto>() {
            @Override
            public void onResponse(Call<HabitCheckinDto> call, Response<HabitCheckinDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listener.onSuccess(response.body());
                } else {
                    String error = "Error al crear checkin: " + response.code();
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
            public void onFailure(Call<HabitCheckinDto> call, Throwable t) {
                Log.e(TAG, "Error al crear checkin", t);
                listener.onError("Error de conexión: " + t.getMessage());
            }
        });
    }

    /**
     * Elimina el checkin de HOY para un hábito específico.
     * @param habitId ID del hábito
     * @param listener Callback para manejar la respuesta
     */
    public void deleteTodayCheckin(long habitId, OnCheckinDeletedListener listener) {
        Call<Void> call = apiService.deleteTodayCheckin(habitId);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    listener.onSuccess();
                } else {
                    String error = "Error al eliminar checkin: " + response.code();
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
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Error al eliminar checkin", t);
                listener.onError("Error de conexión: " + t.getMessage());
            }
        });
    }
}

