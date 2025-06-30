package com.example.sensorreaders.Utilities;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.sensorreaders.Models.Sensor;
import com.example.sensorreaders.Repository.SensorRepository;
import com.example.sensorreaders.Utilities.SensorAlertChecker;
import java.util.List;

public class SensorCheckWorker extends Worker {
    private static final String TAG1 = "NotificationWorker";

    public SensorCheckWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        //android.util.Log.d(TAG, "Iniciando verificación de sensores en segundo plano (Firebase) fuera de try");
        try {

            android.util.Log.d(TAG1, "Iniciando verificación de sensores en segundo plano (Firebase)");

            // Obtener Application context correctamente
            Context appContext = getApplicationContext();
            android.app.Application application;

            if (appContext instanceof android.app.Application) {
                application = (android.app.Application) appContext;
            } else {
                // Fallback para obtener la Application
                application = (android.app.Application) appContext.getApplicationContext();
            }

            // Crear instancia del repository solo para Firebase
            SensorRepository firebaseRepository = new SensorRepository(
                    application,
                    SensorRepository.Fuente.FIREBASE
            );

            // Obtener datos solo de Firebase
            List<Sensor> sensors = firebaseRepository.getAll();

            // Usar la clase existente SensorAlertChecker para verificar y enviar notificaciones
            checkSensorsAndNotify(sensors);

            android.util.Log.d(TAG1, "Verificación de sensores Firebase completada exitosamente");
            return Result.success();

        } catch (Exception e) {
            android.util.Log.e(TAG1, "Error en la verificación de sensores Firebase: " + e.getMessage(), e);
            return Result.failure();
        }
    }


    private void checkSensorsAndNotify(List<Sensor> sensors) {
        if (sensors == null || sensors.isEmpty()) {
            android.util.Log.d(TAG1, "No hay sensores en Firebase para verificar");
            return;
        }

        // Verificar si las notificaciones en segundo plano están habilitadas
        if (!SensorAlertChecker.areBackgroundNotificationsEnabled(getApplicationContext())) {
            android.util.Log.d(TAG1, "Notificaciones en segundo plano deshabilitadas");
            return;
        }

        android.util.Log.d(TAG1, "Verificando alertas para " + sensors.size() + " sensores de Firebase");

        // Usar el método optimizado para múltiples sensores
        SensorAlertChecker.checkMultipleSensorsAlerts(getApplicationContext(), sensors);

        android.util.Log.d(TAG1, "Verificación de alertas Firebase completada");
    }

}