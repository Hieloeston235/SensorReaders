 package com.example.sensorreaders;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

 public class MainActivity extends AppCompatActivity {

     private BottomNavigationView bottomNavigationView;

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
 }
