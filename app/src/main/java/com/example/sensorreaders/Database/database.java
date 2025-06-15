package com.example.sensorreaders.Database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.sensorreaders.DAO.SensoresDao;
import com.example.sensorreaders.Models.Sensor;

@Database(entities = {Sensor.class}, version = 7)
public abstract class  database extends RoomDatabase {
    public abstract SensoresDao sensoresDao();

    private static database INSTANCE;
    public static synchronized database getInstance(Context context){
        if (INSTANCE == null){
            INSTANCE = Room.databaseBuilder(context, database.class, "ClimaApp").fallbackToDestructiveMigration().build();
        }
        return INSTANCE;
    }
    public static void deletBD(){INSTANCE = null;}
}
