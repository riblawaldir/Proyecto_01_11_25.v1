package com.tuempresa.proyecto_01_11_25.api;

import com.tuempresa.proyecto_01_11_25.model.Habit;
import com.tuempresa.proyecto_01_11_25.model.HabitsResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * Interfaz de servicio Retrofit para las operaciones CRUD de hábitos.
 * Define todos los endpoints de la API REST.
 */
public interface HabitApiService {

    /**
     * Obtiene todos los hábitos del servidor.
     * @return Call con la respuesta que contiene la lista de hábitos
     */
    @GET("habits")
    Call<HabitsResponse> getAllHabits();

    /**
     * Obtiene un hábito específico por su ID.
     * @param id ID del hábito
     * @return Call con el hábito encontrado
     */
    @GET("habits/{id}")
    Call<Habit> getHabitById(@Path("id") long id);

    /**
     * Crea un nuevo hábito en el servidor.
     * @param habit El hábito a crear
     * @return Call con el hábito creado (incluyendo el ID asignado)
     */
    @POST("habits")
    Call<Habit> createHabit(@Body Habit habit);

    /**
     * Actualiza un hábito existente.
     * @param id ID del hábito a actualizar
     * @param habit El hábito con los datos actualizados
     * @return Call con el hábito actualizado
     */
    @PUT("habits/{id}")
    Call<Habit> updateHabit(@Path("id") long id, @Body Habit habit);

    /**
     * Elimina un hábito del servidor.
     * @param id ID del hábito a eliminar
     * @return Call con la respuesta de éxito/error
     */
    @DELETE("habits/{id}")
    Call<HabitsResponse> deleteHabit(@Path("id") long id);

    /**
     * Sincroniza múltiples hábitos con el servidor (útil para sincronización batch).
     * @param habits Lista de hábitos a sincronizar
     * @return Call con la respuesta que contiene los hábitos sincronizados
     */
    @POST("habits/sync")
    Call<HabitsResponse> syncHabits(@Body List<Habit> habits);
}

