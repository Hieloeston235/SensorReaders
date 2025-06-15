package com.example.sensorreaders.ViewModel;



import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.sensorreaders.Models.Sensor;
import com.example.sensorreaders.Repository.SensorRepository;

import java.util.List;

public class SensorViewModel extends AndroidViewModel {
    private SensorRepository repository;
    private LiveData<List<Sensor>> list;

    public SensorViewModel(@NonNull Application application){
        super(application);
        repository = new SensorRepository(application);
        list = repository.getList();
    }
    public LiveData<List<Sensor>> getList() {return list;}
    public void Insert(Sensor sensor){
        repository.Insert(sensor);
    }
    public void Update(Sensor sensor){
        repository.Update(sensor);
    }
    public void Delete(Sensor sensor){
        repository.Delete(sensor);
    }
}
