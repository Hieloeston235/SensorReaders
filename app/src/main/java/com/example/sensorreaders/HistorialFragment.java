package com.example.sensorreaders;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.sensorreaders.Models.Sensor;
import com.example.sensorreaders.ViewModel.SensorViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class HistorialFragment extends Fragment {

    private Button btnHoy, btnAyer, btn7Dias, btnAplicarFiltro, btnFechaInicio, btnFechaFin, btnDescargarPDF, btnDescargarExcel;
    private RecyclerView recyclerViewHistorial;
    private LinearLayout layoutNoData;
    private SensorViewModel viewModel;
    private PDFGenerator pdfGenerator;
    private List<Sensor> datosFiltrados;
    private Date fechaInicio, fechaFin;
    private SensorAdapter adapter;

    public HistorialFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_historial, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializa componentes
        btnHoy = view.findViewById(R.id.btnHoy);
        btnAyer = view.findViewById(R.id.btnAyer);
        btn7Dias = view.findViewById(R.id.btn7Dias);
        btnAplicarFiltro = view.findViewById(R.id.btnAplicarFiltro);
        btnFechaInicio = view.findViewById(R.id.btnFechaInicio);
        btnFechaFin = view.findViewById(R.id.btnFechaFin);
        btnDescargarPDF = view.findViewById(R.id.btnDescargarPDF);
        btnDescargarExcel = view.findViewById(R.id.btnDescargarExcel);
        recyclerViewHistorial = view.findViewById(R.id.recyclerViewHistorial);
        layoutNoData = view.findViewById(R.id.layoutNoData);

        pdfGenerator = new PDFGenerator(getContext());
        viewModel = new SensorViewModel(getActivity().getApplication());
        viewModel.refreshFROMApi();
        recyclerViewHistorial.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SensorAdapter(); // Asume que ya lo tienes
        recyclerViewHistorial.setAdapter(adapter);

        // Observa la lista de sensores y actualiza el RecyclerView
        viewModel.getSensorList().observe(getViewLifecycleOwner(), sensores -> {
            aplicarFiltroYMostrar(sensores); // Muestra los datos si ya hay filtro
        });

        // Botones de filtro
        btnHoy.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            fechaInicio = inicioDelDia(cal.getTime());
            fechaFin = finDelDia(cal.getTime());
            aplicarFiltroYMostrar(viewModel.getSensorList().getValue());
        });

        btnAyer.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, -1);
            fechaInicio = inicioDelDia(cal.getTime());
            fechaFin = finDelDia(cal.getTime());
            aplicarFiltroYMostrar(viewModel.getSensorList().getValue());
        });

        btn7Dias.setOnClickListener(v -> {
            Calendar calInicio = Calendar.getInstance();
            calInicio.add(Calendar.DAY_OF_YEAR, -6);
            fechaInicio = inicioDelDia(calInicio.getTime());
            fechaFin = finDelDia(new Date());
            aplicarFiltroYMostrar(viewModel.getSensorList().getValue());
        });

        // Botón aplicar filtro (útil si usas un DatePicker, aquí no implementado aún)
        btnAplicarFiltro.setOnClickListener(v -> {
            if (fechaInicio != null && fechaFin != null) {
                aplicarFiltroYMostrar(viewModel.getSensorList().getValue());
            } else {
                Toast.makeText(getContext(), "Selecciona rango de fechas", Toast.LENGTH_SHORT).show();
            }
        });

        // Descarga PDF / Excel
        btnDescargarPDF.setOnClickListener(v -> {
            if (datosFiltrados != null && !datosFiltrados.isEmpty()) {
                pdfGenerator.generateFromList(datosFiltrados, "Historial_PDF_" + System.currentTimeMillis(), getActivity());
            } else {
                Toast.makeText(getContext(), "No hay datos para exportar", Toast.LENGTH_SHORT).show();
            }
        });

        btnDescargarExcel.setOnClickListener(v -> {
            if (datosFiltrados != null && !datosFiltrados.isEmpty()) {
                pdfGenerator.generateExcelFromList(datosFiltrados, "Historial_Excel_" + System.currentTimeMillis(), getActivity());
            } else {
                Toast.makeText(getContext(), "No hay datos para exportar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void aplicarFiltroYMostrar(List<Sensor> lista) {
        if (lista == null || lista.isEmpty()) {
            recyclerViewHistorial.setVisibility(View.GONE);
            layoutNoData.setVisibility(View.VISIBLE);
            return;
        }

        if (fechaInicio == null || fechaFin == null) {
            // Si no hay filtro definido, muestra todo
            datosFiltrados = lista;
        } else {
            datosFiltrados = lista.stream()
                    .filter(s -> {
                        Date fechaSensor = new Date(s.getFecha());
                        return fechaSensor != null && !fechaSensor.before(fechaInicio) && !fechaSensor.after(fechaFin);
                    })
                    .collect(Collectors.toList());
        }

        if (datosFiltrados.isEmpty()) {
            recyclerViewHistorial.setVisibility(View.GONE);
            layoutNoData.setVisibility(View.VISIBLE);
        } else {
            recyclerViewHistorial.setVisibility(View.VISIBLE);
            layoutNoData.setVisibility(View.GONE);
            adapter.setSensorList(datosFiltrados);
        }
    }

    // Métodos auxiliares para manipular fechas
    private Date inicioDelDia(Date fecha) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(fecha);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private Date finDelDia(Date fecha) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(fecha);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }
}
