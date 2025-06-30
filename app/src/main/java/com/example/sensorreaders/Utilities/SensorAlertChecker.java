package com.example.sensorreaders.Utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.sensorreaders.Models.Sensor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SensorAlertChecker {
    private static final String TAG = "SensorAlertChecker";

    /**
     * Verifica si los datos del sensor generan alguna alerta basada en la configuración guardada
     * @param context Contexto de la aplicación
     * @param sensor Objeto con todos los datos del sensor
     */
    public static void checkSensorAlerts(Context context, Sensor sensor) {
        // Map para almacenar múltiples alertas
        Map<String, String> alertas = new HashMap<>();

        // Verificar cada tipo de sensor y agregar alertas si es necesario
        SharedPreferences prefs = context.getSharedPreferences("notification_settings", Context.MODE_PRIVATE);

        // Log de configuraciones cargadas
        loadAlertSettings(prefs);

        // Verificar temperatura
        double temp = sensor.getTemperatura();
        int tempMin = prefs.getInt("temp_min", 10);
        int tempMax = prefs.getInt("temp_max", 35);

        if (temp < tempMin) {
            alertas.put(SensorNotificationHelper.SENSOR_TEMPERATURA,
                    "Temperatura muy baja: " + temp + "°C (Min: " + tempMin + "°C)");
            Log.w(TAG, "ALERTA TEMPERATURA BAJA: " + temp + "°C");
        } else if (temp > tempMax) {
            alertas.put(SensorNotificationHelper.SENSOR_TEMPERATURA,
                    "Temperatura muy alta: " + temp + "°C (Max: " + tempMax + "°C)");
            Log.w(TAG, "ALERTA TEMPERATURA ALTA: " + temp + "°C");
        }

        // Verificar humedad
        double humedad = sensor.getHumedad();
        int humedadMin = prefs.getInt("humedad_min", 30);
        int humedadMax = prefs.getInt("humedad_max", 80);

        if (humedad < humedadMin) {
            alertas.put(SensorNotificationHelper.SENSOR_HUMEDAD,
                    "Humedad muy baja: " + humedad + "% (Min: " + humedadMin + "%)");
            Log.w(TAG, "ALERTA HUMEDAD BAJA: " + humedad + "%");
        } else if (humedad > humedadMax) {
            alertas.put(SensorNotificationHelper.SENSOR_HUMEDAD,
                    "Humedad muy alta: " + humedad + "% (Max: " + humedadMax + "%)");
            Log.w(TAG, "ALERTA HUMEDAD ALTA: " + humedad + "%");
        }

        // Verificar presión
        double presion = sensor.getPresionAtmosferica();
        int presionMin = prefs.getInt("presion_min", 480) + 500;
        int presionMax = prefs.getInt("presion_max", 530) + 500;

        if (presion < presionMin) {
            alertas.put(SensorNotificationHelper.SENSOR_PRESION,
                    "Presión muy baja: " + presion + " hPa (Min: " + presionMin + " hPa)");
            Log.w(TAG, "ALERTA PRESIÓN BAJA: " + presion + " hPa");
        } else if (presion > presionMax) {
            alertas.put(SensorNotificationHelper.SENSOR_PRESION,
                    "Presión muy alta: " + presion + " hPa (Max: " + presionMax + " hPa)");
            Log.w(TAG, "ALERTA PRESIÓN ALTA: " + presion + " hPa");
        }

        // Verificar viento
        double viento = sensor.getViento();
        int vientoMax = prefs.getInt("viento_max", 25);

        if (viento > vientoMax) {
            alertas.put(SensorNotificationHelper.SENSOR_VIENTO,
                    "Viento muy fuerte: " + viento + " km/h (Max: " + vientoMax + " km/h)");
            Log.w(TAG, "ALERTA VIENTO FUERTE: " + viento + " km/h");
        }

        // Verificar luz
        double luz = sensor.getLuz();
        int luzMin = prefs.getInt("luz_min", 100);
        int luzMax = prefs.getInt("luz_max", 800);

        if (luz < luzMin) {
            alertas.put(SensorNotificationHelper.SENSOR_LUZ,
                    "Luz muy baja: " + luz + " lux (Min: " + luzMin + " lux)");
            Log.w(TAG, "ALERTA LUZ BAJA: " + luz + " lux");
        } else if (luz > luzMax) {
            alertas.put(SensorNotificationHelper.SENSOR_LUZ,
                    "Luz muy alta: " + luz + " lux (Max: " + luzMax + " lux)");
            Log.w(TAG, "ALERTA LUZ ALTA: " + luz + " lux");
        }

        // Verificar lluvia
        boolean lluviaEnabled = prefs.getBoolean("lluvia_enabled", true);
        double lluviaValor = sensor.getLluvia();

        if (lluviaEnabled && lluviaValor > 0) {
            alertas.put(SensorNotificationHelper.SENSOR_LLUVIA,
                    "¡Lluvia detectada! Intensidad: " + lluviaValor);
            Log.w(TAG, "ALERTA LLUVIA: Intensidad " + lluviaValor);
        }

        // Verificar gas
        double gas = sensor.getGas();
        int gasMax = prefs.getInt("gas_max", 500);

        if (gas > gasMax) {
            alertas.put(SensorNotificationHelper.SENSOR_GAS,
                    "Gas detectado: " + gas + " ppm (Max: " + gasMax + " ppm)");
            Log.w(TAG, "ALERTA GAS DETECTADO: " + gas + " ppm");
        }

        // Verificar humo
        double humo = sensor.getHumo();
        int humoMax = prefs.getInt("humo_max", 300);

        if (humo > humoMax) {
            alertas.put(SensorNotificationHelper.SENSOR_HUMO,
                    "¡Humo detectado! " + humo + " ppm (Max: " + humoMax + " ppm)");
            Log.w(TAG, "ALERTA HUMO DETECTADO: " + humo + " ppm");
        }

        // Verificar humedad del suelo
        double humedadSuelo = sensor.getHumedadSuelo();
        int humedadSueloMin = prefs.getInt("humedad_suelo_min", 20);
        int humedadSueloMax = prefs.getInt("humedad_suelo_max", 85);

        if (humedadSuelo < humedadSueloMin) {
            alertas.put(SensorNotificationHelper.SENSOR_HUMEDAD_SUELO,
                    "Suelo muy seco: " + humedadSuelo + "% (Min: " + humedadSueloMin + "%)");
            Log.w(TAG, "ALERTA SUELO SECO: " + humedadSuelo + "%");
        } else if (humedadSuelo > humedadSueloMax) {
            alertas.put(SensorNotificationHelper.SENSOR_HUMEDAD_SUELO,
                    "Suelo muy húmedo: " + humedadSuelo + "% (Max: " + humedadSueloMax + "%)");
            Log.w(TAG, "ALERTA SUELO HÚMEDO: " + humedadSuelo + "%");
        }

        // Procesar todas las alertas en segundo plano
        if (!alertas.isEmpty()) {
            Log.i(TAG, "Procesando " + alertas.size() + " alertas detectadas");
            SensorNotificationHelper.procesarMultiplesSensores(context, alertas);
        } else {
            Log.d(TAG, "No se detectaron alertas para procesar");
        }
    }

    /**
     * Carga y muestra en log las configuraciones de alerta actuales
     */
    private static void loadAlertSettings(SharedPreferences prefs) {
        Log.d(TAG, "=== CONFIGURACIONES DE ALERTA ===");
        Log.d(TAG, "Temperatura: " + prefs.getInt("temp_min", 10) + "°C - " + prefs.getInt("temp_max", 35) + "°C");
        Log.d(TAG, "Humedad: " + prefs.getInt("humedad_min", 30) + "% - " + prefs.getInt("humedad_max", 80) + "%");
        Log.d(TAG, "Presión: " + (prefs.getInt("presion_min", 480) + 500) + "hPa - " + (prefs.getInt("presion_max", 530) + 500) + "hPa");
        Log.d(TAG, "Viento máx: " + prefs.getInt("viento_max", 25) + "km/h");
        Log.d(TAG, "Luz: " + prefs.getInt("luz_min", 100) + "lux - " + prefs.getInt("luz_max", 800) + "lux");
        Log.d(TAG, "Lluvia habilitada: " + prefs.getBoolean("lluvia_enabled", true));
        Log.d(TAG, "Gas máx: " + prefs.getInt("gas_max", 500) + "ppm");
        Log.d(TAG, "Humo máx: " + prefs.getInt("humo_max", 300) + "ppm");
        Log.d(TAG, "Humedad suelo: " + prefs.getInt("humedad_suelo_min", 20) + "% - " + prefs.getInt("humedad_suelo_max", 85) + "%");
        Log.d(TAG, "================================");
    }

    /**
     * Método auxiliar para verificar alertas de temperatura individualmente
     * (mantenido para compatibilidad con código existente)
     */
    public static void checkTemperatureAlerts(Context context, double temperatura) {
        SharedPreferences prefs = context.getSharedPreferences("notification_settings", Context.MODE_PRIVATE);
        int tempMin = prefs.getInt("temp_min", 10);
        int tempMax = prefs.getInt("temp_max", 35);

        Log.d(TAG, "Verificando temperatura: " + temperatura + "°C (rango: " + tempMin + "-" + tempMax + "°C)");

        if (temperatura < tempMin) {
            String mensaje = "🌡️ ALERTA: Temperatura muy baja (" + temperatura + "°C). Mínimo configurado: " + tempMin + "°C";
            Log.w(TAG, "ALERTA TEMPERATURA BAJA: " + mensaje);
            SensorNotificationHelper.mostrarAlerta(context, mensaje);
        } else if (temperatura > tempMax) {
            String mensaje = "🌡️ ALERTA: Temperatura muy alta (" + temperatura + "°C). Máximo configurado: " + tempMax + "°C";
            Log.w(TAG, "ALERTA TEMPERATURA ALTA: " + mensaje);
            SensorNotificationHelper.mostrarAlerta(context, mensaje);
        } else {
            Log.d(TAG, "Temperatura dentro del rango normal");
        }
    }

    /**
     * Método auxiliar para verificar alertas de humedad individualmente
     * (mantenido para compatibilidad con código existente)
     */
    public static void checkHumidityAlerts(Context context, double humedad) {
        SharedPreferences prefs = context.getSharedPreferences("notification_settings", Context.MODE_PRIVATE);
        int humedadMin = prefs.getInt("humedad_min", 30);
        int humedadMax = prefs.getInt("humedad_max", 80);

        Log.d(TAG, "Verificando humedad: " + humedad + "% (rango: " + humedadMin + "-" + humedadMax + "%)");

        if (humedad < humedadMin) {
            String mensaje = "💧 ALERTA: Humedad muy baja (" + humedad + "%). Mínimo configurado: " + humedadMin + "%";
            Log.w(TAG, "ALERTA HUMEDAD BAJA: " + mensaje);
            SensorNotificationHelper.mostrarAlerta(context, mensaje);
        } else if (humedad > humedadMax) {
            String mensaje = "💧 ALERTA: Humedad muy alta (" + humedad + "%). Máximo configurado: " + humedadMax + "%";
            Log.w(TAG, "ALERTA HUMEDAD ALTA: " + mensaje);
            SensorNotificationHelper.mostrarAlerta(context, mensaje);
        } else {
            Log.d(TAG, "Humedad dentro del rango normal");
        }
    }
    // Agregar estos métodos a tu clase SensorAlertChecker existente

    /**
     * Verifica si las notificaciones en segundo plano están habilitadas
     * @param context Contexto de la aplicación
     * @return true si las notificaciones están habilitadas
     */
    public static boolean areBackgroundNotificationsEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("notification_settings", Context.MODE_PRIVATE);
        return prefs.getBoolean("background_notifications_enabled", true);
    }

    /**
     * Habilita o deshabilita las notificaciones en segundo plano
     * @param context Contexto de la aplicación
     * @param enabled true para habilitar, false para deshabilitar
     */
    public static void setBackgroundNotificationsEnabled(Context context, boolean enabled) {
        SharedPreferences prefs = context.getSharedPreferences("notification_settings", Context.MODE_PRIVATE);
        prefs.edit().putBoolean("background_notifications_enabled", enabled).apply();
        Log.d(TAG, "Notificaciones en segundo plano " + (enabled ? "habilitadas" : "deshabilitadas"));
    }

    /**
     * Versión mejorada del método principal que verifica si las notificaciones están habilitadas
     * @param context Contexto de la aplicación
     * @param sensor Objeto con todos los datos del sensor
     */
    public static void checkSensorAlertsIfEnabled(Context context, Sensor sensor) {
        if (!areBackgroundNotificationsEnabled(context)) {
            Log.d(TAG, "Notificaciones en segundo plano deshabilitadas, saltando verificación");
            return;
        }

        // Llamar al método original
        checkSensorAlerts(context, sensor);
    }

    /**
     * Método para verificar múltiples sensores de forma eficiente
     * @param context Contexto de la aplicación
     * @param sensors Lista de sensores a verificar
     */
    public static void checkMultipleSensorsAlerts(Context context, List<Sensor> sensors) {
        if (!areBackgroundNotificationsEnabled(context)) {
            Log.d(TAG, "Notificaciones en segundo plano deshabilitadas");
            return;
        }

        if (sensors == null || sensors.isEmpty()) {
            Log.d(TAG, "No hay sensores para verificar");
            return;
        }

        Log.d(TAG, "Verificando alertas para " + sensors.size() + " sensores");

        int alertasEncontradas = 0;
        for (Sensor sensor : sensors) {
            try {
                // Contar alertas antes de verificar
                Map<String, String> alertasAntes = new HashMap<>();

                checkSensorAlerts(context, sensor);
                alertasEncontradas++;

            } catch (Exception e) {
                Log.e(TAG, "Error al verificar sensor: " + e.getMessage(), e);
            }
        }

        Log.i(TAG, "Verificación completada. Sensores procesados: " + alertasEncontradas);
    }
}