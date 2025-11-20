package com.tuempresa.proyecto_01_11_25.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.tuempresa.proyecto_01_11_25.R;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "DashboardPrefs";
    private static final String KEY_NIGHT_MODE = "night_mode";
    private static final String KEY_DARK_MODE_SENSORS = "dark_mode_sensors";
    private static final String KEY_FOCUS_MODE = "focus_mode";
    private static final String KEY_FOCUS_MODE_SENSORS = "focus_mode_sensors";

    private SharedPreferences prefs;
    private SwitchMaterial switchDarkMode;
    private SwitchMaterial switchDarkModeSensors;
    private SwitchMaterial switchFocusMode;
    private SwitchMaterial switchFocusModeSensors;
    
    // Guardar listeners para poder restaurarlos
    private android.widget.CompoundButton.OnCheckedChangeListener listenerDarkModeSensors;
    private android.widget.CompoundButton.OnCheckedChangeListener listenerFocusMode;
    private android.widget.CompoundButton.OnCheckedChangeListener listenerFocusModeSensors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        switchDarkMode = findViewById(R.id.switchDarkMode);
        switchDarkModeSensors = findViewById(R.id.switchDarkModeSensors);
        switchFocusMode = findViewById(R.id.switchFocusMode);
        switchFocusModeSensors = findViewById(R.id.switchFocusModeSensors);

        // Cargar estados guardados
        loadSettings();

        // Configurar listeners - guardar referencias antes de asignarlos
        listenerDarkModeSensors = (buttonView, isChecked) -> {
            if (isChecked) {
                // Desactivar otros modos
                switchDarkMode.setChecked(false);
                switchFocusMode.setChecked(false);
                switchFocusModeSensors.setChecked(false);
                
                prefs.edit()
                    .putBoolean(KEY_DARK_MODE_SENSORS, true)
                    .putBoolean(KEY_NIGHT_MODE, false)
                    .putBoolean(KEY_FOCUS_MODE, false)
                    .putBoolean(KEY_FOCUS_MODE_SENSORS, false)
                    .apply();
                
                Toast.makeText(this, "Modo Dark con Sensores activado", Toast.LENGTH_SHORT).show();
            } else {
                prefs.edit()
                    .putBoolean(KEY_DARK_MODE_SENSORS, false)
                    .apply();
                
                Toast.makeText(this, "Modo Dark con Sensores desactivado", Toast.LENGTH_SHORT).show();
            }
        };
        
        listenerFocusMode = (buttonView, isChecked) -> {
            if (isChecked) {
                // Desactivar otros modos
                switchDarkMode.setChecked(false);
                switchDarkModeSensors.setChecked(false);
                switchFocusModeSensors.setChecked(false);
                
                prefs.edit()
                    .putBoolean(KEY_FOCUS_MODE, true)
                    .putBoolean(KEY_NIGHT_MODE, false)
                    .putBoolean(KEY_DARK_MODE_SENSORS, false)
                    .putBoolean(KEY_FOCUS_MODE_SENSORS, false)
                    .putLong("last_settings_change", System.currentTimeMillis())
                    .apply();
                
                Toast.makeText(this, "Modo Foco activado", Toast.LENGTH_SHORT).show();
                // NO recrear SettingsActivity - DashboardActivity manejará la recreación
            } else {
                prefs.edit()
                    .putBoolean(KEY_FOCUS_MODE, false)
                    .putLong("last_settings_change", System.currentTimeMillis())
                    .apply();
                
                Toast.makeText(this, "Modo Foco desactivado", Toast.LENGTH_SHORT).show();
                // NO recrear SettingsActivity - DashboardActivity manejará la recreación
            }
        };
        
        listenerFocusModeSensors = (buttonView, isChecked) -> {
            if (isChecked) {
                // Desactivar otros modos
                switchDarkMode.setChecked(false);
                switchDarkModeSensors.setChecked(false);
                switchFocusMode.setChecked(false);
                
                prefs.edit()
                    .putBoolean(KEY_FOCUS_MODE_SENSORS, true)
                    .putBoolean(KEY_NIGHT_MODE, false)
                    .putBoolean(KEY_DARK_MODE_SENSORS, false)
                    .putBoolean(KEY_FOCUS_MODE, false)
                    .apply();
                
                Toast.makeText(this, "Modo Foco con Sensores activado", Toast.LENGTH_SHORT).show();
            } else {
                prefs.edit()
                    .putBoolean(KEY_FOCUS_MODE_SENSORS, false)
                    .apply();
                
                Toast.makeText(this, "Modo Foco con Sensores desactivado", Toast.LENGTH_SHORT).show();
            }
        };
        
        // Configurar listener para modo dark manual
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Desactivar otros modos (sin disparar sus listeners)
                switchDarkModeSensors.setOnCheckedChangeListener(null);
                switchDarkModeSensors.setChecked(false);
                switchDarkModeSensors.setOnCheckedChangeListener(listenerDarkModeSensors);
                
                switchFocusMode.setOnCheckedChangeListener(null);
                switchFocusMode.setChecked(false);
                switchFocusMode.setOnCheckedChangeListener(listenerFocusMode);
                
                switchFocusModeSensors.setOnCheckedChangeListener(null);
                switchFocusModeSensors.setChecked(false);
                switchFocusModeSensors.setOnCheckedChangeListener(listenerFocusModeSensors);
                
                // Activar modo dark
                prefs.edit()
                    .putBoolean(KEY_NIGHT_MODE, true)
                    .putBoolean(KEY_DARK_MODE_SENSORS, false)
                    .putBoolean(KEY_FOCUS_MODE, false)
                    .putBoolean(KEY_FOCUS_MODE_SENSORS, false)
                    .putLong("last_settings_change", System.currentTimeMillis())
                    .apply();
                
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                Toast.makeText(this, "Modo Dark activado", Toast.LENGTH_SHORT).show();
                // NO recrear SettingsActivity - DashboardActivity manejará la recreación
            } else {
                // Desactivar modo dark
                prefs.edit()
                    .putBoolean(KEY_NIGHT_MODE, false)
                    .putLong("last_settings_change", System.currentTimeMillis())
                    .apply();
                
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                Toast.makeText(this, "Modo claro activado", Toast.LENGTH_SHORT).show();
                // NO recrear SettingsActivity - DashboardActivity manejará la recreación
            }
        });

        // Asignar listeners guardados
        switchDarkModeSensors.setOnCheckedChangeListener(listenerDarkModeSensors);
        switchFocusMode.setOnCheckedChangeListener(listenerFocusMode);
        switchFocusModeSensors.setOnCheckedChangeListener(listenerFocusModeSensors);
    }

    private void loadSettings() {
        boolean darkMode = prefs.getBoolean(KEY_NIGHT_MODE, false);
        boolean darkModeSensors = prefs.getBoolean(KEY_DARK_MODE_SENSORS, false);
        boolean focusMode = prefs.getBoolean(KEY_FOCUS_MODE, false);
        boolean focusModeSensors = prefs.getBoolean(KEY_FOCUS_MODE_SENSORS, false);

        switchDarkMode.setChecked(darkMode);
        switchDarkModeSensors.setChecked(darkModeSensors);
        switchFocusMode.setChecked(focusMode);
        switchFocusModeSensors.setChecked(focusModeSensors);
    }
}

