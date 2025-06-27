package com.example.sensorreaders.ViewModel;



import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.sensorreaders.Interface.SensorApiService;
import com.example.sensorreaders.Models.Sensor;
import com.example.sensorreaders.Repository.SensorRepository;
import com.example.sensorreaders.SensorDeserializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SensorViewModel extends AndroidViewModel {
    private SensorRepository repository;
    private LiveData<List<Sensor>> sensorList;

    private final SensorApiService apiService;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public SensorViewModel(@NonNull Application application) {
        super(application);
        repository = new SensorRepository(application);
        sensorList = repository.getList();

        // ✅ Inicializar Retrofit aquí mismo
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Sensor.class, new SensorDeserializer())
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://100.66.204.124:8000/api/sensores/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        apiService = retrofit.create(SensorApiService.class); // ✅
    }
    // Métodos existentes mejorados
    public LiveData<List<Sensor>> getSensorList() {
        return sensorList;
    }

    public void fetchSensorsDirectly(Callback<List<Sensor>> callback) {
        apiService.getSensores().enqueue(callback);
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
        });
    }
    public void refreshFROMApi(){
        repository.refreshFromApi();
    }

    public void getDataSensorFromDB(){
        executorService.execute(() -> {
            repository.fetchAllFromApi();
        });
    }

    public void startFirebase(){
        repository.enableFirebaseSync();

    }
}