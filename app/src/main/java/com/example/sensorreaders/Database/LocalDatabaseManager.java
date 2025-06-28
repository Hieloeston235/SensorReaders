package com.example.sensorreaders.Database;

import android.content.Context;

import androidx.room.Room;

public class LocalDatabaseManager {
    private static FirebaseLocalDatabase firebaseDb;
    private static ApiLocalDatabase apiDb;

    public static FirebaseLocalDatabase getFirebaseDb(Context context) {
        if (firebaseDb == null) {
            firebaseDb = Room.databaseBuilder(context.getApplicationContext(),
                            FirebaseLocalDatabase.class, "firebase_sensores.db")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return firebaseDb;
    }

    public static ApiLocalDatabase getApiDb(Context context) {
        if (apiDb == null) {
            apiDb = Room.databaseBuilder(context.getApplicationContext(),
                            ApiLocalDatabase.class, "api_sensores.db")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return apiDb;
    }
}
