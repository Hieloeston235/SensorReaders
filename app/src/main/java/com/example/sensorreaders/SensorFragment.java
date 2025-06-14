package com.example.sensorreaders;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SensorFragment extends Fragment {

    private List<Sensor> listaSensores = new ArrayList<>();
    private DatabaseReference MyDataBase;
    private RecyclerView recyclerView;
    private SensorAdapter adapter;

    public SensorFragment() {
        // Constructor vacío obligatorio
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sensor, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewSensors);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SensorAdapter(listaSensores);
        recyclerView.setAdapter(adapter);

        MyDataBase = FirebaseDatabase.getInstance().getReference();
        cargarSensores();

        return view;
    }

    private void cargarSensores() {
        MyDataBase.child("sensores").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaSensores.clear();
                for (DataSnapshot sensoresData : snapshot.getChildren()) {
                    String nombre = sensoresData.child("nombre").getValue(String.class);
                    String hora = sensoresData.child("hora").getValue(String.class);
                    Double valor = sensoresData.child("valor").getValue(Double.class);

                    if (nombre != null && hora != null && valor != null) {
                        listaSensores.add(new Sensor(nombre, valor, hora));
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error al obtener sensores", error.toException());
            }
        });
    }
}
