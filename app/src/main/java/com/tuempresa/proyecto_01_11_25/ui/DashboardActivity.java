package com.tuempresa.proyecto_01_11_25.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tuempresa.proyecto_01_11_25.R;
import com.tuempresa.proyecto_01_11_25.database.HabitDatabaseHelper;
import com.tuempresa.proyecto_01_11_25.model.Habit;
import com.tuempresa.proyecto_01_11_25.model.HabitEvent;
import com.tuempresa.proyecto_01_11_25.model.HabitEventStore;
import com.tuempresa.proyecto_01_11_25.sensors.AccelerometerSensorManager;
import com.tuempresa.proyecto_01_11_25.sensors.GyroSensorManager;
import com.tuempresa.proyecto_01_11_25.sensors.LightSensorManager;
import com.tuempresa.proyecto_01_11_25.sensors.StepSensorManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "DashboardPrefs";
    private static final String KEY_NIGHT_MODE = "night_mode";
    private static final String KEY_FOCUS_MODE = "focus_mode";
    private static final String KEY_HABITS_STATE = "habits_completed_state";
    private static final String KEY_LAST_RECREATION_TIME = "last_recreation_time";
    private static final String KEY_LAST_SETTINGS_CHANGE = "last_settings_change";
    private static final long SENSOR_DELAY_MS = 5000; // 5 segundos antes de activar sensores (evitar loops)
    private static final long LIGHT_DEBOUNCE_MS = 5000; // 5 segundos debounce (aumentado para evitar parpadeos)
    private static final long RECREATION_COOLDOWN_MS = 8000; // 8 segundos de cooldown después de recrear
    private static final long SETTINGS_CHANGE_COOLDOWN_MS = 2000; // 2 segundos después de cambiar settings

    private RecyclerView rv;
    private FloatingActionButton btnMap;
    private HabitAdapter adapter;

    private LightSensorManager lightSensor;
    private StepSensorManager walkSensor;
    private GyroSensorManager gyroSensor;
    private AccelerometerSensorManager accelerometerSensor;

    private List<Habit> habits;
    private FusedLocationProviderClient fused;
    private Handler mainHandler;
    private SharedPreferences prefs;
    private HabitDatabaseHelper dbHelper;

    private boolean focusMode = false;
    private boolean isNight = false;
    private boolean isRecreating = false; // Flag para evitar múltiples recreaciones
    private long lastLightChange = 0;
    private long activityCreateTime = 0;
    private long lastRecreationTime = 0; // Tiempo de la última recreación
    private boolean shouldOpenCameraAfterCreation = false; // Flag para abrir cámara después de crear hábito

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Inicializar handler PRIMERO para poder usarlo en el cooldown
        mainHandler = new Handler(Looper.getMainLooper());
        
        // Cargar estado persistente con valores por defecto: modo claro y foco desactivado
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        
        // Guardar tiempo de creación para delay inicial
        activityCreateTime = System.currentTimeMillis();
        
        // Cargar tiempo de última recreación desde SharedPreferences
        lastRecreationTime = prefs.getLong(KEY_LAST_RECREATION_TIME, 0);
        
        // Verificar si acabamos de recrear (evitar loops)
        long timeSinceLastRecreation = lastRecreationTime > 0 ? 
            System.currentTimeMillis() - lastRecreationTime : Long.MAX_VALUE;
        
        // Si acabamos de recrear, bloquear completamente cualquier otra recreación
        boolean justRecreated = timeSinceLastRecreation < RECREATION_COOLDOWN_MS;
        
        if (justRecreated) {
            android.util.Log.d("Dashboard", "⚠️ Recreación reciente, bloqueando cambios: " + timeSinceLastRecreation + "ms");
            isRecreating = true;
            // Mantener el flag por un tiempo adicional
            mainHandler.postDelayed(() -> {
                isRecreating = false;
                android.util.Log.d("Dashboard", "✅ Cooldown completado, sensores activos");
            }, RECREATION_COOLDOWN_MS - timeSinceLastRecreation);
        } else {
            isRecreating = false;
        }
        
        focusMode = prefs.getBoolean(KEY_FOCUS_MODE, false); // Por defecto: false (desactivado)
        isNight = prefs.getBoolean(KEY_NIGHT_MODE, false); // Por defecto: false (modo claro)
        
        // Cargar configuraciones de sensores
        boolean darkModeSensors = prefs.getBoolean("dark_mode_sensors", false);
        boolean focusModeSensors = prefs.getBoolean("focus_mode_sensors", false);
        
        android.util.Log.d("Dashboard", "onCreate - focusMode: " + focusMode + ", isNight: " + isNight + ", darkModeSensors: " + darkModeSensors + ", focusModeSensors: " + focusModeSensors);
        
        // Aplicar tema ANTES de setContentView (crítico)
        // El tema debe aplicarse después de cargar todas las preferencias
        applyTheme();
        
        // Configurar modo nocturno según el estado guardado (solo si NO está en modo foco)
        // Si acabamos de recrear, NO cambiar AppCompatDelegate para evitar loops
        if (!justRecreated && !focusMode) {
            // Solo sincronizar AppCompatDelegate si no acabamos de recrear y no está en modo foco
            int currentNightMode = AppCompatDelegate.getDefaultNightMode();
            int desiredNightMode = isNight ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
            
            if (currentNightMode != desiredNightMode) {
                android.util.Log.d("Dashboard", "Sincronizando modo nocturno: " + desiredNightMode);
                AppCompatDelegate.setDefaultNightMode(desiredNightMode);
            }
        } else if (justRecreated) {
            android.util.Log.d("Dashboard", "⚠️ Saltando sincronización de modo nocturno (en cooldown, tema ya aplicado)");
        }
        
        setContentView(R.layout.activity_dashboard);
        
        // Reconfigurar barra de navegación después de setContentView (especialmente importante en modo foco)
        setupBottomNavigation();

        fused = LocationServices.getFusedLocationProviderClient(this);

        // 🔥 Inicializar base de datos
        dbHelper = new HabitDatabaseHelper(this);

        // 🔥 Inicializar HabitEventStore para cargar eventos guardados
        HabitEventStore.init(this);

        // 🔥 Cargar hábitos desde base de datos
        habits = dbHelper.getAllHabits();
        
        // Si no hay hábitos, cargar desde SharedPreferences (migración)
        if (habits.isEmpty()) {
            habits = loadHabitsWithState();
            // Migrar a base de datos
            for (Habit habit : habits) {
                dbHelper.insertHabit(
                    habit.getTitle(),
                    habit.getGoal(),
                    habit.getCategory(),
                    habit.getType().name(),
                    10
                );
            }
        }

        rv = findViewById(R.id.rvHabits);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HabitAdapter(habits, 
            this::completeDemoHabit,
            this::editHabit,
            this::deleteHabit
        );
        rv.setAdapter(adapter);

        btnMap = findViewById(R.id.btnMap);
        btnMap.setOnClickListener(v -> startActivity(new Intent(this, MapActivity.class)));

        // Botón de configuración
        android.widget.ImageButton btnSettings = findViewById(R.id.btnSettings);
        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        }

        FloatingActionButton btnCamera = findViewById(R.id.btnCamera);
        btnCamera.setOnClickListener(v -> {
            // Verificar permiso de cámara
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) 
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, 
                    new String[]{android.Manifest.permission.CAMERA}, 100);
            } else {
                openCameraForReading();
            }
        });

        // Botón temporal para resetear estado (solo para debugging - remover en producción)
        com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton fabAddHabit = findViewById(R.id.fabAddHabit);
        if (fabAddHabit != null) {
            fabAddHabit.setOnClickListener(v -> {
                startActivityForResult(new Intent(this, SelectHabitTypeActivity.class), 500);
            });
        }

        // 🚶 Sensor de caminar - iniciar inmediatamente (no afecta UI)
        walkSensor = new StepSensorManager(this, () -> {
            // Callback cuando se completa la caminata
            completeHabitByType(Habit.HabitType.WALK);
        });
        walkSensor.start();

        // 🔦 Sensor de luz — modo nocturno dinámico (solo si está habilitado en configuración)
        if (darkModeSensors) {
            lightSensor = new LightSensorManager(this, new LightSensorManager.OnLowLightListener() {
                @Override
                public void onLowLight() {
                    handleLightChange(true);
                }

                @Override
                public void onNormalLight() {
                    handleLightChange(false);
                }
            });
        } else {
            lightSensor = null;
        }
        
        // 🏋️ Sensor de acelerómetro — detección de ejercicio/movimiento
        accelerometerSensor = new AccelerometerSensorManager(this, () -> {
            // Callback cuando se detecta ejercicio (movimiento continuo)
            completeHabitByType(Habit.HabitType.EXERCISE);
        });

        // 🧘 Sensor de giros — modo foco azul (solo si está habilitado en configuración)
        if (focusModeSensors) {
            gyroSensor = new GyroSensorManager(this, this::activateFocusMode);
        } else {
            gyroSensor = null;
        }
        
        // Iniciar sensores después de un delay para evitar loops
        long delayBeforeStartingSensors = SENSOR_DELAY_MS;
        
        if (justRecreated) {
            // Si acabamos de recrear, esperar el cooldown completo + delay adicional
            long remainingCooldown = RECREATION_COOLDOWN_MS - timeSinceLastRecreation;
            delayBeforeStartingSensors = remainingCooldown + SENSOR_DELAY_MS + 2000; // +2s extra de seguridad
            android.util.Log.d("Dashboard", "⚠️ Sensores en espera: cooldown restante " + remainingCooldown + "ms, delay total: " + delayBeforeStartingSensors + "ms");
        }
        
        // Crear variable final para usar en lambda
        final long finalDelay = delayBeforeStartingSensors;
        
        // Los sensores DEBEN activarse después del delay, incluso si justRecreated era true
        // porque el delay ya incluye el tiempo de cooldown
        mainHandler.postDelayed(() -> {
            // Verificar nuevamente si estamos en cooldown (por si acaso)
            long currentTimeSinceRecreation = lastRecreationTime > 0 ? 
                System.currentTimeMillis() - lastRecreationTime : Long.MAX_VALUE;
            
            // Limpiar lastRecreationTime si ya pasó mucho tiempo (más de 2x cooldown)
            // Esto evita que se quede bloqueado si la app se reinició
            if (currentTimeSinceRecreation > RECREATION_COOLDOWN_MS * 2) {
                lastRecreationTime = 0;
                prefs.edit().remove(KEY_LAST_RECREATION_TIME).apply();
                android.util.Log.d("Dashboard", "🧹 Limpiando lastRecreationTime (muy antiguo)");
            }
            
            if (currentTimeSinceRecreation >= RECREATION_COOLDOWN_MS && !isRecreating && !isFinishing() && !isDestroyed()) {
                if (lightSensor != null) lightSensor.start();
                accelerometerSensor.start();
                if (gyroSensor != null) gyroSensor.start();
                
                // Reiniciar lastLightChange para permitir cambios inmediatos después de activar sensores
                lastLightChange = 0;
                
                android.util.Log.d("Dashboard", "✅ Sensores activados después de delay: " + finalDelay + "ms (cooldown: " + currentTimeSinceRecreation + "ms)");
            } else {
                android.util.Log.d("Dashboard", "⚠️ Sensores NO activados: cooldown=" + currentTimeSinceRecreation + 
                    "ms, isRecreating=" + isRecreating + ", isFinishing=" + isFinishing());
            }
        }, finalDelay);
    }

    /**
     * Configura la barra de navegación inferior
     */
    private void setupBottomNavigation() {
        android.widget.FrameLayout bottomNavContainer = findViewById(R.id.bottomNavContainer);
        if (bottomNavContainer == null) {
            android.util.Log.e("Dashboard", "bottomNavContainer es null");
            return;
        }
        
        // Limpiar contenedor si ya tiene vistas
        bottomNavContainer.removeAllViews();
        
        try {
            com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = 
                new com.google.android.material.bottomnavigation.BottomNavigationView(this);
            
            // Configurar layout
            android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
            );
            bottomNav.setLayoutParams(params);
            
            // Configurar menú
            bottomNav.inflateMenu(R.menu.bottom_navigation);
            
            // Configurar colores programáticamente
            bottomNav.setItemIconTintList(createBottomNavColorStateList());
            bottomNav.setItemTextColor(createBottomNavColorStateList());
            
            // Configurar fondo para que sea visible en modo foco
            if (focusMode) {
                bottomNav.setBackgroundColor(android.graphics.Color.parseColor("#001440"));
            }
            
            // Configurar listener
            bottomNav.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_dashboard) {
                    // Ya estamos en dashboard
                    return true;
                } else if (itemId == R.id.nav_scores) {
                    startActivity(new Intent(this, ScoresActivity.class));
                    return true;
                }
                return false;
            });
            
            // Agregar al contenedor
            bottomNavContainer.addView(bottomNav);
            bottomNavContainer.setVisibility(android.view.View.VISIBLE);
            
            android.util.Log.d("Dashboard", "BottomNavigationView creado exitosamente");
        } catch (Exception e) {
            android.util.Log.e("Dashboard", "Error creando bottom nav", e);
            e.printStackTrace();
        }
    }
    
    /**
     * Crea un ColorStateList para el BottomNavigationView
     */
    private android.content.res.ColorStateList createBottomNavColorStateList() {
        int[][] states = new int[][]{
            new int[]{android.R.attr.state_checked},
            new int[]{-android.R.attr.state_checked}
        };
        int[] colors = new int[]{
            android.graphics.Color.parseColor("#FF5200"), // Naranja cuando está seleccionado
            android.graphics.Color.parseColor("#888888")  // Gris cuando no está seleccionado
        };
        return new android.content.res.ColorStateList(states, colors);
    }
    
    /**
     * Activa o desactiva las notificaciones según el modo foco
     */
    private void toggleNotifications(boolean enable) {
        try {
            android.app.NotificationManager notificationManager = 
                (android.app.NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            
            if (notificationManager == null) {
                android.util.Log.e("Dashboard", "NotificationManager es null");
                return;
            }
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                // Para Android 6.0+ necesitamos verificar si tenemos permiso
                if (!notificationManager.isNotificationPolicyAccessGranted()) {
                    // Si no tenemos permiso, intentar abrir la configuración
                    android.util.Log.d("Dashboard", "No hay permiso para cambiar notificaciones, abriendo configuración");
                    Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                    try {
                        startActivity(intent);
                        Toast.makeText(this, "Por favor, otorga permiso para controlar notificaciones", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        android.util.Log.e("Dashboard", "Error abriendo configuración de notificaciones", e);
                    }
                    return;
                }
                
                // Desactivar/activar notificaciones
                if (enable) {
                    // Activar notificaciones
                    notificationManager.setInterruptionFilter(android.app.NotificationManager.INTERRUPTION_FILTER_ALL);
                    android.util.Log.d("Dashboard", "Notificaciones activadas");
                } else {
                    // Desactivar notificaciones (modo No Molestar)
                    notificationManager.setInterruptionFilter(android.app.NotificationManager.INTERRUPTION_FILTER_NONE);
                    android.util.Log.d("Dashboard", "Notificaciones desactivadas (modo foco)");
                }
            } else {
                // Para versiones anteriores a Android 6.0, no podemos controlar notificaciones programáticamente
                android.util.Log.d("Dashboard", "Versión de Android no soporta control de notificaciones");
                Toast.makeText(this, "Esta función requiere Android 6.0 o superior", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            android.util.Log.e("Dashboard", "Error al cambiar estado de notificaciones", e);
        }
    }
    
    /**
     * Aplicar tema según el estado actual y configuración
     */
    private void applyTheme() {
        // Verificar configuraciones (cargar de nuevo para asegurar valores actuales)
        boolean currentFocusMode = prefs.getBoolean(KEY_FOCUS_MODE, false);
        boolean currentNightMode = prefs.getBoolean(KEY_NIGHT_MODE, false);
        boolean darkModeSensors = prefs.getBoolean("dark_mode_sensors", false);
        boolean focusModeSensors = prefs.getBoolean("focus_mode_sensors", false);
        
        android.util.Log.d("Dashboard", "applyTheme - currentFocusMode: " + currentFocusMode + ", currentNightMode: " + currentNightMode);
        
        // Tema FOCUS tiene prioridad absoluta - sobrescribe todo
        if (currentFocusMode) {
            setTheme(R.style.Theme_Proyecto_01_11_25_Focus);
            android.util.Log.d("Dashboard", "✅ Aplicando tema FOCUS (azul)");
        } else if (currentNightMode || darkModeSensors) {
            // Modo oscuro (manual o con sensores)
            setTheme(R.style.Theme_Proyecto_01_11_25);
            android.util.Log.d("Dashboard", "✅ Tema: Modo oscuro");
        } else {
            // Modo claro
            setTheme(R.style.Theme_Proyecto_01_11_25);
            android.util.Log.d("Dashboard", "✅ Tema: Modo claro");
        }
        
        // Forzar recreación del ActionBar si existe
        supportInvalidateOptionsMenu();
    }

    /**
     * Maneja cambios de luz con debounce y validación
     */
    private void handleLightChange(boolean isLowLight) {
        android.util.Log.d("Dashboard", "handleLightChange: isLowLight=" + isLowLight + ", isNight=" + isNight);
        
        long now = System.currentTimeMillis();
        
        // Evitar cambios durante recreación
        if (isRecreating) {
            android.util.Log.d("Dashboard", "Ignorando cambio: isRecreating=true");
            return;
        }
        
        // Evitar cambios poco tiempo después de crear la actividad
        long timeSinceCreation = now - activityCreateTime;
        if (timeSinceCreation < SENSOR_DELAY_MS) {
            android.util.Log.d("Dashboard", "Ignorando cambio: muy pronto después de crear (" + timeSinceCreation + "ms)");
            return;
        }
        
        // Evitar cambios poco tiempo después de recrear
        long timeSinceRecreation = lastRecreationTime > 0 ? now - lastRecreationTime : Long.MAX_VALUE;
        if (timeSinceRecreation < RECREATION_COOLDOWN_MS) {
            android.util.Log.d("Dashboard", "Ignorando cambio: en cooldown después de recrear (" + timeSinceRecreation + "ms < " + RECREATION_COOLDOWN_MS + "ms)");
            return;
        }

        // Debounce adicional (solo si lastLightChange > 0)
        // Si lastLightChange es 0, significa que acabamos de activar los sensores - permitir cambio inmediato
        if (lastLightChange > 0 && (now - lastLightChange < LIGHT_DEBOUNCE_MS)) {
            android.util.Log.d("Dashboard", "Ignorando cambio: debounce activo (" + (now - lastLightChange) + "ms < " + LIGHT_DEBOUNCE_MS + "ms)");
            return;
        }
        
        if (lastLightChange == 0) {
            android.util.Log.d("Dashboard", "✅ Primera detección después de activar sensores - permitiendo cambio");
        }

        // Solo cambiar si realmente cambió
        if ((isLowLight && !isNight) || (!isLowLight && isNight)) {
            lastLightChange = now;
            android.util.Log.d("Dashboard", "✅ Cambiando modo nocturno: " + isLowLight);
            changeNightMode(isLowLight);
        } else {
            android.util.Log.d("Dashboard", "No hay cambio real de estado");
        }
    }

    /**
     * Cambia el modo nocturno sin causar loops
     * NOTA: El sensor de luz puede sobrescribir el modo foco si detecta cambios significativos
     */
    private void changeNightMode(boolean enableNight) {
        if (isRecreating) {
            android.util.Log.d("Dashboard", "No se puede cambiar: isRecreating=" + isRecreating);
            return;
        }
        
        // Detener sensor ANTES de cambiar para evitar eventos durante la recreación
        if (lightSensor != null) {
            android.util.Log.d("Dashboard", "Deteniendo sensor de luz antes de cambiar tema");
            lightSensor.stop();
        }
        
        // Si está en modo foco, salir de él primero para permitir cambio de tema
        if (focusMode) {
            android.util.Log.d("Dashboard", "Saliendo del modo foco para cambiar tema");
            focusMode = false;
            prefs.edit().putBoolean(KEY_FOCUS_MODE, false).apply();
        }
        
        android.util.Log.d("Dashboard", "Cambiando a modo nocturno: " + enableNight);
        isNight = enableNight;
        prefs.edit().putBoolean(KEY_NIGHT_MODE, isNight).apply();
        
        AppCompatDelegate.setDefaultNightMode(
            enableNight ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
        
        Toast.makeText(this, enableNight ? "🌙 Modo oscuro activado" : "☀️ Modo claro activado", Toast.LENGTH_SHORT).show();
        safeRecreate();
    }

    /**
     * Activa el modo foco (azul) con giroscopio
     */
    private void activateFocusMode() {
        android.util.Log.d("Dashboard", "activateFocusMode llamado: isRecreating=" + isRecreating + ", focusMode=" + focusMode);
        
        if (isRecreating) {
            android.util.Log.d("Dashboard", "Recreando, ignorando activación");
            return;
        }
        
        // Permitir activar/desactivar modo foco con giros
        if (focusMode) {
            android.util.Log.d("Dashboard", "Desactivando modo foco (ya estaba activo)");
            focusMode = false;
            prefs.edit().putBoolean(KEY_FOCUS_MODE, false).apply();
            // Restaurar modo nocturno guardado
            AppCompatDelegate.setDefaultNightMode(
                isNight ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
            // Reactivar notificaciones
            toggleNotifications(true);
            Toast.makeText(this, "💙 Modo Foco Desactivado", Toast.LENGTH_SHORT).show();
        } else {
            android.util.Log.d("Dashboard", "Activando modo foco");
            focusMode = true;
            prefs.edit().putBoolean(KEY_FOCUS_MODE, true).apply();
            // Desactivar notificaciones
            toggleNotifications(false);
            Toast.makeText(this, "💙 Modo Foco Activado!", Toast.LENGTH_SHORT).show();

            // Registrar evento en mapa
            addLocationEvent("Modo Foco 🧘 Activado", HabitEvent.HabitType.FOCUS);
        }
        
        safeRecreate();
    }

    /**
     * Recrea la Activity de forma segura sin loops
     */
    private void safeRecreate() {
        if (isRecreating) {
            android.util.Log.d("Dashboard", "⚠️ Ya se está recreando, ignorando");
            return;
        }
        
        // Verificar cooldown antes de recrear
        long timeSinceLastRecreation = lastRecreationTime > 0 ? 
            System.currentTimeMillis() - lastRecreationTime : Long.MAX_VALUE;
        
        if (timeSinceLastRecreation < RECREATION_COOLDOWN_MS) {
            android.util.Log.d("Dashboard", "⚠️ Recreación bloqueada: en cooldown (" + timeSinceLastRecreation + "ms)");
            return;
        }
        
        android.util.Log.d("Dashboard", "✅ Iniciando recreación segura");
        isRecreating = true;
        lastRecreationTime = System.currentTimeMillis();
        
        // Guardar tiempo de recreación en SharedPreferences para persistir entre recreaciones
        prefs.edit().putLong(KEY_LAST_RECREATION_TIME, lastRecreationTime).apply();
        
        // Detener TODOS los sensores antes de recrear
        if (lightSensor != null) {
            lightSensor.stop();
            android.util.Log.d("Dashboard", "Sensor de luz detenido");
        }
        if (accelerometerSensor != null) {
            accelerometerSensor.stop();
            android.util.Log.d("Dashboard", "Sensor de acelerómetro detenido");
        }
        if (gyroSensor != null) {
            gyroSensor.stop();
            android.util.Log.d("Dashboard", "Sensor de giroscopio detenido");
        }
        
        // Limpiar handlers pendientes
        if (mainHandler != null) {
            mainHandler.removeCallbacksAndMessages(null);
        }
        
        // Recrear después de un delay más largo para asegurar que los sensores se detuvieron
        mainHandler.postDelayed(() -> {
            if (isFinishing() || isDestroyed()) {
                android.util.Log.d("Dashboard", "Activity ya destruida, no recrear");
                isRecreating = false;
                return;
            }
            
            android.util.Log.d("Dashboard", "🔄 Ejecutando recreate()");
            try {
                recreate();
            } catch (Exception e) {
                android.util.Log.e("Dashboard", "Error al recrear", e);
                isRecreating = false;
            }
        }, 1000); // Aumentado a 1 segundo para dar más tiempo
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Limpiar handlers pendientes
        if (mainHandler != null) {
            mainHandler.removeCallbacksAndMessages(null);
        }
        
        // Detener sensores
        if (walkSensor != null) walkSensor.stop();
        if (lightSensor != null) lightSensor.stop();
        if (accelerometerSensor != null) accelerometerSensor.stop();
        if (gyroSensor != null) gyroSensor.stop();
    }

    /**
     * Carga los hábitos predeterminados y restaura sus estados de completado
     */
    private List<Habit> loadHabitsWithState() {
        List<Habit> defaultHabits = Habit.defaultHabits();
        
        // Cargar estados guardados
        String habitsStateJson = prefs.getString(KEY_HABITS_STATE, null);
        if (habitsStateJson != null) {
            try {
                JSONObject stateJson = new JSONObject(habitsStateJson);
                
                // Restaurar estado de cada hábito
                for (Habit habit : defaultHabits) {
                    String habitKey = habit.getTitle(); // Usar título como key
                    if (stateJson.has(habitKey)) {
                        boolean completed = stateJson.getBoolean(habitKey);
                        habit.setCompleted(completed);
                        android.util.Log.d("Dashboard", "Restaurado estado de " + habitKey + ": " + completed);
                    }
                }
            } catch (JSONException e) {
                android.util.Log.e("Dashboard", "Error al cargar estados de hábitos", e);
            }
        }
        
        return defaultHabits;
    }

    /**
     * Guarda los estados de completado de todos los hábitos
     */
    private void saveHabitsState() {
        try {
            JSONObject stateJson = new JSONObject();
            
            for (Habit habit : habits) {
                stateJson.put(habit.getTitle(), habit.isCompleted());
            }
            
            prefs.edit().putString(KEY_HABITS_STATE, stateJson.toString()).apply();
            android.util.Log.d("Dashboard", "Estados de hábitos guardados");
        } catch (JSONException e) {
            android.util.Log.e("Dashboard", "Error al guardar estados de hábitos", e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == 200 && resultCode == RESULT_OK && data != null) {
            // Cámara desde botón principal (lectura)
            long habitId = data.getLongExtra("habit_id", -1);
            if (habitId > 0) {
                // Página detectada y agregada, actualizar Dashboard
                refreshHabitsList();
                
                // Mostrar Snackbar de confirmación
                com.google.android.material.snackbar.Snackbar.make(
                    findViewById(android.R.id.content),
                    "Página detectada y registrada 📘✔",
                    com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
                ).show();
            } else {
                // Comportamiento antiguo para READ (sin habit_id)
                String habitType = data.getStringExtra("habit_completed");
                if ("READ".equals(habitType)) {
                    completeHabitByType(Habit.HabitType.READ);
                }
            }
        } else if ((requestCode == 300 || requestCode == 301 || requestCode == 302) && resultCode == RESULT_OK) {
            // HabitDetailActivity, JournalingActivity o MeditationActivity
            // Recargar hábitos para actualizar progreso y estado
            refreshHabitsList();
        } else if (requestCode == 400 && resultCode == RESULT_OK) {
            // ConfigureHabitActivity (edición)
            refreshHabitsList();
        } else if (requestCode == 500 && resultCode == RESULT_OK) {
            // SelectHabitTypeActivity -> ConfigureHabitActivity (creación)
            refreshHabitsList();
            
            // Si se creó un hábito de leer desde el diálogo, abrir cámara automáticamente
            if (shouldOpenCameraAfterCreation) {
                shouldOpenCameraAfterCreation = false; // Resetear flag
                // Pequeño delay para que se recargue la lista
                mainHandler.postDelayed(() -> {
                    // Buscar el hábito de leer recién creado
                    habits = dbHelper.getAllHabits();
                    Habit readingHabit = null;
                    for (Habit habit : habits) {
                        if (habit.getType() == Habit.HabitType.READ_BOOK) {
                            readingHabit = habit;
                            break;
                        }
                    }
                    
                    if (readingHabit != null) {
                        Intent cameraIntent = new Intent(this, CameraActivity.class);
                        cameraIntent.putExtra("habit_id", readingHabit.getId());
                        cameraIntent.putExtra("habit_type", "READ_BOOK");
                        startActivityForResult(cameraIntent, 200);
                    }
                }, 500);
            }
        }
        
        // Recargar hábitos cuando se vuelve de crear/editar (para casos sin resultCode específico)
        if (dbHelper != null && resultCode == RESULT_OK) {
            refreshHabitsList();
        }
    }
    
    /**
     * Refresca la lista de hábitos y actualiza el adapter
     */
    private void refreshHabitsList() {
        if (dbHelper != null) {
            habits = dbHelper.getAllHabits();
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        // Verificar cooldown antes de hacer cualquier cambio
        long timeSinceLastRecreation = lastRecreationTime > 0 ? 
            System.currentTimeMillis() - lastRecreationTime : Long.MAX_VALUE;
        
        if (timeSinceLastRecreation < RECREATION_COOLDOWN_MS) {
            android.util.Log.d("Dashboard", "onResume - En cooldown, ignorando cambios: " + timeSinceLastRecreation + "ms");
            return;
        }
        
        // Verificar si hubo un cambio reciente en SettingsActivity
        long lastSettingsChange = prefs.getLong(KEY_LAST_SETTINGS_CHANGE, 0);
        long timeSinceSettingsChange = lastSettingsChange > 0 ? 
            System.currentTimeMillis() - lastSettingsChange : Long.MAX_VALUE;
        
        // Verificar si cambió el modo foco desde SettingsActivity
        boolean newFocusMode = prefs.getBoolean(KEY_FOCUS_MODE, false);
        boolean newNightMode = prefs.getBoolean(KEY_NIGHT_MODE, false);
        boolean darkModeSensors = prefs.getBoolean("dark_mode_sensors", false);
        boolean focusModeSensors = prefs.getBoolean("focus_mode_sensors", false);
        
        // IMPORTANTE: Detener sensor de luz si dark_mode_sensors está desactivado
        // Esto previene que el sensor interfiera cuando se activa el modo dark manual
        if (!darkModeSensors && lightSensor != null) {
            android.util.Log.d("Dashboard", "onResume - Deteniendo sensor de luz (dark_mode_sensors desactivado)");
            lightSensor.stop();
            lightSensor = null;
        }
        
        // IMPORTANTE: Detener sensor de giro si focus_mode_sensors está desactivado
        if (!focusModeSensors && gyroSensor != null) {
            android.util.Log.d("Dashboard", "onResume - Deteniendo sensor de giro (focus_mode_sensors desactivado)");
            gyroSensor.stop();
            gyroSensor = null;
        }
        
        // Si cambió el modo foco o modo nocturno, aplicar tema y recrear
        // PERO solo si no estamos en cooldown, no estamos recreando, y ha pasado suficiente tiempo desde el cambio en settings
        if ((newFocusMode != focusMode || newNightMode != isNight) && !isRecreating) {
            // Si el cambio fue muy reciente (menos de SETTINGS_CHANGE_COOLDOWN_MS), esperar un poco más
            // PERO si es modo foco, aplicar inmediatamente (tiene prioridad)
            if (timeSinceSettingsChange < SETTINGS_CHANGE_COOLDOWN_MS && !newFocusMode) {
                android.util.Log.d("Dashboard", "onResume - Cambio detectado pero muy reciente, esperando: " + timeSinceSettingsChange + "ms");
                // Programar recreación después del cooldown
                if (mainHandler != null) {
                    mainHandler.postDelayed(() -> {
                        if (!isFinishing() && !isDestroyed() && !isRecreating) {
                            boolean currentFocusMode = prefs.getBoolean(KEY_FOCUS_MODE, false);
                            boolean currentNightMode = prefs.getBoolean(KEY_NIGHT_MODE, false);
                            if (currentFocusMode != focusMode || currentNightMode != isNight) {
                                focusMode = currentFocusMode;
                                isNight = currentNightMode;
                                applyTheme();
                                safeRecreate();
                            }
                        }
                    }, SETTINGS_CHANGE_COOLDOWN_MS - timeSinceSettingsChange);
                }
                return;
            }
            
            android.util.Log.d("Dashboard", "onResume - Cambio detectado: focusMode=" + focusMode + " -> " + newFocusMode + ", isNight=" + isNight + " -> " + newNightMode);
            focusMode = newFocusMode;
            isNight = newNightMode;
            
            // Limpiar el timestamp de cambio de settings
            prefs.edit().remove(KEY_LAST_SETTINGS_CHANGE).apply();
            
            // Activar/desactivar notificaciones según modo foco
            toggleNotifications(!newFocusMode);
            
            // Aplicar tema y recrear
            applyTheme();
            safeRecreate();
            return; // Salir temprano para evitar inicializar sensores antes de recrear
        }
        
        // Recargar hábitos desde base de datos al volver
        if (dbHelper != null) {
            habits = dbHelper.getAllHabits();
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }
        
        // Reinicializar sensores solo si están habilitados y no existen
        if (darkModeSensors && lightSensor == null) {
            android.util.Log.d("Dashboard", "onResume - Inicializando sensor de luz (dark_mode_sensors activado)");
            lightSensor = new LightSensorManager(this, new LightSensorManager.OnLowLightListener() {
                @Override
                public void onLowLight() {
                    handleLightChange(true);
                }

                @Override
                public void onNormalLight() {
                    handleLightChange(false);
                }
            });
            if (!isRecreating) {
                lightSensor.start();
            }
        }
        
        if (focusModeSensors && gyroSensor == null) {
            android.util.Log.d("Dashboard", "onResume - Inicializando sensor de giro (focus_mode_sensors activado)");
            gyroSensor = new GyroSensorManager(this, this::activateFocusMode);
            if (!isRecreating) {
                gyroSensor.start();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            openCameraForReading();
        } else if (requestCode == 100) {
            Toast.makeText(this, "Se necesita permiso de cámara para leer", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Abre la cámara para detectar páginas del hábito de leer
     * Si no existe un hábito de leer, muestra diálogo para crearlo
     */
    private void openCameraForReading() {
        // Buscar hábito activo de tipo READ_BOOK
        Habit readingHabit = null;
        for (Habit habit : habits) {
            if (habit.getType() == Habit.HabitType.READ_BOOK) {
                readingHabit = habit;
                break;
            }
        }
        
        if (readingHabit != null) {
            // Existe hábito de leer, abrir cámara directamente
            Intent cameraIntent = new Intent(this, CameraActivity.class);
            cameraIntent.putExtra("habit_id", readingHabit.getId());
            cameraIntent.putExtra("habit_type", "READ_BOOK");
            startActivityForResult(cameraIntent, 200);
        } else {
            // No existe hábito de leer, mostrar diálogo
            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Hábito de lectura no encontrado")
                .setMessage("No tienes un hábito de lectura configurado.\n¿Deseas crearlo ahora?")
                .setPositiveButton("Crear hábito de leer", (dialog, which) -> {
                    // Marcar que se debe abrir la cámara después de crear
                    shouldOpenCameraAfterCreation = true;
                    // Abrir selector de tipo y luego configuración
                    Intent intent = new Intent(this, SelectHabitTypeActivity.class);
                    intent.putExtra("auto_select_type", "READ_BOOK");
                    startActivityForResult(intent, 500);
                })
                .setNegativeButton("Cancelar", null)
                .show();
        }
    }

    /**
     * Completa un hábito por su tipo (EXERCISE, WALK, READ, DEMO)
     */
    private void completeHabitByType(Habit.HabitType type) {
        for (Habit habit : habits) {
            if (habit.getType() == type && !habit.isCompleted()) {
                habit.setCompleted(true);
                
                // Actualizar en base de datos
                dbHelper.updateHabitCompleted(habit.getTitle(), true);
                
                // Agregar puntos
                int points = dbHelper.getHabitPoints(habit.getTitle());
                dbHelper.addScore(habit.getTitle(), points);
                
                // Guardar estado inmediatamente
                saveHabitsState();
                
                // Guardar evento en el mapa (excepto WALK que ya lo guarda StepSensorManager, READ lo guarda CameraActivity)
                if (type == Habit.HabitType.EXERCISE) {
                    addLocationEvent("Ejercicio ✅ Completado", HabitEvent.HabitType.EXERCISE);
                }
                // Nota: WALK ya guarda su evento en StepSensorManager, READ lo guarda CameraActivity, DEMO lo guarda en completeDemoHabit
                
                // Actualizar UI
                int position = habits.indexOf(habit);
                if (position >= 0) {
                    adapter.notifyItemChanged(position);
                } else {
                    adapter.notifyDataSetChanged();
                }
                
                android.util.Log.d("Dashboard", "Hábito completado: " + habit.getTitle() + " (+" + points + " puntos)");
                Toast.makeText(this, "✅ " + habit.getTitle() + " completado (+" + points + " pts)", Toast.LENGTH_SHORT).show();
                break;
            }
        }
    }

    /** ✅ Maneja el click en un hábito según su tipo */
    private void completeDemoHabit(Habit h) {
        // Si ya está completado, desmarcarlo (toggle) - solo para DEMO
        if (h.isCompleted() && h.getType() == Habit.HabitType.DEMO) {
            h.setCompleted(false);
            dbHelper.updateHabitCompleted(h.getTitle(), false);
            saveHabitsState();
            int position = habits.indexOf(h);
            if (position >= 0) {
                adapter.notifyItemChanged(position);
            } else {
                adapter.notifyDataSetChanged();
            }
            Toast.makeText(this, "Hábito desmarcado", Toast.LENGTH_SHORT).show();
            return;
        }

        // Abrir Activity específica según el tipo de hábito
        switch (h.getType()) {
            case READ_BOOK:
            case WATER:
            case COLD_SHOWER:
                startActivityForResult(new Intent(this, HabitDetailActivity.class)
                    .putExtra("habit_id", h.getId()), 300);
                break;
            case JOURNALING:
                startActivityForResult(new Intent(this, JournalingActivity.class)
                    .putExtra("habit_id", h.getId()), 301);
                break;
            case MEDITATE:
                startActivityForResult(new Intent(this, MeditationActivity.class)
                    .putExtra("habit_id", h.getId()), 302);
                break;
            case DEMO:
                // Completar DEMO manualmente
                h.setCompleted(true);
                dbHelper.updateHabitCompleted(h.getTitle(), true);
                int points = dbHelper.getHabitPoints(h.getTitle());
                dbHelper.addScore(h.getTitle(), points);
                saveHabitsState();
                addLocationEvent("Demo ✅ Completado", HabitEvent.HabitType.DEMO);
                int position = habits.indexOf(h);
                if (position >= 0) {
                    adapter.notifyItemChanged(position);
                } else {
                    adapter.notifyDataSetChanged();
                }
                Toast.makeText(this, "✅ " + h.getTitle() + " completado (+" + points + " pts)", Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(this,
                        "Esto se completa automáticamente con sensores ✅",
                        Toast.LENGTH_SHORT).show();
                break;
        }
    }

    /**
     * Edita un hábito existente - usa ConfigureHabitActivity específica por tipo
     */
    private void editHabit(Habit habit) {
        Intent intent = new Intent(this, ConfigureHabitActivity.class);
        intent.putExtra("habit_id", habit.getId());
        startActivityForResult(intent, 400);
    }
    
    /**
     * Elimina un hábito
     */
    private void deleteHabit(Habit habit) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Eliminar Hábito")
            .setMessage("¿Estás seguro de que quieres eliminar \"" + habit.getTitle() + "\"?")
            .setPositiveButton("Eliminar", (dialog, which) -> {
                long habitId = habit.getId();
                if (habitId > 0) {
                    boolean deleted = dbHelper.deleteHabit(habitId);
                    if (deleted) {
                        // Remover de la lista local
                        habits.remove(habit);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(this, "✅ Hábito eliminado", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "❌ Error al eliminar el hábito", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Si no tiene ID, buscar por título
                    long id = dbHelper.getHabitIdByTitle(habit.getTitle());
                    if (id > 0) {
                        boolean deleted = dbHelper.deleteHabit(id);
                        if (deleted) {
                            habits.remove(habit);
                            adapter.notifyDataSetChanged();
                            Toast.makeText(this, "✅ Hábito eliminado", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }
    
    private boolean hasFineLocationPermission() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == android.content.pm.PackageManager.PERMISSION_GRANTED;
    }

    @android.annotation.SuppressLint("MissingPermission")
    private void addLocationEvent(String message, HabitEvent.HabitType type) {
        if (!hasFineLocationPermission()) {
            android.util.Log.d("Dashboard", "Sin permiso de ubicación, se omite evento: " + message);
            return;
        }
        try {
            fused.getLastLocation().addOnSuccessListener(loc -> {
                if (loc != null) {
                    HabitEventStore.add(new HabitEvent(
                            loc.getLatitude(),
                            loc.getLongitude(),
                            message,
                            type
                    ));
                }
            });
        } catch (SecurityException se) {
            android.util.Log.w("Dashboard", "Permiso de ubicación rechazado en tiempo de ejecución", se);
        }
    }
}
