package com.tuempresa.proyecto_01_11_25.api;

import com.tuempresa.proyecto_01_11_25.model.Score;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Interfaz de servicio Retrofit para las operaciones de Scores.
 */
public interface ScoreApiService {

    /**
     * Crea un nuevo score en el servidor.
     * @param score El score a crear
     * @return Call con el score creado (incluyendo el ID asignado)
     */
    @POST("scores")
    Call<Score> createScore(@Body Score score);
}

