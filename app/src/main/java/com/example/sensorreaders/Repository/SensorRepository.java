package com.example.sensorreaders.Repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.sensorreaders.DAO.SensoresDao;
import com.example.sensorreaders.Database.Database;
import com.example.sensorreaders.Models.Sensor;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Nullable;

public class SensorRepository {
    private SensoresDao sensoresDao;
    private LiveData<List<Sensor>> list;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    public SensorRepository(@Nullable Application application){
        Database db = Database.getInstance(application);
        sensoresDao = db.sensoresDao();
        list = sensoresDao.getallSensors();
    }

    public LiveData<List<Sensor>> getList(){
        return list;
    }
    public void Insert(Sensor sensor){ executorService.execute(() -> sensoresDao.insert(sensor));}
    public void Update(Sensor sensor){ executorService.execute(()-> sensoresDao.update(sensor));}
    public void Delete(Sensor sensor){ executorService.execute(() -> sensoresDao.delete(sensor));}
}
