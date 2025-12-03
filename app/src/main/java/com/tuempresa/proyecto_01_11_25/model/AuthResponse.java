package com.tuempresa.proyecto_01_11_25.model;

/**
 * Modelo de respuesta de autenticación de la API.
 */
public class AuthResponse {
    private boolean success;
    private String message;
    private String token;
    private UserDto user;

    public AuthResponse() {
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserDto getUser() {
        return user;
    }

    public void setUser(UserDto user) {
        this.user = user;
    }
}

