package com.example.sensorreaders.Interface;

import com.example.sensorreaders.Models.Sensor;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface SensorApiService {
    @GET("obtener/")
    Call<List<Sensor>> getSensores();
}
