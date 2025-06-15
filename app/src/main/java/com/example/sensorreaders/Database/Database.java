package com.example.sensorreaders.Database;

import android.content.Context;

import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.sensorreaders.DAO.SensoresDao;

public abstract class  Database extends RoomDatabase {
    public abstract SensoresDao sensoresDao();

    private static Database INSTANCE;
    public static synchronized Database getInstance(Context context){
        if (INSTANCE == null){
            INSTANCE = Room.databaseBuilder(context, Database.class, "ClimaApp").fallbackToDestructiveMigration().build();
        }
        return INSTANCE;
    }
    public static void deletBD(){INSTANCE = null;}
}
