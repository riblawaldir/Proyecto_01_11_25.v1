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
    private TextView tvUserName, tvUserEmail, tvUserPhone;

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
        MaterialButton btnLogout = findViewById(R.id.btnLogout);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);

        // Cargar datos
        loadUserData();

        // Configurar Logout
        btnLogout.setOnClickListener(v -> logout());

        // Configurar Navegación
        bottomNav.setSelectedItemId(R.id.nav_profile);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_dashboard) {
                startActivity(new Intent(this, DashboardActivity.class));
                finish(); // Evitar apilar actividades
                return true;
            } else if (itemId == R.id.nav_scores) {
                startActivity(new Intent(this, ScoresActivity.class));
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
        if (email != null) {
            User user = dbHelper.getUserByEmail(email);
            if (user != null) {
                String fullName = (user.getFirstName() != null ? user.getFirstName() : "") + " " +
                        (user.getLastName() != null ? user.getLastName() : "");
                tvUserName.setText(fullName.trim().isEmpty() ? "Usuario" : fullName.trim());
                tvUserEmail.setText(user.getEmail());
                tvUserPhone.setText(user.getPhone() != null ? user.getPhone() : "Sin teléfono");
            } else {
                tvUserName.setText("Usuario");
                tvUserEmail.setText(email);
            }
        }
    }

    private void logout() {
        session.logoutUser();
        // Redirigir al Login y limpiar pila
        Intent i = new Intent(this, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }
}
