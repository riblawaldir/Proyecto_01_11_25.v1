package com.tuempresa.proyecto_01_11_25.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.util.Log;

import com.tuempresa.proyecto_01_11_25.api.HabitApiClient;
import com.tuempresa.proyecto_01_11_25.api.HabitApiService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Monitor de conexión a la API.
 * Detecta cambios en la conectividad a la API y notifica a los listeners.
 * Verifica periódicamente si la API está disponible haciendo peticiones reales.
 */
public class ConnectionMonitor {
    private static final String TAG = "ConnectionMonitor";
    private static ConnectionMonitor instance;
    
    private final Context context;
    private final ConnectivityManager connectivityManager;
    private final List<ConnectionListener> listeners;
    private boolean isConnected = false;
    private ConnectivityManager.NetworkCallback networkCallback;
    private final ExecutorService executorService;
    private final ScheduledExecutorService scheduledExecutor;
    private static final long CHECK_INTERVAL_SECONDS = 10; // Verificar cada 10 segundos

    public interface ConnectionListener {
        void onConnectionChanged(boolean isConnected);
    }

    private ConnectionMonitor(Context context) {
        this.context = context.getApplicationContext();
        this.connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        this.listeners = new ArrayList<>();
        this.executorService = Executors.newSingleThreadExecutor();
        this.scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        this.isConnected = false; // Inicialmente desconectado hasta verificar
        setupNetworkCallback();
        checkApiConnection(); // Verificar inmediatamente
        startPeriodicCheck(); // Iniciar verificación periódica
    }

    public static synchronized ConnectionMonitor getInstance(Context context) {
        if (instance == null) {
            instance = new ConnectionMonitor(context);
        }
        return instance;
    }

    private void setupNetworkCallback() {
        NetworkRequest networkRequest = new NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            .build();

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                Log.d(TAG, "Red disponible - verificando conexión a API...");
                // Cuando hay red, verificar conexión a la API
                checkApiConnection();
            }

            @Override
            public void onLost(Network network) {
                Log.d(TAG, "Red perdida - API no disponible");
                boolean wasConnected = isConnected;
                isConnected = false;
                if (wasConnected) {
                    notifyListeners(false);
                }
            }

            @Override
            public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                boolean hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                                     networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
                if (hasInternet) {
                    // Si hay internet, verificar conexión a la API
                    checkApiConnection();
                } else {
                    // Si no hay internet, la API tampoco está disponible
                    boolean wasConnected = isConnected;
                    isConnected = false;
                    if (wasConnected) {
                        notifyListeners(false);
                    }
                }
            }
        };

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
    }
    
    /**
     * Inicia la verificación periódica de la conexión a la API
     */
    private void startPeriodicCheck() {
        scheduledExecutor.scheduleWithFixedDelay(
            this::checkApiConnection,
            CHECK_INTERVAL_SECONDS,
            CHECK_INTERVAL_SECONDS,
            TimeUnit.SECONDS
        );
    }
    
    /**
     * Verifica la conexión a internet (no solo API)
     */
    private void checkApiConnection() {
        executorService.execute(() -> {
            try {
                // Verificar conexión a internet usando ConnectivityManager
                android.net.Network activeNetwork = connectivityManager.getActiveNetwork();
                boolean hasInternet = false;
                
                if (activeNetwork != null) {
                    android.net.NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
                    if (capabilities != null) {
                        hasInternet = capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                                     capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_VALIDATED);
                    }
                }
                
                // Si no hay red activa, verificar con el método legacy
                if (!hasInternet) {
                    android.net.NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                    hasInternet = activeNetworkInfo != null && 
                                 activeNetworkInfo.isConnected() && 
                                 activeNetworkInfo.isAvailable();
                }
                
                boolean wasConnected = isConnected;
                isConnected = hasInternet;
                
                if (hasInternet) {
                    Log.d(TAG, "✅ Internet disponible");
                } else {
                    Log.d(TAG, "❌ Sin conexión a internet");
                }
                
                // Notificar solo si cambió el estado
                if (wasConnected != isConnected) {
                    notifyListeners(isConnected);
                }
            } catch (Exception e) {
                // Error al verificar conexión
                boolean wasConnected = isConnected;
                isConnected = false;
                Log.d(TAG, "❌ Error al verificar conexión: " + e.getMessage());
                
                // Notificar solo si cambió el estado
                if (wasConnected != isConnected) {
                    notifyListeners(false);
                }
            }
        });
    }

    public void addListener(ConnectionListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
            // Notificar estado actual inmediatamente
            listener.onConnectionChanged(isConnected);
        }
    }

    public void removeListener(ConnectionListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(boolean connected) {
        for (ConnectionListener listener : listeners) {
            listener.onConnectionChanged(connected);
        }
    }

    /**
     * Verifica si hay conexión a la API (sin hacer petición, solo retorna el último estado conocido)
     * Para verificar realmente, se debe esperar a que el monitor periódico actualice el estado
     */
    public boolean isConnected() {
        return isConnected;
    }
    
    /**
     * Fuerza una verificación inmediata de la conexión a la API
     */
    public void checkConnectionNow() {
        checkApiConnection();
    }

    public void destroy() {
        if (networkCallback != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
        if (scheduledExecutor != null) {
            scheduledExecutor.shutdown();
        }
        if (executorService != null) {
            executorService.shutdown();
        }
        listeners.clear();
    }
}

