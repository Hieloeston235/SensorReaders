package com.example.sensorreaders;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sensorreaders.Models.Sensor;
import com.example.sensorreaders.ViewModel.SensorViewModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SensorFragment extends Fragment {

    private TextView tvTemperatura, tvHumedad, tvPresion, tvViento, tvLuz, tvLluvia, tvGas, tvHumo, tvHumedadSuelo;
    private Button btnDescargarPDF, btnDescargarExcel;
    private SensorViewModel viewModels;
    private PDFGenerator pdfGenerator;
    private LiveData<List<Sensor>> listaSensores;
    private int Ubi;
    private Integer lastSensorIdShown = null;

    public SensorFragment() {
        // Constructor vacío obligatorio
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sensor, container, false);
        pdfGenerator = new PDFGenerator(getContext());

        viewModels = new SensorViewModel(getActivity().getApplication());

        listaSensores = viewModels.getSensorList();

        tvTemperatura = view.findViewById(R.id.tvTemperatura);
        tvHumedad = view.findViewById(R.id.tvHumedad);
        tvPresion = view.findViewById(R.id.tvPresion);
        tvViento = view.findViewById(R.id.tvViento);
        tvLuz = view.findViewById(R.id.tvLuz);
        tvLluvia = view.findViewById(R.id.tvLluvia);
        tvGas = view.findViewById(R.id.tvGas);
        tvHumo = view.findViewById(R.id.tvHumo);
        tvHumedadSuelo = view.findViewById(R.id.tvHumedadSuelo);

        btnDescargarExcel = view.findViewById(R.id.btnDescargarExcel);
        btnDescargarPDF = view.findViewById(R.id.btnDescargarPDF);

        listaSensores.observe(getActivity(), sensors -> {
            if (sensors != null && !sensors.isEmpty()) {
                int lastIndex = sensors.size() - 1;
                Sensor lastSensor = sensors.get(lastIndex);
                if (lastSensorIdShown == null || !lastSensorIdShown.equals(lastSensor.getId())) {
                    lastSensorIdShown = lastSensor.getId();

                    tvTemperatura.setText(String.format(Locale.getDefault(), "%.2f °C", lastSensor.getTemperatura())); // double
                    tvHumedad.setText(String.format(Locale.getDefault(), "%.0f%%", lastSensor.getHumedad())); // double redondeado a entero
                    tvPresion.setText(String.format(Locale.getDefault(), "%.2f hPa", lastSensor.getPresionAtmosferica())); // double
                    tvViento.setText(String.format(Locale.getDefault(), "%.1f km/h", lastSensor.getViento())); // double
                    tvLuz.setText(String.format(Locale.getDefault(), "%.0f lx", lastSensor.getLuz())); // double redondeado
                    tvLluvia.setText(lastSensor.getLluvia() != null && lastSensor.getLluvia() == 1 ? "Sí" : "No");
                    tvGas.setText(String.format(Locale.getDefault(), "%.0f ppm", lastSensor.getGas())); // double redondeado
                    tvHumo.setText(String.format(Locale.getDefault(), "%.0f ppm", lastSensor.getHumo())); // double redondeado
                    tvHumedadSuelo.setText(String.format(Locale.getDefault(), "%.0f %%", lastSensor.getHumedadSuelo())); // double redondeado
                }


            } else {
                // Limpieza en caso de lista vacía
                tvTemperatura.setText("-");
                tvHumedad.setText("-");
                tvPresion.setText("-");
                tvViento.setText("-");
                tvLuz.setText("-");
                tvLluvia.setText("-");
                tvGas.setText("-");
                tvHumo.setText("-");
                tvHumedadSuelo.setText("-");
            }
        });


        btnDescargarExcel.setOnClickListener(v -> {
            pdfGenerator.generateExcelFromLiveData(listaSensores,"Reporte_Sensores_Excel_" + System.currentTimeMillis(), getActivity());
        });
        btnDescargarPDF.setOnClickListener(v -> {
            pdfGenerator.generateFromLiveData(listaSensores,"Reporte_Sensores_Excel_" + System.currentTimeMillis(), getActivity());
        });



        return view;
    }


}
