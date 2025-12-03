package com.tuempresa.proyecto_01_11_25.api;

import com.tuempresa.proyecto_01_11_25.model.AuthResponse;
import com.tuempresa.proyecto_01_11_25.model.LoginRequest;
import com.tuempresa.proyecto_01_11_25.model.RegisterRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Interfaz de servicio Retrofit para autenticación.
 */
public interface AuthApiService {
    /**
     * Inicia sesión en el servidor.
     * @param request Datos de login (email, password)
     * @return Call con la respuesta de autenticación (token, user)
     */
    @POST("auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    /**
     * Registra un nuevo usuario en el servidor.
     * @param request Datos de registro (email, password, displayName)
     * @return Call con la respuesta de autenticación (token, user)
     */
    @POST("auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);
}

