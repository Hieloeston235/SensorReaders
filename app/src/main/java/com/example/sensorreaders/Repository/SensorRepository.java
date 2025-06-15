package com.example.sensorreaders.Repository;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.example.sensorreaders.DAO.SensoresDao;
import com.example.sensorreaders.Database.database;
import com.example.sensorreaders.Models.Sensor;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Nullable;

public class SensorRepository {
    private SensoresDao sensoresDao;
    private LiveData<List<Sensor>> list;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private DatabaseReference firebaseRef;

    public SensorRepository(@Nullable Application application){
        database db = database.getInstance(application);
        sensoresDao = db.sensoresDao();
        list = sensoresDao.getallSensors();

        firebaseRef = FirebaseDatabase.getInstance().getReference("sensores");

        starFirebaseSync();
    }

    private void starFirebaseSync() {
        firebaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                executorService.execute(() -> {
                    List<Sensor> firebaseSensors = new ArrayList<>();

                    // Obtener todos los datos de Firebase
                    for (DataSnapshot child : snapshot.getChildren()) {
                        Sensor sensor = child.getValue(Sensor.class);
                        if (sensor != null) {
                            sensor.setFirebaseKey(child.getKey());
                            firebaseSensors.add(sensor);
                        }
                    }

                    // Sincronizar con la base de datos local
                    syncLocalWithFirebase(firebaseSensors);
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseSync", "Error de sincronización: " + error.getMessage());
            }
        });
    }
    private void syncLocalWithFirebase(List<Sensor> firebaseSensors) {
        // Obtener sensores locales
        List<Sensor> localSensors = sensoresDao.getall();

        // Mapa para búsqueda rápida
        Map<String, Sensor> localSensorsMap = new HashMap<>();
        for (Sensor sensor : localSensors) {
            if (sensor.getFirebaseKey() != null) {
                localSensorsMap.put(sensor.getFirebaseKey(), sensor);
            }
        }

        // 1. Actualizar o insertar sensores de Firebase
        for (Sensor firebaseSensor : firebaseSensors) {
            if (localSensorsMap.containsKey(firebaseSensor.getFirebaseKey())) {
                // Actualizar sensor local si es necesario
                sensoresDao.update(firebaseSensor);
            } else {
                // Insertar nuevo sensor
                firebaseSensor.setFecha(System.currentTimeMillis()); // Asignar la fecha actual
                sensoresDao.insert(firebaseSensor);

            }
        }


    }
    public LiveData<List<Sensor>> getList(){
        return list;
    }
    public void Insert(Sensor sensor){ executorService.execute(() -> sensoresDao.insert(sensor));}
    public void Update(Sensor sensor){ executorService.execute(()-> sensoresDao.update(sensor));}
    public void Delete(Sensor sensor){ executorService.execute(() -> sensoresDao.delete(sensor));}
    public List<Sensor> getAll(){ return sensoresDao.getall();}
}
