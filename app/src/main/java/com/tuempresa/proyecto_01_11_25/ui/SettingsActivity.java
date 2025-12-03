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

    // Backup
    private com.google.android.material.button.MaterialButton btnExportData;
    private com.google.android.material.button.MaterialButton btnImportData;
    private com.tuempresa.proyecto_01_11_25.utils.BackupManager backupManager;
    
    private final androidx.activity.result.ActivityResultLauncher<android.content.Intent> exportLauncher =
            registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    android.net.Uri uri = result.getData().getData();
                    if (uri != null) {
                        performExport(uri);
                    }
                }
            });

    private final androidx.activity.result.ActivityResultLauncher<android.content.Intent> importLauncher =
            registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    android.net.Uri uri = result.getData().getData();
                    if (uri != null) {
                        performImport(uri);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        backupManager = new com.tuempresa.proyecto_01_11_25.utils.BackupManager(this);

        switchDarkMode = findViewById(R.id.switchDarkMode);
        switchDarkModeSensors = findViewById(R.id.switchDarkModeSensors);
        switchFocusMode = findViewById(R.id.switchFocusMode);
        switchFocusModeSensors = findViewById(R.id.switchFocusModeSensors);
        
        // btnExportData = findViewById(R.id.btnExportData);
        // btnImportData = findViewById(R.id.btnImportData);
        
        // setupBackupButtons();

        // Cargar estados guardados
        loadSettings();
        
        // Agregar botones de cuenta programáticamente
        addAccountButtons();

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
    
    private void setupBackupButtons() {
        btnExportData.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(android.content.Intent.CATEGORY_OPENABLE);
            intent.setType("application/json");
            intent.putExtra(android.content.Intent.EXTRA_TITLE, "habitus_backup_" + System.currentTimeMillis() + ".json");
            exportLauncher.launch(intent);
        });

        btnImportData.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Restaurar Copia de Seguridad")
                .setMessage("¿Deseas importar datos desde un archivo? Esto fusionará los hábitos existentes.")
                .setPositiveButton("Seleccionar Archivo", (dialog, which) -> {
                    android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(android.content.Intent.CATEGORY_OPENABLE);
                    intent.setType("application/json");
                    importLauncher.launch(intent);
                })
                .setNegativeButton("Cancelar", null)
                .show();
        });
    }
    
    private void performExport(android.net.Uri uri) {
        Toast.makeText(this, "Exportando datos...", Toast.LENGTH_SHORT).show();
        backupManager.exportData(uri, new com.tuempresa.proyecto_01_11_25.utils.BackupManager.OnBackupListener() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> Toast.makeText(SettingsActivity.this, message, Toast.LENGTH_LONG).show());
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(SettingsActivity.this, error, Toast.LENGTH_LONG).show());
            }
        });
    }
    
    private void performImport(android.net.Uri uri) {
        Toast.makeText(this, "Importando datos...", Toast.LENGTH_SHORT).show();
        backupManager.importData(uri, new com.tuempresa.proyecto_01_11_25.utils.BackupManager.OnBackupListener() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> Toast.makeText(SettingsActivity.this, message, Toast.LENGTH_LONG).show());
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(SettingsActivity.this, error, Toast.LENGTH_LONG).show());
            }
        });
    }

    private void addAccountButtons() {
        // Encontrar el LinearLayout principal del ScrollView
        android.view.ViewGroup mainLayout = findViewById(R.id.scrollView);
        if (mainLayout instanceof androidx.core.widget.NestedScrollView) {
            androidx.core.widget.NestedScrollView scrollView = (androidx.core.widget.NestedScrollView) mainLayout;
            if (scrollView.getChildCount() > 0 && scrollView.getChildAt(0) instanceof android.widget.LinearLayout) {
                android.widget.LinearLayout contentLayout = (android.widget.LinearLayout) scrollView.getChildAt(0);
                
                // Crear sección de cuenta
                android.widget.TextView accountTitle = new android.widget.TextView(this);
                accountTitle.setText("Cuenta");
                accountTitle.setTextColor(getResources().getColor(R.color.orangeStart));
                accountTitle.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 16);
                accountTitle.setTypeface(null, android.graphics.Typeface.BOLD);
                android.widget.LinearLayout.LayoutParams titleParams = new android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                );
                titleParams.setMargins(
                    (int) (4 * getResources().getDisplayMetrics().density),
                    (int) (24 * getResources().getDisplayMetrics().density),
                    0,
                    (int) (12 * getResources().getDisplayMetrics().density)
                );
                accountTitle.setLayoutParams(titleParams);
                contentLayout.addView(accountTitle);
                
                // Crear card para botones
                com.google.android.material.card.MaterialCardView card = new com.google.android.material.card.MaterialCardView(this);
                android.widget.LinearLayout.LayoutParams cardParams = new android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                );
                cardParams.setMargins(0, 0, 0, (int) (24 * getResources().getDisplayMetrics().density));
                card.setLayoutParams(cardParams);
                card.setRadius(16 * getResources().getDisplayMetrics().density);
                card.setCardElevation(2 * getResources().getDisplayMetrics().density);
                card.setCardBackgroundColor(getResources().getColor(R.color.white));
                
                // LinearLayout dentro del card
                android.widget.LinearLayout cardContent = new android.widget.LinearLayout(this);
                cardContent.setOrientation(android.widget.LinearLayout.VERTICAL);
                cardContent.setPadding(
                    (int) (20 * getResources().getDisplayMetrics().density),
                    (int) (20 * getResources().getDisplayMetrics().density),
                    (int) (20 * getResources().getDisplayMetrics().density),
                    (int) (20 * getResources().getDisplayMetrics().density)
                );
                
                // Botón de logout
                com.google.android.material.button.MaterialButton btnLogout = new com.google.android.material.button.MaterialButton(this);
                btnLogout.setText("Cerrar Sesión");
                btnLogout.setTextColor(getResources().getColor(android.R.color.white));
                btnLogout.setBackgroundColor(getResources().getColor(R.color.orange));
                btnLogout.setCornerRadius((int) (12 * getResources().getDisplayMetrics().density));
                android.widget.LinearLayout.LayoutParams logoutParams = new android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                );
                btnLogout.setLayoutParams(logoutParams);
                btnLogout.setOnClickListener(v -> logout());
                cardContent.addView(btnLogout);
                
                // Texto descriptivo logout
                android.widget.TextView logoutDesc = new android.widget.TextView(this);
                logoutDesc.setText("Cierra tu sesión actual. Podrás volver a iniciar sesión cuando quieras.");
                logoutDesc.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 12);
                logoutDesc.setTextColor(getResources().getColor(R.color.textLight));
                logoutDesc.setGravity(android.view.Gravity.CENTER);
                android.widget.LinearLayout.LayoutParams logoutDescParams = new android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                );
                logoutDescParams.setMargins(0, (int) (8 * getResources().getDisplayMetrics().density), 0, (int) (16 * getResources().getDisplayMetrics().density));
                logoutDesc.setLayoutParams(logoutDescParams);
                cardContent.addView(logoutDesc);
                
                // Botón de borrar cuenta
                com.google.android.material.button.MaterialButton btnDelete = new com.google.android.material.button.MaterialButton(this);
                btnDelete.setText("Borrar Cuenta");
                btnDelete.setTextColor(getResources().getColor(android.R.color.white));
                btnDelete.setBackgroundColor(0xFFD32F2F); // Rojo
                btnDelete.setCornerRadius((int) (12 * getResources().getDisplayMetrics().density));
                android.widget.LinearLayout.LayoutParams deleteParams = new android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                );
                deleteParams.setMargins(0, 0, 0, 0);
                btnDelete.setLayoutParams(deleteParams);
                btnDelete.setOnClickListener(v -> deleteAccount());
                cardContent.addView(btnDelete);
                
                // Texto descriptivo borrar cuenta
                android.widget.TextView deleteDesc = new android.widget.TextView(this);
                deleteDesc.setText("⚠️ Esta acción es permanente. Se eliminarán todos tus datos y no podrás recuperarlos.");
                deleteDesc.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 12);
                deleteDesc.setTextColor(0xFFD32F2F); // Rojo
                deleteDesc.setGravity(android.view.Gravity.CENTER);
                android.widget.LinearLayout.LayoutParams deleteDescParams = new android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                );
                deleteDescParams.setMargins(0, (int) (8 * getResources().getDisplayMetrics().density), 0, (int) (16 * getResources().getDisplayMetrics().density));
                deleteDesc.setLayoutParams(deleteDescParams);
                cardContent.addView(deleteDesc);
                
                // Botón de borrar todos los hábitos
                com.google.android.material.button.MaterialButton btnDeleteAllHabits = new com.google.android.material.button.MaterialButton(this);
                btnDeleteAllHabits.setText("Borrar Todos los Hábitos");
                btnDeleteAllHabits.setTextColor(getResources().getColor(android.R.color.white));
                btnDeleteAllHabits.setBackgroundColor(0xFFFF9800); // Naranja oscuro
                btnDeleteAllHabits.setCornerRadius((int) (12 * getResources().getDisplayMetrics().density));
                android.widget.LinearLayout.LayoutParams deleteHabitsParams = new android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                );
                deleteHabitsParams.setMargins(0, 0, 0, 0);
                btnDeleteAllHabits.setLayoutParams(deleteHabitsParams);
                btnDeleteAllHabits.setOnClickListener(v -> deleteAllHabits());
                cardContent.addView(btnDeleteAllHabits);
                
                // Texto descriptivo borrar hábitos
                android.widget.TextView deleteHabitsDesc = new android.widget.TextView(this);
                deleteHabitsDesc.setText("Elimina todos tus hábitos. Esta acción es permanente y no podrás recuperarlos.");
                deleteHabitsDesc.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 12);
                deleteHabitsDesc.setTextColor(0xFFFF9800); // Naranja
                deleteHabitsDesc.setGravity(android.view.Gravity.CENTER);
                android.widget.LinearLayout.LayoutParams deleteHabitsDescParams = new android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                );
                deleteHabitsDescParams.setMargins(0, (int) (8 * getResources().getDisplayMetrics().density), 0, (int) (16 * getResources().getDisplayMetrics().density));
                deleteHabitsDesc.setLayoutParams(deleteHabitsDescParams);
                cardContent.addView(deleteHabitsDesc);
                
                // Botón de limpiar base de datos local
                com.google.android.material.button.MaterialButton btnClearLocalDB = new com.google.android.material.button.MaterialButton(this);
                btnClearLocalDB.setText("Limpiar Datos Locales");
                btnClearLocalDB.setTextColor(getResources().getColor(R.color.orange));
                btnClearLocalDB.setBackgroundColor(android.graphics.Color.TRANSPARENT);
                btnClearLocalDB.setStrokeColor(getResources().getColorStateList(R.color.orange));
                btnClearLocalDB.setStrokeWidth((int) (2 * getResources().getDisplayMetrics().density));
                btnClearLocalDB.setCornerRadius((int) (12 * getResources().getDisplayMetrics().density));
                android.widget.LinearLayout.LayoutParams clearDBParams = new android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                );
                btnClearLocalDB.setLayoutParams(clearDBParams);
                btnClearLocalDB.setOnClickListener(v -> clearLocalDatabase());
                cardContent.addView(btnClearLocalDB);
                
                // Texto descriptivo clear DB
                android.widget.TextView clearDBDesc = new android.widget.TextView(this);
                clearDBDesc.setText("Elimina todos los datos guardados localmente. Los datos se sincronizarán desde la API cuando vuelvas a conectarte.");
                clearDBDesc.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 12);
                clearDBDesc.setTextColor(getResources().getColor(R.color.textLight));
                clearDBDesc.setGravity(android.view.Gravity.CENTER);
                android.widget.LinearLayout.LayoutParams clearDBDescParams = new android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                );
                clearDBDescParams.setMargins(0, (int) (8 * getResources().getDisplayMetrics().density), 0, 0);
                clearDBDesc.setLayoutParams(clearDBDescParams);
                cardContent.addView(clearDBDesc);
                
                card.addView(cardContent);
                contentLayout.addView(card);
            }
        }
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

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            logout();
            return true;
        } else if (item.getItemId() == R.id.action_delete_account) {
            deleteAccount();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Cerrar Sesión")
                .setMessage("¿Estás seguro de que deseas cerrar sesión?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    // CRÍTICO: Limpiar hábitos antes de cerrar sesión
                    // Esto asegura que no queden datos del usuario anterior en la BD local
                    com.tuempresa.proyecto_01_11_25.database.HabitDatabaseHelper dbHelper = 
                        new com.tuempresa.proyecto_01_11_25.database.HabitDatabaseHelper(this);
                    // Obtener userId antes de cerrar sesión
                    com.tuempresa.proyecto_01_11_25.utils.SessionManager sessionManager = 
                            new com.tuempresa.proyecto_01_11_25.utils.SessionManager(this);
                    long userId = sessionManager.getUserId();
                    
                    // Cerrar sesión
                    sessionManager.logout();
                    
                    // Limpiar todos los hábitos de la BD local (ya no hay usuario logueado)
                    // Esto se hace después del logout para que deleteHabitsNotBelongingToCurrentUser funcione
                    // Pero como ya cerramos sesión, eliminamos todos los hábitos
                    try {
                        android.database.sqlite.SQLiteDatabase db = dbHelper.getWritableDatabase();
                        db.delete("habits", null, null);
                        db.close();
                        android.util.Log.d("SettingsActivity", "✅ Todos los hábitos eliminados de la BD local después del logout");
                    } catch (Exception e) {
                        android.util.Log.e("SettingsActivity", "Error al limpiar hábitos en logout", e);
                    }
                    
                    // Redirigir a LoginActivity
                    android.content.Intent intent = new android.content.Intent(this, LoginActivity.class);
                    intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                    
                    Toast.makeText(this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteAccount() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("⚠️ Eliminar Cuenta")
                .setMessage("Esta acción es PERMANENTE y eliminará:\n\n" +
                        "• Tu cuenta de usuario\n" +
                        "• Todos tus hábitos\n" +
                        "• Todo tu progreso y puntajes\n\n" +
                        "¿Estás completamente seguro?")
                .setPositiveButton("Sí, eliminar todo", (dialog, which) -> {
                    // Confirmación adicional
                    new android.app.AlertDialog.Builder(this)
                            .setTitle("Última confirmación")
                            .setMessage("¿Realmente deseas eliminar tu cuenta? No podrás recuperar tus datos.")
                            .setPositiveButton("Eliminar definitivamente", (dialog2, which2) -> {
                                performAccountDeletion();
                            })
                            .setNegativeButton("Cancelar", null)
                            .show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void performAccountDeletion() {
        com.tuempresa.proyecto_01_11_25.utils.SessionManager sessionManager = 
                new com.tuempresa.proyecto_01_11_25.utils.SessionManager(this);
        long userId = sessionManager.getUserId();
        
        com.tuempresa.proyecto_01_11_25.database.HabitDatabaseHelper dbHelper = 
                new com.tuempresa.proyecto_01_11_25.database.HabitDatabaseHelper(this);
        
        boolean deleted = dbHelper.deleteUser(userId);
        
        if (deleted) {
            // Cerrar sesión
            sessionManager.logout();
            
            // Redirigir a LoginActivity
            android.content.Intent intent = new android.content.Intent(this, LoginActivity.class);
            intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            
            Toast.makeText(this, "Cuenta eliminada correctamente", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Error al eliminar la cuenta", Toast.LENGTH_LONG).show();
        }
    }
    
    private void deleteAllHabits() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("⚠️ Borrar Todos los Hábitos")
                .setMessage("¿Estás seguro de que deseas eliminar TODOS tus hábitos?\n\n" +
                        "Esta acción es PERMANENTE y eliminará:\n" +
                        "• Todos tus hábitos\n" +
                        "• Todo el progreso asociado\n" +
                        "• Todas las entradas de diario relacionadas\n\n" +
                        "No podrás recuperar esta información.")
                .setPositiveButton("Sí, borrar todo", (dialog, which) -> {
                    // Confirmación adicional
                    new android.app.AlertDialog.Builder(this)
                            .setTitle("Última confirmación")
                            .setMessage("¿Realmente deseas eliminar todos tus hábitos? Esta acción no se puede deshacer.")
                            .setPositiveButton("Eliminar definitivamente", (dialog2, which2) -> {
                                performDeleteAllHabits();
                            })
                            .setNegativeButton("Cancelar", null)
                            .show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
    
    private void performDeleteAllHabits() {
        try {
            com.tuempresa.proyecto_01_11_25.utils.SessionManager sessionManager = 
                    new com.tuempresa.proyecto_01_11_25.utils.SessionManager(this);
            long userId = sessionManager.getUserId();
            
            com.tuempresa.proyecto_01_11_25.database.HabitDatabaseHelper dbHelper = 
                    new com.tuempresa.proyecto_01_11_25.database.HabitDatabaseHelper(this);
            
            // Obtener todos los hábitos del usuario
            java.util.List<com.tuempresa.proyecto_01_11_25.model.Habit> habits = dbHelper.getAllHabits();
            
            // Eliminar cada hábito
            int deletedCount = 0;
            for (com.tuempresa.proyecto_01_11_25.model.Habit habit : habits) {
                boolean deleted = dbHelper.deleteHabit(habit.getId());
                if (deleted) {
                    deletedCount++;
                }
            }
            
            // Capturar el valor final para usar en la clase interna
            final int finalDeletedCount = deletedCount;
            
            // También intentar eliminar desde la API si hay conexión
            com.tuempresa.proyecto_01_11_25.network.ConnectionMonitor connectionMonitor = 
                    com.tuempresa.proyecto_01_11_25.network.ConnectionMonitor.getInstance(this);
            if (connectionMonitor.isConnected()) {
                // La sincronización se encargará de eliminar en el servidor
                com.tuempresa.proyecto_01_11_25.sync.SyncManager syncManager = 
                        com.tuempresa.proyecto_01_11_25.sync.SyncManager.getInstance(this);
                syncManager.syncAll(new com.tuempresa.proyecto_01_11_25.sync.SyncManager.SyncListener() {
                    @Override
                    public void onSyncStarted() {
                        Toast.makeText(SettingsActivity.this, "Sincronizando eliminaciones...", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSyncCompleted(int syncedCount) {
                        Toast.makeText(SettingsActivity.this, "✅ " + finalDeletedCount + " hábitos eliminados", Toast.LENGTH_LONG).show();
                        finish(); // Cerrar SettingsActivity para que se recargue el Dashboard
                    }

                    @Override
                    public void onSyncError(String error) {
                        Toast.makeText(SettingsActivity.this, "✅ " + finalDeletedCount + " hábitos eliminados localmente. Error al sincronizar: " + error, Toast.LENGTH_LONG).show();
                        finish();
                    }
                });
            } else {
                Toast.makeText(this, "✅ " + finalDeletedCount + " hábitos eliminados. Se sincronizarán al reconectar.", Toast.LENGTH_LONG).show();
                finish();
            }
            
            dbHelper.close();
        } catch (Exception e) {
            Toast.makeText(this, "Error al eliminar hábitos: " + e.getMessage(), Toast.LENGTH_LONG).show();
            android.util.Log.e("SettingsActivity", "Error al eliminar hábitos", e);
        }
    }
    
    private void clearLocalDatabase() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Limpiar Datos Locales")
                .setMessage("¿Estás seguro de que deseas eliminar todos los datos guardados localmente?\n\n" +
                        "Esto eliminará:\n" +
                        "• Todos los hábitos guardados localmente\n" +
                        "• Todos los puntajes guardados localmente\n" +
                        "• Todas las entradas de diario guardadas localmente\n\n" +
                        "Los datos se sincronizarán automáticamente desde la API cuando vuelvas a conectarte.")
                .setPositiveButton("Sí, limpiar", (dialog, which) -> {
                    try {
                        // Eliminar base de datos local
                        com.tuempresa.proyecto_01_11_25.database.HabitDatabaseHelperSync.deleteLocalDatabase(this);
                        
                        // Limpiar flag de SharedPreferences
                        SharedPreferences appPrefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
                        appPrefs.edit().putBoolean("local_db_deleted", false).apply();
                        
                        Toast.makeText(this, "✅ Datos locales eliminados. La app se sincronizará desde la API.", Toast.LENGTH_LONG).show();
                        
                        // Cerrar SettingsActivity y volver al Dashboard para que se recarguen los datos
                        finish();
                    } catch (Exception e) {
                        Toast.makeText(this, "Error al limpiar datos: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}

