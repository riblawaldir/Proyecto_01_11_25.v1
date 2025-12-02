package com.tuempresa.proyecto_01_11_25.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.tuempresa.proyecto_01_11_25.R;
import com.tuempresa.proyecto_01_11_25.api.AuthApiService;
import com.tuempresa.proyecto_01_11_25.api.HabitApiClient;
import com.tuempresa.proyecto_01_11_25.model.AuthResponse;
import com.tuempresa.proyecto_01_11_25.model.RegisterRequest;
import com.tuempresa.proyecto_01_11_25.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private android.widget.EditText etFirstName, etLastName, etPhone, etEmail, etPassword, etConfirmPassword;
    private MaterialButton btnRegister;
    private TextView tvLogin;
    private SessionManager sessionManager;
    private AuthApiService authApiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        sessionManager = new SessionManager(this);
        
        // Inicializar API Client y Auth Service
        HabitApiClient apiClient = HabitApiClient.getInstance(this);
        authApiService = apiClient.getAuthApiService();

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

        // Validar longitud mínima de contraseña (API requiere mínimo 6 caracteres)
        if (password.length() < 6) {
            etPassword.setError("La contraseña debe tener al menos 6 caracteres");
            return;
        }

        // Deshabilitar botón mientras se procesa
        btnRegister.setEnabled(false);
        btnRegister.setText("Registrando...");

        // Crear displayName con firstName y lastName
        String displayName = firstName + " " + lastName;

        // Crear petición de registro
        RegisterRequest registerRequest = new RegisterRequest(email, password, displayName);

        // Llamar a la API
        Call<AuthResponse> call = authApiService.register(registerRequest);
        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                btnRegister.setEnabled(true);
                btnRegister.setText("Registrarse");

                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    
                    if (authResponse.isSuccess() && authResponse.getToken() != null) {
                        // Guardar sesión con token JWT
                        long userId = authResponse.getUser() != null ? authResponse.getUser().getId() : -1;
                        String userDisplayName = authResponse.getUser() != null ? authResponse.getUser().getDisplayName() : displayName;
                        
                        sessionManager.createLoginSession(
                            userId,
                            email,
                            authResponse.getToken(),
                            userDisplayName
                        );

                        // NO limpiar hábitos aquí - se hará después de sincronizar en DashboardActivity
                        // La limpieza se hará automáticamente después de descargar los hábitos del servidor
                        // Esto evita eliminar hábitos que aún no se han descargado

                        Toast.makeText(RegisterActivity.this, "Cuenta creada exitosamente", Toast.LENGTH_SHORT).show();

                        // Ir al Dashboard con flags para limpiar el stack
                        Intent intent = new Intent(RegisterActivity.this, DashboardActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        String message = authResponse.getMessage() != null ? authResponse.getMessage() : "Error al crear cuenta";
                        Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Error en la respuesta
                    String errorMessage = "Error al conectar con el servidor";
                    if (response.code() == 400) {
                        errorMessage = "El email ya está registrado o los datos son inválidos";
                    } else if (response.code() == 500) {
                        errorMessage = "Error del servidor. Intenta más tarde";
                    }
                    Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                btnRegister.setEnabled(true);
                btnRegister.setText("Registrarse");
                
                String errorMessage = "Error de conexión. Verifica tu internet";
                if (t.getMessage() != null) {
                    errorMessage += ": " + t.getMessage();
                }
                Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                android.util.Log.e("RegisterActivity", "Error en registro", t);
            }
        });
    }
}
