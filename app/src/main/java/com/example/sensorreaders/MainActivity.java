package com.example.sensorreaders;

import static android.content.ContentValues.TAG;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;

import com.example.sensorreaders.Models.Sensor;
import com.example.sensorreaders.ViewModel.SensorViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private DatabaseReference MyDataBase;
    private SessionManager sessionManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar SessionManager
        sessionManager = new SessionManager(this);

        // IMPORTANTE: Crear/actualizar sesión al entrar a MainActivity
        // Esto asegura que hay una sesión válida cuando el usuario llega aquí
        sessionManager.createSession();

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        loadFragment(new HistorialFragment());

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            if (item.getItemId() == R.id.nav_clima_actual) {
                //selectedFragment = new SensorFragment();
            } else if (item.getItemId() == R.id.nav_historial) {
                selectedFragment = new HistorialFragment();
            } else if (item.getItemId() == R.id.nav_ajustes) {
                selectedFragment = new AjustesFragment();
            }
         MyDataBase = FirebaseDatabase.getInstance().getReference();

            return loadFragment(selectedFragment);
        });

        SensorViewModel viewModel = new SensorViewModel(MainActivity.this.getApplication());
        LiveData<List<Sensor>> sensorData = viewModel.getSensorList();

            // 2. Crea el PDF
         PDFGenerator pdfGenerator = new PDFGenerator(this);
         pdfGenerator.generateFromLiveData(sensorData, "reporte_sensores_" + System.currentTimeMillis(),this);


         //Los infieles se quejan del trabajo de los fieles
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Solo actualizar actividad, no verificar expiración aquí
        // La verificación de expiración se maneja en Splash y Login
        if (sessionManager.isLoggedIn()) {
            sessionManager.updateLastActivity();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Actualizar la última actividad cuando la app pasa a segundo plano
        if (sessionManager.isLoggedIn()) {
            sessionManager.updateLastActivity();
        }
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        // Actualizar actividad en cada interacción del usuario
        if (sessionManager.isLoggedIn()) {
            sessionManager.updateLastActivity();
        }
    }

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

    //funcion para agregar sensores (sirve de prueba borrar al entregar)
    private void AgregarSensor(Sensor sensor){
        MyDataBase.child("sensores")
                .push()
                .setValue(sensor)
                .addOnFailureListener(e -> {
                    Log.e(TAG,"Error añadir sensores" + e.getMessage());
                });
    }

    /**
     * Método público para cerrar sesión (llamado desde AjustesFragment)
     */
    public void logoutUser() {
        // Limpiar sesión
        sessionManager.clearSession();

        // Cerrar sesión de Firebase
        FirebaseAuth.getInstance().signOut();

        // Redirigir al LoginActivity
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Obtener el SessionManager para uso en fragmentos
     */
    public SessionManager getSessionManager() {
        return sessionManager;
    }
}

