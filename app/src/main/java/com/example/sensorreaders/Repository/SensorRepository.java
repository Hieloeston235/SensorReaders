package com.example.sensorreaders.Repository;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.example.sensorreaders.DAO.SensoresDao;
import com.example.sensorreaders.Database.LocalDatabaseManager;
import com.example.sensorreaders.Database.database;
import com.example.sensorreaders.Interface.SensorApiService;
import com.example.sensorreaders.Models.Sensor;
import com.example.sensorreaders.NetworkUtil;
import com.example.sensorreaders.SensorDeserializer;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.GsonBuildConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Nullable;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SensorRepository {
    public enum Fuente { API, FIREBASE }

    private SensoresDao sensoresDao;
    private LiveData<List<Sensor>> list;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private DatabaseReference firebaseRef;
    private SensorApiService apiService;
    private ValueEventListener firebaseListener;
    private Call<List<Sensor>> currentApiCall;
    private Application application;

    public SensorRepository(@NonNull Application application, @NonNull Fuente fuente) {
        this.application = application;

        // Inicializa la base de datos adecuada según la fuente
        if (fuente == Fuente.API) {
            sensoresDao = LocalDatabaseManager.getApiDb(application).sensoresDao();
        } else {
            sensoresDao = LocalDatabaseManager.getFirebaseDb(application).sensoresDao();
        }

        list = sensoresDao.getallSensors();

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Sensor.class, new SensorDeserializer())
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://100.66.204.124:8000/api/sensores/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        apiService = retrofit.create(SensorApiService.class);
        firebaseRef = FirebaseDatabase.getInstance().getReference("sensores");
    }

    private void syncWithApi() {
        if (NetworkUtil.isConnectedToInternet(application)) {
            currentApiCall = apiService.getSensores();
            currentApiCall.enqueue(new Callback<List<Sensor>>() {
                @Override
                public void onResponse(Call<List<Sensor>> call, Response<List<Sensor>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        executorService.execute(() -> {
                            List<Sensor> apiSensors = response.body();
                            sensoresDao.deleteAll();
                            for (Sensor sensor : apiSensors) {
                                sensoresDao.insert(sensor);
                            }
                        });
                    } else {
                        Log.e("API", "Respuesta fallida: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<List<Sensor>> call, Throwable t) {
                    if (!call.isCanceled()) {
                        Log.e("API", "Error al llamar a la API: " + t.getMessage());
                    } else {
                        Log.d("API", "Llamada cancelada");
                    }
                }
            });
            Log.d("RefreshApi", "Refrescando desde la Api...");
        } else {
            Log.d("RefreshApi", "Sin conexión. Mostrando datos locales.");
        }
    }

    public void refreshFromApi() {
        syncWithApi();
    }

    public void refreshFromFirebase() {
        startFirebaseSync();
    }

    private void startFirebaseSync() {
        if (NetworkUtil.isConnectedToInternet(application)) {
            firebaseListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    executorService.execute(() -> {
                        List<Sensor> firebaseSensors = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Sensor sensor = child.getValue(Sensor.class);
                            if (sensor != null) {
                                sensor.setFirebaseKey(child.getKey());
                                firebaseSensors.add(sensor);
                            }
                            Log.d("FirebaseSync", "onDataChange: child: " + child.getKey());
                        }
                        sensoresDao.deleteAll();
                        syncLocalWithFirebase(firebaseSensors);
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("FirebaseSync", "Error de sincronización: " + error.getMessage());
                }
            };

            firebaseRef.addValueEventListener(firebaseListener);
            Log.d("RefreshFirebase", "Refrescando desde Firebase...");
        } else {
            Log.d("RefreshFirebase", "Sin conexión. Mostrando datos locales.");
        }
    }

    public void syncLocalWithFirebase(List<Sensor> firebaseSensors) {
        List<Sensor> localSensors = sensoresDao.getall();
        Map<String, Sensor> localSensorsMap = new HashMap<>();
        for (Sensor sensor : localSensors) {
            if (sensor.getFirebaseKey() != null) {
                localSensorsMap.put(sensor.getFirebaseKey(), sensor);
            }
        }

        for (Sensor firebaseSensor : firebaseSensors) {
            if (localSensorsMap.containsKey(firebaseSensor.getFirebaseKey())) {
                sensoresDao.update(firebaseSensor);
            } else {
                firebaseSensor.setFecha(System.currentTimeMillis());
                sensoresDao.insert(firebaseSensor);
            }
        }
    }

    public void disconnectFirebase() {
        if (firebaseRef != null && firebaseListener != null) {
            firebaseRef.removeEventListener(firebaseListener);
            firebaseListener = null;
            Log.d("FirebaseSync", "Listener desconectado");
        }
    }

    public void disconnectApi() {
        if (currentApiCall != null && !currentApiCall.isCanceled()) {
            currentApiCall.cancel();
            Log.d("API", "Llamada API cancelada");
        }
    }

    public LiveData<List<Sensor>> getList() {
        return list;
    }

    public void Insert(Sensor sensor) {
        executorService.execute(() -> sensoresDao.insert(sensor));
    }

    public void Update(Sensor sensor) {
        executorService.execute(() -> sensoresDao.update(sensor));
    }

    public void Delete(Sensor sensor) {
        executorService.execute(() -> sensoresDao.delete(sensor));
    }

    public List<Sensor> getAll() {
        return sensoresDao.getall();
    }
}
