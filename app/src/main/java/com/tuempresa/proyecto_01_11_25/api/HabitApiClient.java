package com.tuempresa.proyecto_01_11_25.api;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tuempresa.proyecto_01_11_25.model.Habit;
import com.tuempresa.proyecto_01_11_25.utils.HabitTypeAdapter;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Cliente singleton para manejar las llamadas a la API usando Retrofit.
 * Configura Retrofit con Gson y un adaptador personalizado para HabitType.
 */
public class HabitApiClient {
    private static final String TAG = "HabitApiClient";
    
    // URL base de la API - Configurar según tu servidor
    // Para desarrollo local: "https://localhost:7000/api/v1/" o "http://10.0.2.2:5000/api/v1/" (emulador Android)
    // Para Somee (producción): "https://[tu-proyecto].somee.com/api/v1/"
    // IMPORTANTE: Reemplaza [tu-proyecto] con el nombre real de tu proyecto en Somee
    private static final String BASE_URL = "https://demopagina.somee.com/api/v1/";
    
    private static HabitApiClient instance;
    private HabitApiService apiService;
    private Retrofit retrofit;

    /**
     * Constructor privado para implementar patrón Singleton.
     */
    private HabitApiClient() {
        // Configurar Gson con adaptador personalizado para HabitType
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Habit.HabitType.class, new HabitTypeAdapter())
                .setLenient()
                .create();

        // Configurar interceptor de logging (solo en modo debug)
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Configurar OkHttpClient con timeout
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        // Configurar Retrofit
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        apiService = retrofit.create(HabitApiService.class);
    }

    /**
     * Obtiene la instancia singleton del cliente API.
     * @return Instancia de HabitApiClient
     */
    public static synchronized HabitApiClient getInstance() {
        if (instance == null) {
            instance = new HabitApiClient();
        }
        return instance;
    }

    /**
     * Obtiene el servicio de API configurado.
     * @return HabitApiService para realizar llamadas a la API
     */
    public HabitApiService getApiService() {
        return apiService;
    }

    /**
     * Permite cambiar la URL base de la API (útil para testing o diferentes entornos).
     * @param baseUrl Nueva URL base
     */
    public void setBaseUrl(String baseUrl) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Habit.HabitType.class, new HabitTypeAdapter())
                .setLenient()
                .create();

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        apiService = retrofit.create(HabitApiService.class);
        Log.d(TAG, "Base URL actualizada a: " + baseUrl);
    }

    /**
     * Obtiene la URL base actual.
     * @return URL base configurada
     */
    public String getBaseUrl() {
        return BASE_URL;
    }
}

