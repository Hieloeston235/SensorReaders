 package com.example.sensorreaders;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.sensorreaders.Database.Database;
import com.example.sensorreaders.Models.Sensor;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

 public class MainActivity extends AppCompatActivity {

     private BottomNavigationView bottomNavigationView;
     private DatabaseReference MyDataBase;

     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);

         bottomNavigationView = findViewById(R.id.bottom_navigation);

         loadFragment(new SensorFragment());

         bottomNavigationView.setOnItemSelectedListener(item -> {
             Fragment selectedFragment = null;
             if (item.getItemId() == R.id.nav_sensores) {
                 selectedFragment = new SensorFragment();
             } else if (item.getItemId() == R.id.nav_ajustes) {
                 //selectedFragment = new AjustesFragment(); // otro fragmento que se necesite
             }

             return loadFragment(selectedFragment);
         });

         //para probar sensores
         List<String> sensoresN = Arrays.asList("Aire", "Agua","Tierra","Fuego");
         List<String> sensoresH = Arrays.asList("Ayer", "Hoy", "Mañana", "La semana pasada");
         List<Double> sensoresV = Arrays.asList(24.99, 67.99, 123.456, 7.89);
         Random random = new Random();
         MyDataBase = FirebaseDatabase.getInstance().getReference();

         // 1. Obtén los datos de tu Room Database
/*         AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                 AppDatabase.class, "nombre-de-tu-bd").build();

         List<Sensor> sensorData = db.sensorDao().getAllSensors(); // Asegúrate de tener este método en tu DAO
*/
         Database db = Database.getInstance(MainActivity.this);

// 2. Crea el PDF
         PDFGenerator pdfGenerator = new PDFGenerator(this);
         pdfGenerator.generateSensorReport(sensorData, "reporte_sensores_" + System.currentTimeMillis());
         //AgregarSensor(new Sensor(sensoresN.get(random.nextInt(sensoresN.size())), sensoresV.get(random.nextInt(sensoresV.size())), sensoresH.get(random.nextInt(sensoresH.size()))));

         //Los infieles se quejan del trabajo de los fieles
     }

     private boolean loadFragment(Fragment fragment) {
         if (fragment != null) {
             getSupportFragmentManager()
                     .beginTransaction()
                     .replace(R.id.fragment_container, fragment)
                     .commit();
             return true;
         }
         return false;
     }
     //funcion para agregar sensores (sirve de prueba borrar al entregar)
     private void AgregarSensor(Sensor sensor){
         MyDataBase.child("sensores")
                 .push()
                 .setValue(sensor)
                 .addOnFailureListener(e -> {
                     Log.e(TAG,"Error añadir sensores" + e.getMessage());
                 });
     }
 }
