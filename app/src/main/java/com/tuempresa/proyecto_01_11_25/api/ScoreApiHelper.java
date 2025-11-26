package com.tuempresa.proyecto_01_11_25.api;

import android.util.Log;

import com.tuempresa.proyecto_01_11_25.model.Score;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Clase helper para facilitar el uso de la API de scores.
 * Proporciona métodos de alto nivel con callbacks para manejar las respuestas.
 */
public class ScoreApiHelper {
    private static final String TAG = "ScoreApiHelper";
    private ScoreApiService apiService;

    public ScoreApiHelper() {
        this.apiService = HabitApiClient.getInstance().getScoreApiService();
    }

    /**
     * Interfaz para callback cuando se crea un score.
     */
    public interface OnScoreSavedListener {
        void onSuccess(Score score);
        void onError(String error);
    }

    /**
     * Crea un nuevo score en el servidor.
     * @param score El score a crear
     * @param listener Callback para manejar la respuesta
     */
    public void createScore(Score score, OnScoreSavedListener listener) {
        Call<Score> call = apiService.createScore(score);
        call.enqueue(new Callback<Score>() {
            @Override
            public void onResponse(Call<Score> call, Response<Score> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listener.onSuccess(response.body());
                } else {
                    String error = "Error al crear score: " + response.code();
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
            public void onFailure(Call<Score> call, Throwable t) {
                Log.e(TAG, "Error al crear score", t);
                listener.onError("Error de conexión: " + t.getMessage());
            }
        });
    }
}

