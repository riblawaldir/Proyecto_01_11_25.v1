package com.tuempresa.proyecto_01_11_25.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.tuempresa.proyecto_01_11_25.R;
import com.tuempresa.proyecto_01_11_25.database.HabitDatabaseHelper;
import com.tuempresa.proyecto_01_11_25.model.User;
import com.tuempresa.proyecto_01_11_25.utils.SessionManager;

public class ProfileActivity extends AppCompatActivity {

    private SessionManager session;
    private HabitDatabaseHelper dbHelper;
    private TextView tvUserName, tvUserEmail, tvUserPhone, tvUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        session = new SessionManager(getApplicationContext());
        dbHelper = new HabitDatabaseHelper(this);

        // Referencias UI
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvUserPhone = findViewById(R.id.tvUserPhone);
        tvUserId = findViewById(R.id.tvUserId);
        MaterialButton btnLogout = findViewById(R.id.btnLogout);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);

        // Cargar datos
        loadUserData();

        // Configurar Logout
        btnLogout.setOnClickListener(v -> logout());
        
        // Agregar botones de borrar cuenta y borrar hábitos
        addDeleteButtons();

        // Configurar Navegación
        bottomNav.setSelectedItemId(R.id.nav_profile);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_dashboard) {
                Intent intent = new Intent(this, DashboardActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                finish(); // Evitar apilar actividades
                return true;
            } else if (itemId == R.id.nav_scores) {
                Intent intent = new Intent(this, ScoresActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_profile) {
                return true;
            }
            return false;
        });
    }

    private void loadUserData() {
        String email = session.getUserEmail();
        long userId = session.getUserId();
        
        // Mostrar ID del usuario (siempre disponible desde la sesión)
        if (userId > 0) {
            tvUserId.setText("ID: " + userId);
        } else {
            tvUserId.setText("ID: --");
        }
        
        if (email != null) {
            User user = dbHelper.getUserByEmail(email);
            if (user != null) {
                String fullName = (user.getFirstName() != null ? user.getFirstName() : "") + " " +
                        (user.getLastName() != null ? user.getLastName() : "");
                tvUserName.setText(fullName.trim().isEmpty() ? "Usuario" : fullName.trim());
                tvUserEmail.setText(user.getEmail());
                tvUserPhone.setText(user.getPhone() != null ? user.getPhone() : "Sin teléfono");
                
                // Si el usuario tiene un userId en la BD y es diferente, actualizar el ID mostrado
                if (user.getUserId() > 0 && user.getUserId() != userId) {
                    tvUserId.setText("ID: " + user.getUserId());
                }
            } else {
                tvUserName.setText("Usuario");
                tvUserEmail.setText(email);
            }
        }
    }

    private void logout() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Cerrar Sesión")
                .setMessage("¿Estás seguro de que deseas cerrar sesión?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    // CRÍTICO: Limpiar hábitos antes de cerrar sesión
                    com.tuempresa.proyecto_01_11_25.database.HabitDatabaseHelper dbHelper = 
                        new com.tuempresa.proyecto_01_11_25.database.HabitDatabaseHelper(this);
                    long userId = session.getUserId();
                    
                    session.logoutUser();
                    
                    // Limpiar todos los hábitos de la BD local después del logout
                    try {
                        android.database.sqlite.SQLiteDatabase db = dbHelper.getWritableDatabase();
                        db.delete("habits", null, null);
                        db.close();
                        android.util.Log.d("ProfileActivity", "✅ Todos los hábitos eliminados de la BD local después del logout");
                    } catch (Exception e) {
                        android.util.Log.e("ProfileActivity", "Error al limpiar hábitos en logout", e);
                    }
                    
                    // Redirigir al Login y limpiar pila
                    Intent i = new Intent(this, LoginActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }
    
    private void addDeleteButtons() {
        // Encontrar el contenedor del botón de logout (probablemente un LinearLayout o CardView)
        android.view.ViewGroup rootView = findViewById(android.R.id.content);
        MaterialButton btnLogout = findViewById(R.id.btnLogout);
        
        if (btnLogout == null) return;
        
        // Obtener el padre del botón de logout
        android.view.ViewGroup parent = (android.view.ViewGroup) btnLogout.getParent();
        if (parent == null) return;
        
        // Crear botón de borrar cuenta
        MaterialButton btnDeleteAccount = new MaterialButton(this);
        btnDeleteAccount.setText("Borrar Cuenta");
        btnDeleteAccount.setTextColor(getResources().getColor(android.R.color.white));
        btnDeleteAccount.setBackgroundColor(0xFFD32F2F); // Rojo
        btnDeleteAccount.setCornerRadius((int) (12 * getResources().getDisplayMetrics().density));
        android.view.ViewGroup.LayoutParams deleteAccountParams = new android.view.ViewGroup.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        );
        btnDeleteAccount.setLayoutParams(deleteAccountParams);
        btnDeleteAccount.setOnClickListener(v -> deleteAccount());
        
        // Crear botón de borrar todos los hábitos
        MaterialButton btnDeleteAllHabits = new MaterialButton(this);
        btnDeleteAllHabits.setText("Borrar Todos los Hábitos");
        btnDeleteAllHabits.setTextColor(getResources().getColor(android.R.color.white));
        btnDeleteAllHabits.setBackgroundColor(0xFFFF9800); // Naranja oscuro
        btnDeleteAllHabits.setCornerRadius((int) (12 * getResources().getDisplayMetrics().density));
        android.view.ViewGroup.LayoutParams deleteHabitsParams = new android.view.ViewGroup.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        );
        deleteHabitsParams.width = android.view.ViewGroup.LayoutParams.MATCH_PARENT;
        btnDeleteAllHabits.setLayoutParams(deleteHabitsParams);
        btnDeleteAllHabits.setOnClickListener(v -> deleteAllHabits());
        
        // Agregar márgenes a los botones
        int margin = (int) (16 * getResources().getDisplayMetrics().density);
        if (parent instanceof android.widget.LinearLayout) {
            android.widget.LinearLayout.LayoutParams logoutParams = (android.widget.LinearLayout.LayoutParams) btnLogout.getLayoutParams();
            logoutParams.setMargins(margin, logoutParams.topMargin, margin, margin);
            btnLogout.setLayoutParams(logoutParams);
            
            android.widget.LinearLayout.LayoutParams deleteAccountLayoutParams = new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            );
            deleteAccountLayoutParams.setMargins(margin, 0, margin, margin);
            btnDeleteAccount.setLayoutParams(deleteAccountLayoutParams);
            
            android.widget.LinearLayout.LayoutParams deleteHabitsLayoutParams = new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            );
            deleteHabitsLayoutParams.setMargins(margin, 0, margin, margin);
            btnDeleteAllHabits.setLayoutParams(deleteHabitsLayoutParams);
        }
        
        // Insertar botones después del botón de logout
        int logoutIndex = parent.indexOfChild(btnLogout);
        parent.addView(btnDeleteAccount, logoutIndex + 1);
        parent.addView(btnDeleteAllHabits, logoutIndex + 2);
    }
    
    private void deleteAccount() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("⚠️ Borrar Cuenta")
                .setMessage("Esta acción es PERMANENTE y eliminará:\n\n" +
                        "• Tu cuenta de usuario\n" +
                        "• Todos tus hábitos\n" +
                        "• Todo tu progreso y puntajes\n\n" +
                        "¿Estás completamente seguro?")
                .setPositiveButton("Sí, borrar todo", (dialog, which) -> {
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
        long userId = session.getUserId();
        boolean deleted = dbHelper.deleteUser(userId);
        
        if (deleted) {
            // Cerrar sesión
            session.logoutUser();
            
            // Redirigir a LoginActivity
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            
            android.widget.Toast.makeText(this, "Cuenta eliminada correctamente", android.widget.Toast.LENGTH_LONG).show();
        } else {
            android.widget.Toast.makeText(this, "Error al eliminar la cuenta", android.widget.Toast.LENGTH_LONG).show();
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
            long userId = session.getUserId();
            
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
                        android.widget.Toast.makeText(ProfileActivity.this, "Sincronizando eliminaciones...", android.widget.Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSyncCompleted(int syncedCount) {
                        android.widget.Toast.makeText(ProfileActivity.this, "✅ " + finalDeletedCount + " hábitos eliminados", android.widget.Toast.LENGTH_LONG).show();
                        // Recargar Dashboard
                        Intent intent = new Intent(ProfileActivity.this, DashboardActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onSyncError(String error) {
                        android.widget.Toast.makeText(ProfileActivity.this, "✅ " + finalDeletedCount + " hábitos eliminados localmente. Error al sincronizar: " + error, android.widget.Toast.LENGTH_LONG).show();
                        // Recargar Dashboard
                        Intent intent = new Intent(ProfileActivity.this, DashboardActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                });
            } else {
                android.widget.Toast.makeText(this, "✅ " + finalDeletedCount + " hábitos eliminados. Se sincronizarán al reconectar.", android.widget.Toast.LENGTH_LONG).show();
                // Recargar Dashboard
                Intent intent = new Intent(this, DashboardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        } catch (Exception e) {
            android.widget.Toast.makeText(this, "Error al eliminar hábitos: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
            android.util.Log.e("ProfileActivity", "Error al eliminar hábitos", e);
        }
    }
}
