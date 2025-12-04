package com.tuempresa.proyecto_01_11_25.api;

import com.tuempresa.proyecto_01_11_25.model.UserDto;
import com.tuempresa.proyecto_01_11_25.model.UserStatsResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Interfaz de servicio Retrofit para las operaciones de usuarios.
 */
public interface UserApiService {
    /**
     * Busca un usuario por email y devuelve sus datos con estadísticas.
     * @param email El correo electrónico del usuario a buscar
     * @return Call con la respuesta que contiene el usuario y sus estadísticas
     */
    @GET("users/by-email/{email}")
    Call<UserStatsResponse> getUserByEmailWithStats(@Path("email") String email);

    /**
     * Busca un usuario por ID y devuelve sus datos con estadísticas.
     * Primero obtiene el usuario y luego sus estadísticas.
     * @param id El ID del usuario a buscar
     * @return Call con el usuario
     */
    @GET("users/{id}")
    Call<UserDto> getUserById(@Path("id") long id);

    /**
     * Busca usuarios por nombre (búsqueda parcial).
     * @param name El nombre o parte del nombre a buscar
     * @return Call con la lista de usuarios que coinciden
     */
    @GET("users")
    Call<java.util.List<UserDto>> searchUsersByName(@Query("name") String name);

    /**
     * Obtiene las estadísticas de un usuario por ID.
     * @param id El ID del usuario
     * @return Call con las estadísticas del usuario
     */
    @GET("users/{id}/stats")
    Call<UserStatsResponse.UserStats> getUserStats(@Path("id") long id);
}

