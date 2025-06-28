package com.example.sensorreaders.Database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.sensorreaders.DAO.SensoresDao;
import com.example.sensorreaders.Models.Sensor;

@Database( entities = { Sensor.class}, version = 1)
public abstract class ApiLocalDatabase extends RoomDatabase {
    public abstract SensoresDao sensoresDao();
}
