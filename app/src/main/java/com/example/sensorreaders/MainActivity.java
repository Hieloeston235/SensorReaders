package com.example.sensorreaders;

import static android.content.ContentValues.TAG;
import static androidx.core.content.ContextCompat.startActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.sensorreaders.Database.database;
import com.example.sensorreaders.Models.Sensor;
import com.example.sensorreaders.Utilities.SensorAlertChecker;
import com.example.sensorreaders.Utilities.SensorCheckWorker;
import com.example.sensorreaders.Utilities.SensorNotificationHelper;
import com.example.sensorreaders.ViewModel.SensorViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private DatabaseReference MyDataBase;
    private SessionManager sessionManager;
    private PDFGenerator pdfGenerator;
    private SensorViewModel viewModel;
    private LiveData<List<Sensor>> listaSensores;
    private Integer lastSensorIdShown;
    private String SENSOR_WORK_NAME = "SensorWeather";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Crear canal de notificaciones y limpiar notificaciones atascadas
        SensorNotificationHelper.createNotificationChannel(this);
        SensorNotificationHelper.limpiarNotificacionesAtascadas(this);

        // Inicializar sesión y asegurar que esté activa
        sessionManager = new SessionManager(this);
        sessionManager.createSession();

        // Configurar BottomNavigationView y cargar fragmento inicial
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        loadFragment(new SensorFragment());

        // Manejar selección de fragmentos en la barra de navegación inferior
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            if (item.getItemId() == R.id.nav_clima_actual) {
                selectedFragment = new SensorFragment();
            } else if (item.getItemId() == R.id.nav_historial) {
                selectedFragment = new HistorialFragment();
            } else if (item.getItemId() == R.id.nav_ajustes) {
                selectedFragment = new AjustesFragment();
            } else if (item.getItemId() == R.id.nav_Graficas) {
                selectedFragment = new GraficasFragment();
            }
            MyDataBase = FirebaseDatabase.getInstance().getReference();
            return loadFragment(selectedFragment);
        });

        // Inicializar ViewModel y sincronizar datos desde API a Firebase
        viewModel = new ViewModelProvider(this).get(SensorViewModel.class);
        viewModel.fromApiToFirebase();
        listaSensores = viewModel.getSensorList();

        // Configurar trabajo periódico con WorkManager (cada 15 minutos)
        PeriodicWorkRequest sensorWorkRequest =
                new PeriodicWorkRequest.Builder(SensorCheckWorker.class, 15, TimeUnit.MINUTES)
                        .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                SENSOR_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                sensorWorkRequest
        );
        Log.d(TAG, "onCreate: Se ajustó el WorkManager");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sessionManager.isLoggedIn()) {
            sessionManager.updateLastActivity();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sessionManager.isLoggedIn()) {
            sessionManager.updateLastActivity();
        }
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        if (sessionManager.isLoggedIn()) {
            sessionManager.updateLastActivity();
        }
    }

    // Reemplaza el fragmento actual en el contenedor
    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    // Método para cerrar sesión desde AjustesFragment
    public void logoutUser() {
        sessionManager.clearSession();
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    // Solicita permiso de notificación si es Android 13+
    public void Ejecutable() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Aquí podrías limpiar recursos o tareas pendientes
    }

    // Procesa los datos del sensor más reciente para verificar alertas
    public void onSensorDataReceived(double temp, double humedad, double presion, double viento,
                                     double luz, double lluvia, double gas, double humo, double humedadSuelo, long fecha) {

        Sensor sensor = new Sensor(gas, humedad, humedadSuelo, humo, lluvia, luz, presion, temp, viento, fecha);
        SensorAlertChecker.checkSensorAlerts(this, sensor);
    }

    // Configura la lista de sensores y escucha cambios para procesar nuevos
    public void setlistSensor() {
        viewModel = new SensorViewModel(this.getApplication());
        listaSensores = viewModel.getSensorList();
        viewModel.fromApiToFirebase();

        if (listaSensores.getValue() != null && !listaSensores.getValue().isEmpty()) {
            lastSensorIdShown = listaSensores.getValue().size() - 1;
        } else {
            lastSensorIdShown = null;
        }

        listaSensores.observe(this, new Observer<List<Sensor>>() {
            @Override
            public void onChanged(List<Sensor> sensors) {
                if (sensors == null || sensors.isEmpty()) return;

                int lastIndex = sensors.size() - 1;
                if (lastIndex < 0 || lastIndex >= sensors.size()) return;

                if (lastSensorIdShown != null && lastIndex <= lastSensorIdShown) return;

                lastSensorIdShown = lastIndex;

                Sensor latestSensor = sensors.get(lastIndex);

                onSensorDataReceived(
                        latestSensor.getTemperatura(),
                        latestSensor.getHumedad(),
                        latestSensor.getPresionAtmosferica(),
                        latestSensor.getViento(),
                        latestSensor.getLuz(),
                        latestSensor.getLluvia(),
                        latestSensor.getGas(),
                        latestSensor.getHumo(),
                        latestSensor.getHumedadSuelo(),
                        latestSensor.getFecha()
                );
            }
        });
    }
}
