package com.tuempresa.proyecto_01_11_25.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.tuempresa.proyecto_01_11_25.R;
import com.tuempresa.proyecto_01_11_25.database.HabitDatabaseHelper;
import com.tuempresa.proyecto_01_11_25.model.User;
import com.tuempresa.proyecto_01_11_25.utils.SessionManager;

public class RegisterActivity extends AppCompatActivity {

    private android.widget.EditText etFirstName, etLastName, etPhone, etEmail, etPassword, etConfirmPassword;
    private MaterialButton btnRegister;
    private TextView tvLogin;
    private HabitDatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = new HabitDatabaseHelper(this);
        sessionManager = new SessionManager(this);

        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);

        btnRegister.setOnClickListener(v -> register());
        tvLogin.setOnClickListener(v -> finish());
    }

    private void register() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty()
                || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validaciones
        if (!firstName.matches("^[a-zA-Z]+$")) {
            etFirstName.setError("Solo se permiten letras");
            return;
        }

        if (!lastName.matches("^[a-zA-Z]+$")) {
            etLastName.setError("Solo se permiten letras");
            return;
        }

        if (!phone.matches("^\\d{8}$")) {
            etPhone.setError("Debe tener 8 números");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ||
                (!email.endsWith(".com") && !email.endsWith(".org") && !email.endsWith(".net"))) {
            etEmail.setError("Email inválido (debe terminar en .com, .org, etc)");
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dbHelper.getUserByEmail(email) != null) {
            Toast.makeText(this, "El email ya está registrado", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear usuario
        long userId = dbHelper.createUser(email, password, firstName, lastName, phone); // En producción, hashear
                                                                                        // password
        if (userId > 0) {
            sessionManager.createLoginSession(userId, email);
            Toast.makeText(this, "Cuenta creada exitosamente", Toast.LENGTH_SHORT).show();

            // Ir al Dashboard con flags para limpiar el stack
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Error al crear cuenta", Toast.LENGTH_SHORT).show();
        }
    }
}
