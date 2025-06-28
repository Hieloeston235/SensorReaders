package com.example.sensorreaders.ViewModel;



import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.sensorreaders.Models.Sensor;
import com.example.sensorreaders.Repository.SensorRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SensorViewModel extends AndroidViewModel {
    private SensorRepository repository;
    private LiveData<List<Sensor>> sensorList;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public SensorViewModel(@NonNull Application application) {
        super(application);
        repository = new SensorRepository(application);
        sensorList = repository.getList();
    }

    // Métodos existentes mejorados
    public LiveData<List<Sensor>> getSensorList() {
        return sensorList;
    }

    public void insertSensor(Sensor sensor) {
        executorService.execute(() -> {
            // Verificar si el sensor ya existe en Firebase
            if (sensor.getFirebaseKey() == null) {
                // Asignar un ID temporal para operaciones offline
                sensor.setLocalOnly(true);
            }
            repository.Insert(sensor);
        });
    }

    public void updateSensor(Sensor sensor) {
        executorService.execute(() -> {
            // Marcar como modificado localmente si es un sensor offline
            if (sensor.getFirebaseKey() == null) {
                sensor.setLocalOnly(true);
            }
            repository.Update(sensor);
        });
    }

    public void deleteSensor(Sensor sensor) {
        executorService.execute(() -> {
            repository.Delete(sensor);
        });
    }

    // Nuevos métodos para sincronización
    public void syncWithFirebase() {
        executorService.execute(() -> {
            List<Sensor> localSensors = repository.getAll();
            for (Sensor sensor : localSensors) {
                if (sensor.isLocalOnly()) {
                    // Si es un sensor creado offline, sincronizarlo
                    sensor.setLocalOnly(false);
                    repository.Insert(sensor); // Esto ahora lo subirá a Firebase
                }
            }
        });
    }

    // Método para forzar una actualización desde Firebase
    public void refreshFromFirebase() {
        executorService.execute(() -> {
            // Esto activará el listener de Firebase en el Repository
            // y actualizará automáticamente los datos locales
            repository.refreshFromFirebase();
        });
    }
    public void refreshFROMApi(){
        repository.refreshFromApi();
    }
}