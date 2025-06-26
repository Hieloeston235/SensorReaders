package com.example.sensorreaders.Database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.sensorreaders.DAO.SensoresDao;
import com.example.sensorreaders.Models.Sensor;

@Database(entities = {Sensor.class}, version = 8)
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

    public static void RestartBD(Context context){
        // Cierra la instancia actual si está abierta
        if (INSTANCE != null) {
            INSTANCE.close();
            INSTANCE = null;
        }

        // Elimina la base de datos física
        context.deleteDatabase("ClimaApp");

        // Crea una nueva instancia de la BD
        INSTANCE = Room.databaseBuilder(context.getApplicationContext(), database.class, "ClimaApp")
                .fallbackToDestructiveMigration()
                .build();
    }

}
