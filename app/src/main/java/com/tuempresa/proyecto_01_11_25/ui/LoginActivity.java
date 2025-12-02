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
import com.tuempresa.proyecto_01_11_25.model.LoginRequest;
import com.tuempresa.proyecto_01_11_25.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private android.widget.EditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private TextView tvRegister;
    private SessionManager sessionManager;
    private AuthApiService authApiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Verificar sesión antes de mostrar la vista
        sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        // Inicializar API Client y Auth Service
        HabitApiClient apiClient = HabitApiClient.getInstance(this);
        authApiService = apiClient.getAuthApiService();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);

        btnLogin.setOnClickListener(v -> login());
        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private void login() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Deshabilitar botón mientras se procesa
        btnLogin.setEnabled(false);
        btnLogin.setText("Iniciando sesión...");

        // Crear petición de login
        LoginRequest loginRequest = new LoginRequest(email, password);

        // Llamar a la API
        Call<AuthResponse> call = authApiService.login(loginRequest);
        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Iniciar Sesión");

                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    
                    if (authResponse.isSuccess() && authResponse.getToken() != null) {
                        // Guardar sesión con token JWT
                        long userId = authResponse.getUser() != null ? authResponse.getUser().getId() : -1;
                        String displayName = authResponse.getUser() != null ? authResponse.getUser().getDisplayName() : email;
                        
                        sessionManager.createLoginSession(
                            userId,
                            email,
                            authResponse.getToken(),
                            displayName
                        );

                        // NO limpiar hábitos aquí - se hará después de sincronizar en DashboardActivity
                        // La limpieza se hará automáticamente después de descargar los hábitos del servidor
                        // Esto evita eliminar hábitos que aún no se han descargado

                        Toast.makeText(LoginActivity.this, "Bienvenido " + displayName, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        String message = authResponse.getMessage() != null ? authResponse.getMessage() : "Error al iniciar sesión";
                        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Error en la respuesta
                    String errorMessage = "Error al conectar con el servidor";
                    if (response.code() == 400) {
                        errorMessage = "Email o contraseña incorrectos";
                    } else if (response.code() == 500) {
                        errorMessage = "Error del servidor. Intenta más tarde";
                    }
                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Iniciar Sesión");
                
                String errorMessage = "Error de conexión. Verifica tu internet";
                if (t.getMessage() != null) {
                    errorMessage += ": " + t.getMessage();
                }
                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                android.util.Log.e("LoginActivity", "Error en login", t);
            }
        });
    }
}
