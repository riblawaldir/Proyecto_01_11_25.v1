package com.tuempresa.proyecto_01_11_25.api;

import android.content.Context;
import android.util.Log;

import com.tuempresa.proyecto_01_11_25.utils.SessionManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Interceptor de OkHttp que agrega automáticamente el token JWT
 * al header Authorization de todas las peticiones.
 */
public class AuthInterceptor implements Interceptor {
    private static final String TAG = "AuthInterceptor";
    private final SessionManager sessionManager;

    public AuthInterceptor(Context context) {
        this.sessionManager = new SessionManager(context);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        
        // Obtener token del SessionManager
        String token = sessionManager.getToken();
        
        // Si hay token, agregarlo al header Authorization
        if (token != null && !token.isEmpty()) {
            Request.Builder requestBuilder = original.newBuilder()
                .header("Authorization", "Bearer " + token);
            
            Request newRequest = requestBuilder.build();
            Log.d(TAG, "Agregando token JWT a petición: " + original.url());
            return chain.proceed(newRequest);
        }
        
        // Si no hay token, proceder sin header Authorization
        // (útil para endpoints públicos como /auth/login y /auth/register)
        Log.d(TAG, "No hay token JWT, procediendo sin Authorization header");
        return chain.proceed(original);
    }
}

