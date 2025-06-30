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
    private SensorRepository firebaseRepository;
    private SensorRepository apiRepository;
    private LiveData<List<Sensor>> sensorList;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private SensorRepository.Fuente fuenteActual = SensorRepository.Fuente.FIREBASE;

    public SensorViewModel(@NonNull Application application) {
        super(application);
        firebaseRepository = new SensorRepository(application, SensorRepository.Fuente.FIREBASE);
        apiRepository = new SensorRepository(application, SensorRepository.Fuente.API);
        sensorList = firebaseRepository.getList(); // Por defecto usamos la base de datos de la API
    }
    public void setFuente(SensorRepository.Fuente fuente) {
        this.fuenteActual = fuente;

        if (fuente == SensorRepository.Fuente.FIREBASE) {
            sensorList = firebaseRepository.getList();
        } else {
            sensorList = apiRepository.getList();
        }
    }

    // Métodos existentes mejorados
    public LiveData<List<Sensor>> getSensorList() {
        return sensorList;
    }

    public void insertSensor(Sensor sensor, SensorRepository.Fuente fuente) {
        SensorRepository repo = (fuente == SensorRepository.Fuente.API) ? apiRepository : firebaseRepository;
        executorService.execute(() -> {
            if (sensor.getFirebaseKey() == null) {
                sensor.setLocalOnly(true);
            }
            repo.Insert(sensor);
        });
    }

    public void updateSensor(Sensor sensor, SensorRepository.Fuente fuente) {
        SensorRepository repo = (fuente == SensorRepository.Fuente.API) ? apiRepository : firebaseRepository;
        executorService.execute(() -> {
            if (sensor.getFirebaseKey() == null) {
                sensor.setLocalOnly(true);
            }
            repo.Update(sensor);
        });
    }

    public void deleteSensor(Sensor sensor, SensorRepository.Fuente fuente) {
        SensorRepository repo = (fuente == SensorRepository.Fuente.API) ? apiRepository : firebaseRepository;
        executorService.execute(() -> {
            repo.Delete(sensor);
        });
    }

    // Nuevos métodos para sincronización
    public void syncWithFirebase() {
        executorService.execute(() -> {
            List<Sensor> localSensors = firebaseRepository.getAll();
            for (Sensor sensor : localSensors) {
                if (sensor.isLocalOnly()) {
                    sensor.setLocalOnly(false);
                    firebaseRepository.Insert(sensor);
                }
            }
        });
    }

    public void refreshFromFirebase() {
        firebaseRepository.refreshFromFirebase();
    }

    public void refreshFromApi() {
        apiRepository.refreshFromApi();
    }

    public void disconnectFirebase() {
        firebaseRepository.disconnectFirebase();
    }

    public void disconnectApi() {
        apiRepository.disconnectApi();
    }

    public void fromApiToFirebase() {
       apiRepository.disconnectApi();
       firebaseRepository.refreshFromFirebase();
       setFuente(SensorRepository.Fuente.FIREBASE);
    }

    public void fromFirebaseToApi() {
        firebaseRepository.disconnectFirebase();

        apiRepository.refreshFromApi();
        setFuente(SensorRepository.Fuente.API);
    }

    public List<Sensor> getAllFromApi() {
        return apiRepository.getAll();
    }

    public List<Sensor> getAllFromFirebase() {
        return firebaseRepository.getAll();
    }

}