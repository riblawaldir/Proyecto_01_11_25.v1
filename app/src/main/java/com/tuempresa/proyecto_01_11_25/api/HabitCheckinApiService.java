package com.tuempresa.proyecto_01_11_25.api;

import com.tuempresa.proyecto_01_11_25.model.HabitCheckinDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Interfaz de servicio Retrofit para las operaciones de HabitCheckins (completados con ubicación).
 */
public interface HabitCheckinApiService {

    /**
     * Obtiene todos los checkins de HOY para el usuario autenticado.
     * @return Call con la lista de checkins de hoy
     */
    @GET("checkins/today")
    Call<List<HabitCheckinDto>> getTodayCheckins();

    /**
     * Crea un nuevo checkin (hábito completado con ubicación).
     * @param checkin El checkin a crear
     * @return Call con el checkin creado (incluyendo el ID asignado)
     */
    @POST("checkins")
    Call<HabitCheckinDto> createCheckin(@Body HabitCheckinDto checkin);

    /**
     * Elimina el checkin de HOY para un hábito específico.
     * @param habitId ID del hábito
     * @return Call vacío
     */
    @DELETE("checkins/today/{habitId}")
    Call<Void> deleteTodayCheckin(@Path("habitId") long habitId);
}

