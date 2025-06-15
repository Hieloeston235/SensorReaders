package com.example.sensorreaders;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "user_session";
    private static final String KEY_LAST_ACTIVITY = "last_activity";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final long SESSION_TIMEOUT = 30 * 60 * 1000; // 30 minutos en milisegundos

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    /**
     * Crear sesión cuando el usuario inicie sesión
     */
    public void createSession() {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putLong(KEY_LAST_ACTIVITY, System.currentTimeMillis());
        editor.apply();
    }

    /**
     * Actualizar la última actividad del usuario
     */
    public void updateLastActivity() {
        editor.putLong(KEY_LAST_ACTIVITY, System.currentTimeMillis());
        editor.apply();
    }

    /**
     * Verificar si la sesión está activa
     * @return true si la sesión está activa, false si expiró
     */
    public boolean isSessionActive() {
        if (!preferences.getBoolean(KEY_IS_LOGGED_IN, false)) {
            return false;
        }

        long lastActivity = preferences.getLong(KEY_LAST_ACTIVITY, 0);
        long currentTime = System.currentTimeMillis();

        // Si han pasado más de 30 minutos desde la última actividad
        if (currentTime - lastActivity > SESSION_TIMEOUT) {
            clearSession();
            return false;
        }

        return true;
    }

    /**
     * Verificar si la sesión es válida (alias para isSessionActive)
     * @return true si la sesión es válida, false si no
     */
    public boolean isSessionValid() {
        return isSessionActive();
    }

    /**
     * Cerrar sesión del usuario
     */
    public void logoutUser() {
        clearSession();
    }

    /**
     * Cerrar sesión y limpiar datos de sesión
     */
    public void clearSession() {
        editor.clear();
        editor.apply();
    }

    /**
     * Verificar si el usuario está logueado
     * @return true si está logueado, false si no
     */
    public boolean isLoggedIn() {
        return preferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Obtener el tiempo restante de sesión en minutos
     * @return minutos restantes de sesión
     */
    public int getSessionTimeRemaining() {
        if (!preferences.getBoolean(KEY_IS_LOGGED_IN, false)) {
            return 0;
        }

        long lastActivity = preferences.getLong(KEY_LAST_ACTIVITY, 0);
        long currentTime = System.currentTimeMillis();
        long timeElapsed = currentTime - lastActivity;
        long timeRemaining = SESSION_TIMEOUT - timeElapsed;

        if (timeRemaining <= 0) {
            return 0;
        }

        return (int) (timeRemaining / (60 * 1000)); // Convertir a minutos
    }
}