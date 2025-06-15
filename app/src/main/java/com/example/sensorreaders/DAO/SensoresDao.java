package com.example.sensorreaders.DAO;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Upsert;

import com.example.sensorreaders.Models.Sensor;

import java.util.List;

@Dao
public interface SensoresDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Sensor sensor);
    @Query("SELECT * FROM sensores_datosensor")
    LiveData<List<Sensor>> getallSensors();
    @Query("SELECT * FROM sensores_datosensor WHERE id = :id")
    LiveData<List<Sensor>> getSensorsById(int id);
    @Update
    void update (Sensor sensor);
    @Delete
    void delete(Sensor sensor);
    @Query("SELECT * FROM sensores_datosensor")
    List<Sensor> getall();
    @Upsert
    void upsert(Sensor sensor);

}
