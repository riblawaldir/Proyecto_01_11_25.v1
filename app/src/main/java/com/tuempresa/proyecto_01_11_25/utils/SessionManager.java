package com.tuempresa.proyecto_01_11_25.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "HabitusSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_JWT_TOKEN = "jwtToken";
    private static final String KEY_DISPLAY_NAME = "displayName";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    /**
     * Crea una sesión de login con datos de la API.
     * @param userId ID del usuario
     * @param email Email del usuario
     * @param token Token JWT
     * @param displayName Nombre para mostrar
     */
    public void createLoginSession(long userId, String email, String token, String displayName) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putLong(KEY_USER_ID, userId);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_JWT_TOKEN, token);
        editor.putString(KEY_DISPLAY_NAME, displayName);
        editor.commit();
    }

    /**
     * Método de compatibilidad con código antiguo (sin token).
     * @deprecated Usar createLoginSession con token
     */
    @Deprecated
    public void createLoginSession(long userId, String email) {
        createLoginSession(userId, email, null, null);
    }

    public void logoutUser() {
        editor.clear();
        editor.commit();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false) && getToken() != null;
    }

    public long getUserId() {
        return pref.getLong(KEY_USER_ID, -1);
    }

    public String getUserEmail() {
        return pref.getString(KEY_USER_EMAIL, null);
    }

    /**
     * Obtiene el token JWT almacenado.
     * @return Token JWT o null si no existe
     */
    public String getToken() {
        return pref.getString(KEY_JWT_TOKEN, null);
    }

    /**
     * Guarda el token JWT.
     * @param token Token JWT
     */
    public void setToken(String token) {
        editor.putString(KEY_JWT_TOKEN, token);
        editor.commit();
    }

    public String getDisplayName() {
        return pref.getString(KEY_DISPLAY_NAME, null);
    }

    public void logout() {
        logoutUser();
    }
}
