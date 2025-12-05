package com.tuempresa.proyecto_01_11_25.api;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tuempresa.proyecto_01_11_25.model.Habit;
import com.tuempresa.proyecto_01_11_25.utils.FlexibleDateAdapter;
import com.tuempresa.proyecto_01_11_25.utils.HabitTypeAdapter;

import java.util.Date;

import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

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
    // Para desarrollo local en emulador: "http://10.0.2.2:5098/api/v1/" (puerto por defecto de .NET)
    // Para desarrollo local en dispositivo físico: "http://10.26.9.139:5098/api/v1/" (IP de tu PC en la red local)
    // Para producción: "https://demopagina.somee.com/api/v1/" o la URL donde esté desplegada la API
    // IMPORTANTE: Verifica el puerto en launchSettings.json de la API (puede ser 5098, 5000, etc.)
    
    // URL de producción en internet (API desplegada en Somee.com)
    // NOTA: Si Somee no tiene SSL válido, cambiar a HTTP
    private static final String PRODUCTION_API_URL = "http://habitusplus.somee.com/api/v1/";
    
    // Detectar automáticamente si es emulador, dispositivo físico o producción
    private static String getBaseUrl(Context context) {
        // Verificar si está corriendo en emulador
        boolean isEmulator = android.os.Build.FINGERPRINT.startsWith("generic")
                || android.os.Build.FINGERPRINT.startsWith("unknown")
                || android.os.Build.MODEL.contains("google_sdk")
                || android.os.Build.MODEL.contains("Emulator")
                || android.os.Build.MODEL.contains("Android SDK built for x86")
                || android.os.Build.MANUFACTURER.contains("Genymotion")
                || (android.os.Build.BRAND.startsWith("generic") && android.os.Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(android.os.Build.PRODUCT);
        
        if (isEmulator) {
            // Emulador: usar 10.0.2.2 que es el alias de localhost del host
            return "http://10.0.2.2:5098/api/v1/";
        } else {
            // Dispositivo físico: usar la API en internet (producción)
            // Esto permite que cualquier persona use la app desde cualquier lugar
            return PRODUCTION_API_URL;
            
            // Si quieres usar desarrollo local en dispositivo físico, descomenta la siguiente línea:
            // return "http://10.26.9.139:5098/api/v1/";
        }
    }
    
    private static String BASE_URL = null; // Se inicializará dinámicamente
    
    private static HabitApiClient instance;
    private HabitApiService apiService;
    private ScoreApiService scoreApiService;
    private AuthApiService authApiService;
    private UserApiService userApiService;
    private HabitCheckinApiService habitCheckinApiService;
    private Retrofit retrofit;
    private Context context;

    /**
     * Constructor privado para implementar patrón Singleton.
     * @param context Contexto de la aplicación (necesario para AuthInterceptor)
     */
    private HabitApiClient(Context context) {
        this.context = context.getApplicationContext();
        // Inicializar BASE_URL según el tipo de dispositivo
        if (BASE_URL == null) {
            BASE_URL = getBaseUrl(context);
            Log.d(TAG, "Base URL configurada: " + BASE_URL);
        }
        initializeRetrofit();
    }

    /**
     * Inicializa Retrofit con todos los interceptores y servicios.
     */
    private void initializeRetrofit() {
        // Configurar Gson con adaptadores personalizados
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Habit.HabitType.class, new HabitTypeAdapter())
                .registerTypeAdapter(Date.class, new FlexibleDateAdapter())
                .setLenient()
                .create();

        // Configurar interceptor de logging (solo en modo debug)
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Configurar OkHttpClient con timeout y AuthInterceptor
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS);

        // Agregar AuthInterceptor si hay contexto
        if (context != null) {
            clientBuilder.addInterceptor(new AuthInterceptor(context));
        }

        // Configurar SSL para aceptar certificados (necesario si Somee tiene certificado no válido)
        // SOLO PARA DESARROLLO - En producción deberías usar certificados válidos
        try {
            // Crear un TrustManager que acepta todos los certificados
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{};
                    }
                }
            };

            // Crear SSLContext
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            // Configurar HostnameVerifier para aceptar todos los hostnames
            HostnameVerifier hostnameVerifier = new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };

            // Aplicar configuración SSL solo si la URL es HTTPS
            if (BASE_URL != null && BASE_URL.startsWith("https://")) {
                clientBuilder.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0]);
                clientBuilder.hostnameVerifier(hostnameVerifier);
                Log.d(TAG, "SSL configurado para aceptar certificados (solo desarrollo)");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al configurar SSL", e);
        }

        OkHttpClient okHttpClient = clientBuilder.build();

        // Configurar Retrofit
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        apiService = retrofit.create(HabitApiService.class);
        scoreApiService = retrofit.create(ScoreApiService.class);
        authApiService = retrofit.create(AuthApiService.class);
        userApiService = retrofit.create(UserApiService.class);
        habitCheckinApiService = retrofit.create(HabitCheckinApiService.class);
    }

    /**
     * Obtiene la instancia singleton del cliente API.
     * @param context Contexto de la aplicación
     * @return Instancia de HabitApiClient
     */
    public static synchronized HabitApiClient getInstance(Context context) {
        if (instance == null) {
            instance = new HabitApiClient(context);
        }
        return instance;
    }

    /**
     * Obtiene la instancia singleton del cliente API (método de compatibilidad).
     * @return Instancia de HabitApiClient
     * @deprecated Usar getInstance(Context) en su lugar
     */
    @Deprecated
    public static synchronized HabitApiClient getInstance() {
        if (instance == null) {
            throw new IllegalStateException("HabitApiClient debe inicializarse con getInstance(Context) primero");
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
     * Obtiene el servicio de Score API configurado.
     * @return ScoreApiService para realizar llamadas a la API de scores
     */
    public ScoreApiService getScoreApiService() {
        return scoreApiService;
    }

    /**
     * Obtiene el servicio de Auth API configurado.
     * @return AuthApiService para realizar llamadas de autenticación
     */
    public AuthApiService getAuthApiService() {
        return authApiService;
    }

    /**
     * Obtiene el servicio de User API configurado.
     * @return UserApiService para realizar llamadas de usuarios
     */
    public UserApiService getUserApiService() {
        return userApiService;
    }

    /**
     * Obtiene el servicio de HabitCheckin API configurado.
     * @return HabitCheckinApiService para realizar llamadas de checkins
     */
    public HabitCheckinApiService getHabitCheckinApiService() {
        return habitCheckinApiService;
    }

    /**
     * Permite cambiar la URL base de la API (útil para testing o diferentes entornos).
     * @param baseUrl Nueva URL base
     */
    public void setBaseUrl(String baseUrl) {
        BASE_URL = baseUrl;
        initializeRetrofit();
        Log.d(TAG, "Base URL actualizada a: " + baseUrl);
    }

    /**
     * Obtiene la URL base actual.
     * @return URL base configurada
     */
    public String getBaseUrl() {
        return BASE_URL != null ? BASE_URL : "http://10.0.2.2:5098/api/v1/";
    }
}

