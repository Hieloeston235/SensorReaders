package com.example.sensorreaders.Utilities;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.sensorreaders.R;

import android.Manifest;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SensorNotificationHelper {

    private static final String CHANNEL_ID = "SENSOR_ALERT";
    private static final String CHANNEL_NAME = "AlertaSensores";
    private static final String CHANNEL_DESC = "Canal para alertas críticas de sensores";

    // Constantes para tipos de sensores
    public static final String SENSOR_TEMPERATURA = "temperatura";
    public static final String SENSOR_HUMEDAD = "humedad";
    public static final String SENSOR_PRESION = "presion";
    public static final String SENSOR_VIENTO = "viento";
    public static final String SENSOR_LUZ = "luz";
    public static final String SENSOR_LLUVIA = "lluvia";
    public static final String SENSOR_GAS = "gas";
    public static final String SENSOR_HUMO = "humo";
    public static final String SENSOR_HUMEDAD_SUELO = "humedad_suelo";

    // SharedPreferences para tracking de notificaciones
    private static final String PREFS_NAME = "sensor_notifications";
    private static final String KEY_ACTIVE_NOTIFICATIONS = "active_notifications";

    // Action para el BroadcastReceiver
    private static final String ACTION_NOTIFICATION_DISMISSED = "com.example.sensorreaders.NOTIFICATION_DISMISSED";
    // NUEVO PREFS con tiempo
    private static final String KEY_NOTIFICATION_TIMESTAMP = "notification_timestamps";

    // ExecutorService para tareas en segundo plano
    private static ExecutorService executorService = Executors.newFixedThreadPool(3);

    // Handler para operaciones en UI thread
    private static Handler mainHandler = new Handler(Looper.getMainLooper());

    // Crear canal de notificación
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESC);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    // 1. SOLUCIÓN: Mostrar alerta solo si no existe una activa del mismo tipo
    public static void mostrarAlertaUnica(Context context, String tipoSensor, String mensaje) {

        // Verificar primero si las notificaciones están habilitadas
        SharedPreferences prefs = context.getSharedPreferences("notification_settings", Context.MODE_PRIVATE);

        boolean notificationsEnabled = prefs.getBoolean("notifications_enabled", false);
        Log.d("DEBUG", "Estado notificaciones: " + notificationsEnabled);

        if (!notificationsEnabled) {
            Log.d(TAG, "Notificaciones desactivadas por el usuario");
            return;
        }

        // Ejecutar en segundo plano
        executorService.execute(() -> {
            // Verificar si ya existe una notificación activa para este sensor
            if (existeNotificacionActiva(context, tipoSensor)) {
                Log.d(TAG, "Notificación ya existe para sensor: " + tipoSensor + ". No se enviará duplicada.");
                return;
            }

            // Ejecutar en UI thread para mostrar la notificación
            mainHandler.post(() -> {
                mostrarNotificacionInterna(context, tipoSensor, mensaje);
            });
        });
    }

    // 2. SOLUCIÓN: Procesar múltiples sensores en segundo plano
    public static void procesarMultiplesSensores(Context context, Map<String, String> alertasSensores) {
        // Verificar primero si las notificaciones están habilitadas
        SharedPreferences prefs = context.getSharedPreferences("notification_settings", Context.MODE_PRIVATE);

        boolean notificationsEnabled = prefs.getBoolean("notifications_enabled", false);
        Log.d("DEBUG", "Estado notificaciones: " + notificationsEnabled);

        if (!notificationsEnabled) {
            Log.d(TAG, "Notificaciones desactivadas por el usuario");
            return;
        }
        if (alertasSensores == null || alertasSensores.isEmpty()) {
            return;
        }

        // Ejecutar en segundo plano
        executorService.execute(() -> {
            Log.d(TAG, "Procesando " + alertasSensores.size() + " alertas de sensores en segundo plano");

            for (Map.Entry<String, String> entry : alertasSensores.entrySet()) {
                String tipoSensor = entry.getKey();
                String mensaje = entry.getValue();

                // Verificar si ya existe notificación para este sensor
                if (!existeNotificacionActiva(context, tipoSensor)) {
                    // Enviar notificación en UI thread
                    mainHandler.post(() -> {
                        mostrarNotificacionInterna(context, tipoSensor, mensaje);
                    });

                    // Pequeña pausa entre notificaciones para evitar spam
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        });
    }

    // Método interno para mostrar notificación
    private static void mostrarNotificacionInterna(Context context, String tipoSensor, String mensaje) {
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            verificarPermisos(context, activity);
        }

        if (tienePermisoNotificaciones(context)) {
            int notificationId = getNotificationId(tipoSensor);

            // Crear intent para cuando se descarte la notificación
            Intent dismissIntent = new Intent(context, NotificationDismissedReceiver.class);
            dismissIntent.setAction(ACTION_NOTIFICATION_DISMISSED);
            dismissIntent.putExtra("sensor_type", tipoSensor);
            dismissIntent.putExtra("notification_id", notificationId);

            PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(
                    context,
                    notificationId,
                    dismissIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_warning)
                    .setContentTitle("¡Alerta del Sensor!")
                    .setContentText(mensaje)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setDeleteIntent(dismissPendingIntent); // Intent cuando se descarte

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                try {
                    notificationManager.notify(notificationId, builder.build());
                    marcarNotificacionActiva(context, tipoSensor); // Solo si no lanza excepción
                    Log.d(TAG, "Notificación enviada y marcada activa para: " + tipoSensor);
                } catch (Exception e) {
                    Log.e(TAG, "Fallo al mostrar notificación: " + e.getMessage());
                    limpiarNotificacionesAtascadas(context);
                }
            } else {
                Log.w(TAG, "No hay permiso POST_NOTIFICATIONS");
            }

        }
    }

    // Verificar si existe una notificación activa para el sensor
    private static boolean existeNotificacionActiva(Context context, String tipoSensor) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        long now = System.currentTimeMillis();
        long lastTimestamp = prefs.getLong(KEY_NOTIFICATION_TIMESTAMP + "_" + tipoSensor, 0);

        // Si ya hay una notificación reciente (ej. 15 minutos), no mostrarla
        return (now - lastTimestamp) < 1 * 60 * 1000;
    }

    // Marcar notificación como activa
    private static void marcarNotificacionActiva(Context context, String tipoSensor) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putLong(KEY_NOTIFICATION_TIMESTAMP + "_" + tipoSensor, System.currentTimeMillis())
                .apply();
    }

    // Remover notificación activa (llamado cuando se descarta)
    public static void removerNotificacionActiva(Context context, String tipoSensor) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_NOTIFICATION_TIMESTAMP + "_" + tipoSensor).apply();
        Log.d(TAG, "Timestamp de notificación removido para sensor: " + tipoSensor);
        Log.d(TAG, "Notificación removida para sensor: " + tipoSensor);
    }

    // Generar ID único para cada tipo de sensor
    private static int getNotificationId(String tipoSensor) {
        return tipoSensor.hashCode();
    }

    // Método original mantenido para compatibilidad
    public static void mostrarAlerta(Context context, String mensaje) {
        mostrarAlertaUnica(context, "general", mensaje);
    }

    // Verificar permisos
    public static boolean tienePermisoNotificaciones(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    public static void verificarPermisos(Context context, Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    // Limpiar recursos
    public static void cleanup() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    // BroadcastReceiver para manejar cuando se descarta una notificación
    public static class NotificationDismissedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_NOTIFICATION_DISMISSED.equals(intent.getAction())) {
                String sensorType = intent.getStringExtra("sensor_type");
                if (sensorType != null) {
                    removerNotificacionActiva(context, sensorType);
                }
            }
        }
    }
    public static void limpiarNotificacionesAtascadas(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("sensor_notifications", Context.MODE_PRIVATE);
        prefs.edit().remove("active_notifications").apply();
    }

}
