package com.example.sensorreaders.Utilities;

// Agrega este método a tu AjustesFragment o mejor aún, créalo como una clase separada

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.sensorreaders.Models.Sensor;

public class SensorAlertChecker {
    private static final String TAG = "SensorAlertChecker";

    /**
     * Verifica si los datos del sensor generan alguna alerta basada en la configuración guardada
     * @param context Contexto de la aplicación
     * @param sensorData Objeto con todos los datos del sensor
     */
    public static void checkSensorAlerts(Context context, Sensor sensorData) {
        Log.d(TAG, "=== VERIFICANDO ALERTAS DE SENSORES ===");

        // Verificar si las notificaciones están habilitadas
        SharedPreferences prefs = context.getSharedPreferences("notification_settings", Context.MODE_PRIVATE);
        boolean notificationsEnabled = prefs.getBoolean("notifications_enabled", false);

        if (!notificationsEnabled) {
            Log.d(TAG, "Notificaciones deshabilitadas, no se verificarán alertas");
            return;
        }

        Log.d(TAG, "Datos del sensor: " + sensorData.toString());

        // Cargar configuraciones de alertas
        loadAlertSettings(prefs);

        // Verificar cada tipo de sensor
        checkTemperatureAlerts(context, sensorData.getTemperatura(), prefs);
        checkHumidityAlerts(context, sensorData.getHumedad(), prefs);
        checkPressureAlerts(context, sensorData.getPresionAtmosferica(), prefs);
        checkWindAlerts(context, sensorData.getViento(), prefs);
        checkLightAlerts(context, sensorData.getLuz(), prefs);
        checkRainAlerts(context, sensorData.getLluvia(), prefs);
        checkGasAlerts(context, sensorData.getGas(), prefs);
        checkSmokeAlerts(context, sensorData.getHumo(), prefs);
        checkSoilHumidityAlerts(context, sensorData.getHumedadSuelo(), prefs);

        Log.d(TAG, "=== FIN VERIFICACIÓN DE ALERTAS ===");
    }

    private static void loadAlertSettings(SharedPreferences prefs) {
        Log.d(TAG, "Configuraciones de alerta cargadas:");
        Log.d(TAG, "Temperatura: " + prefs.getInt("temp_min", 10) + "°C - " + prefs.getInt("temp_max", 35) + "°C");
        Log.d(TAG, "Humedad: " + prefs.getInt("humedad_min", 30) + "% - " + prefs.getInt("humedad_max", 80) + "%");
        Log.d(TAG, "Presión: " + (prefs.getInt("presion_min", 480) + 500) + "hPa - " + (prefs.getInt("presion_max", 530) + 500) + "hPa");
        Log.d(TAG, "Viento máx: " + prefs.getInt("viento_max", 25) + "km/h");
        Log.d(TAG, "Luz: " + prefs.getInt("luz_min", 100) + "lux - " + prefs.getInt("luz_max", 800) + "lux");
        Log.d(TAG, "Lluvia habilitada: " + prefs.getBoolean("lluvia_enabled", true));
        Log.d(TAG, "Gas máx: " + prefs.getInt("gas_max", 500) + "ppm");
        Log.d(TAG, "Humo máx: " + prefs.getInt("humo_max", 300) + "ppm");
        Log.d(TAG, "Humedad suelo: " + prefs.getInt("humedad_suelo_min", 20) + "% - " + prefs.getInt("humedad_suelo_max", 85) + "%");
    }

    private static void checkTemperatureAlerts(Context context, double temperatura, SharedPreferences prefs) {
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

    private static void checkHumidityAlerts(Context context, double humedad, SharedPreferences prefs) {
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

    private static void checkPressureAlerts(Context context, double presion, SharedPreferences prefs) {
        int presionMin = prefs.getInt("presion_min", 480) + 500; // Aplicar offset
        int presionMax = prefs.getInt("presion_max", 530) + 500; // Aplicar offset

        Log.d(TAG, "Verificando presión: " + presion + "hPa (rango: " + presionMin + "-" + presionMax + "hPa)");

        if (presion < presionMin) {
            String mensaje = "🌀 ALERTA: Presión muy baja (" + presion + "hPa). Mínimo configurado: " + presionMin + "hPa";
            Log.w(TAG, "ALERTA PRESIÓN BAJA: " + mensaje);
            SensorNotificationHelper.mostrarAlerta(context, mensaje);
        } else if (presion > presionMax) {
            String mensaje = "🌀 ALERTA: Presión muy alta (" + presion + "hPa). Máximo configurado: " + presionMax + "hPa";
            Log.w(TAG, "ALERTA PRESIÓN ALTA: " + mensaje);
            SensorNotificationHelper.mostrarAlerta(context, mensaje);
        } else {
            Log.d(TAG, "Presión dentro del rango normal");
        }
    }

    private static void checkWindAlerts(Context context, double viento, SharedPreferences prefs) {
        int vientoMax = prefs.getInt("viento_max", 25);

        Log.d(TAG, "Verificando viento: " + viento + "km/h (máximo: " + vientoMax + "km/h)");

        if (viento > vientoMax) {
            String mensaje = "💨 ALERTA: Viento muy fuerte (" + viento + "km/h). Máximo configurado: " + vientoMax + "km/h";
            Log.w(TAG, "ALERTA VIENTO FUERTE: " + mensaje);
            SensorNotificationHelper.mostrarAlerta(context, mensaje);
        } else {
            Log.d(TAG, "Velocidad del viento dentro del rango normal");
        }
    }

    private static void checkLightAlerts(Context context, double luz, SharedPreferences prefs) {
        int luzMin = prefs.getInt("luz_min", 100);
        int luzMax = prefs.getInt("luz_max", 800);

        Log.d(TAG, "Verificando luz: " + luz + "lux (rango: " + luzMin + "-" + luzMax + "lux)");

        if (luz < luzMin) {
            String mensaje = "💡 ALERTA: Luz muy baja (" + luz + "lux). Mínimo configurado: " + luzMin + "lux";
            Log.w(TAG, "ALERTA LUZ BAJA: " + mensaje);
            SensorNotificationHelper.mostrarAlerta(context, mensaje);
        } else if (luz > luzMax) {
            String mensaje = "☀️ ALERTA: Luz muy alta (" + luz + "lux). Máximo configurado: " + luzMax + "lux";
            Log.w(TAG, "ALERTA LUZ ALTA: " + mensaje);
            SensorNotificationHelper.mostrarAlerta(context, mensaje);
        } else {
            Log.d(TAG, "Intensidad de luz dentro del rango normal");
        }
    }

    private static void checkRainAlerts(Context context, double lluviaA, SharedPreferences prefs) {
        boolean lluvia = false;
        if (lluviaA >= 1){
                lluvia = true;
        }
        boolean lluviaEnabled = prefs.getBoolean("lluvia_enabled", true);

        Log.d(TAG, "Verificando lluvia: " + lluvia + " (alertas habilitadas: " + lluviaEnabled + ")");

        if (lluvia && lluviaEnabled) {
            String mensaje = "🌧️ ALERTA: Se detectó lluvia";
            Log.w(TAG, "ALERTA LLUVIA: " + mensaje);
            SensorNotificationHelper.mostrarAlerta(context, mensaje);
        } else if (lluvia && !lluviaEnabled) {
            Log.d(TAG, "Lluvia detectada pero alertas deshabilitadas");
        } else {
            Log.d(TAG, "No hay lluvia detectada");
        }
    }

    private static void checkGasAlerts(Context context, double gas, SharedPreferences prefs) {
        int gasMax = prefs.getInt("gas_max", 500);

        Log.d(TAG, "Verificando gas: " + gas + "ppm (máximo: " + gasMax + "ppm)");

        if (gas > gasMax) {
            String mensaje = "⚠️ ALERTA: Nivel de gas peligroso (" + gas + "ppm). Máximo configurado: " + gasMax + "ppm";
            Log.w(TAG, "ALERTA GAS PELIGROSO: " + mensaje);
            SensorNotificationHelper.mostrarAlerta(context, mensaje);
        } else {
            Log.d(TAG, "Nivel de gas dentro del rango seguro");
        }
    }

    private static void checkSmokeAlerts(Context context, double humo, SharedPreferences prefs) {
        int humoMax = prefs.getInt("humo_max", 300);

        Log.d(TAG, "Verificando humo: " + humo + "ppm (máximo: " + humoMax + "ppm)");

        if (humo > humoMax) {
            String mensaje = "🚨 ALERTA: Humo detectado (" + humo + "ppm). Máximo configurado: " + humoMax + "ppm";
            Log.w(TAG, "ALERTA HUMO DETECTADO: " + mensaje);
            SensorNotificationHelper.mostrarAlerta(context, mensaje);
        } else {
            Log.d(TAG, "Nivel de humo dentro del rango seguro");
        }
    }

    private static void checkSoilHumidityAlerts(Context context, double humedadSuelo, SharedPreferences prefs) {
        int humedadSueloMin = prefs.getInt("humedad_suelo_min", 20);
        int humedadSueloMax = prefs.getInt("humedad_suelo_max", 85);

        Log.d(TAG, "Verificando humedad del suelo: " + humedadSuelo + "% (rango: " + humedadSueloMin + "-" + humedadSueloMax + "%)");

        if (humedadSuelo < humedadSueloMin) {
            String mensaje = "🌱 ALERTA: Suelo muy seco (" + humedadSuelo + "%). Mínimo configurado: " + humedadSueloMin + "%";
            Log.w(TAG, "ALERTA SUELO SECO: " + mensaje);
            SensorNotificationHelper.mostrarAlerta(context, mensaje);
        } else if (humedadSuelo > humedadSueloMax) {
            String mensaje = "🌊 ALERTA: Suelo muy húmedo (" + humedadSuelo + "%). Máximo configurado: " + humedadSueloMax + "%";
            Log.w(TAG, "ALERTA SUELO HÚMEDO: " + mensaje);
            SensorNotificationHelper.mostrarAlerta(context, mensaje);
        } else {
            Log.d(TAG, "Humedad del suelo dentro del rango normal");
        }
    }
}
