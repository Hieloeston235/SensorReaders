package com.example.sensorreaders;

import android.app.DatePickerDialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.DatePicker;

import com.example.sensorreaders.Models.Sensor;
import com.example.sensorreaders.ViewModel.SensorViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistorialFragment extends Fragment {

    private Button btnHoy, btnAyer, btn7Dias, btnAplicarFiltro, btnFechaInicio, btnFechaFin, btnDescargarPDF, btnDescargarExcel;
    private RecyclerView recyclerViewHistorial;
    private LinearLayout layoutNoData;
    private TextView tvContadorRegistros;
    private SensorViewModel viewModel;
    private PDFGenerator pdfGenerator;
    private List<Sensor> datosFiltrados;
    private Date fechaInicio, fechaFin;
    private SensorAdapter adapter;
    private SimpleDateFormat dateFormat;

    private List<Sensor> listaDesdeApi = new ArrayList<>();


    public HistorialFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_historial, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar vistas
        initViews(view);

        // Configurar formato de fecha
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        // Configurar ViewModel y RecyclerView
        setupViewModelAndRecyclerView();

        // Configurar listeners
        setupClickListeners();

        // Cargar datos iniciales (últimos 7 días por defecto)
        setFiltro7Dias();
    }

    private void initViews(View view) {
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
        tvContadorRegistros = view.findViewById(R.id.tvContadorRegistros);
    }

    private void setupViewModelAndRecyclerView() {
        pdfGenerator = new PDFGenerator(getContext());
        viewModel = new ViewModelProvider(this).get(SensorViewModel.class);

        viewModel.getDataSensorFromDB();
        recyclerViewHistorial.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SensorAdapter();
        recyclerViewHistorial.setAdapter(adapter);

        viewModel.fetchSensorsDirectly(new Callback<List<Sensor>>() {
            @Override
            public void onResponse(Call<List<Sensor>> call, Response<List<Sensor>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaDesdeApi = response.body();
                    aplicarFiltroYMostrar(listaDesdeApi);

                }
            }

            @Override
            public void onFailure(Call<List<Sensor>> call, Throwable t) {
                Toast.makeText(getContext(), "Error al obtener datos de la API", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void setupClickListeners() {
        // Botones de filtro rápido
        btnHoy.setOnClickListener(v -> {
            setFiltroHoy();
            resetearSeleccionBotones();
            btnHoy.setSelected(true);
        });

        btnAyer.setOnClickListener(v -> {
            setFiltroAyer();
            resetearSeleccionBotones();
            btnAyer.setSelected(true);
        });

        btn7Dias.setOnClickListener(v -> {
            setFiltro7Dias();
            resetearSeleccionBotones();
            btn7Dias.setSelected(true);
        });

        // Selectores de fecha
        btnFechaInicio.setOnClickListener(v -> mostrarDatePicker(true));
        btnFechaFin.setOnClickListener(v -> mostrarDatePicker(false));

        // Aplicar filtro personalizado
        btnAplicarFiltro.setOnClickListener(v -> {
            if (fechaInicio != null && fechaFin != null) {
                if (fechaInicio.after(fechaFin)) {
                    Toast.makeText(getContext(), "La fecha de inicio debe ser anterior a la fecha fin", Toast.LENGTH_SHORT).show();
                    return;
                }
                aplicarFiltroYMostrar(viewModel.getSensorList().getValue());
                resetearSeleccionBotones();
                Toast.makeText(getContext(), "Filtro aplicado correctamente", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Selecciona ambas fechas (inicio y fin)", Toast.LENGTH_SHORT).show();
            }
        });

        // Botones de descarga
        btnDescargarPDF.setOnClickListener(v -> {
            if (datosFiltrados != null && !datosFiltrados.isEmpty()) {
                pdfGenerator.generateFromList(datosFiltrados, "Historial_PDF_" + System.currentTimeMillis(), getActivity());
                Toast.makeText(getContext(), "Generando PDF...", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "No hay datos para exportar", Toast.LENGTH_SHORT).show();
            }
        });

        btnDescargarExcel.setOnClickListener(v -> {
            if (datosFiltrados != null && !datosFiltrados.isEmpty()) {
                pdfGenerator.generateExcelFromList(datosFiltrados, "Historial_Excel_" + System.currentTimeMillis(), getActivity());
                Toast.makeText(getContext(), "Generando Excel...", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "No hay datos para exportar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarDatePicker(boolean esFechaInicio) {
        Calendar calendar = Calendar.getInstance();

        // Si ya hay una fecha seleccionada, usar esa como punto de partida
        if (esFechaInicio && fechaInicio != null) {
            calendar.setTime(fechaInicio);
        } else if (!esFechaInicio && fechaFin != null) {
            calendar.setTime(fechaFin);
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (DatePicker view, int year, int month, int dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);

                    if (esFechaInicio) {
                        fechaInicio = inicioDelDia(selectedDate.getTime());
                        btnFechaInicio.setText("📅 " + dateFormat.format(fechaInicio));
                    } else {
                        fechaFin = finDelDia(selectedDate.getTime());
                        btnFechaFin.setText("📅 " + dateFormat.format(fechaFin));
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        // Configurar límites del DatePicker
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

        datePickerDialog.show();
    }

    private void setFiltroHoy() {
        Calendar cal = Calendar.getInstance();
        fechaInicio = inicioDelDia(cal.getTime());
        fechaFin = finDelDia(cal.getTime());
        aplicarFiltroYMostrar(listaDesdeApi);

        actualizarTextoFechas();
    }

    private void setFiltroAyer() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        fechaInicio = inicioDelDia(cal.getTime());
        fechaFin = finDelDia(cal.getTime());
        aplicarFiltroYMostrar(listaDesdeApi);

        actualizarTextoFechas();
    }

    private void setFiltro7Dias() {
        Calendar calInicio = Calendar.getInstance();
        calInicio.add(Calendar.DAY_OF_YEAR, -6);
        fechaInicio = inicioDelDia(calInicio.getTime());
        fechaFin = finDelDia(new Date());
        aplicarFiltroYMostrar(listaDesdeApi);

        actualizarTextoFechas();
    }

    private void actualizarTextoFechas() {
        if (fechaInicio != null) {
            btnFechaInicio.setText("📅 " + dateFormat.format(fechaInicio));
        }
        if (fechaFin != null) {
            btnFechaFin.setText("📅 " + dateFormat.format(fechaFin));
        }
    }

    private void resetearSeleccionBotones() {
        btnHoy.setSelected(false);
        btnAyer.setSelected(false);
        btn7Dias.setSelected(false);
    }

    private void aplicarFiltroYMostrar(List<Sensor> lista) {
        if (lista == null || lista.isEmpty()) {
            recyclerViewHistorial.setVisibility(View.GONE);
            layoutNoData.setVisibility(View.VISIBLE);
            datosFiltrados = null;
            actualizarContadorRegistros(0);
            return;
        }

        if (fechaInicio == null || fechaFin == null) {
            datosFiltrados = lista;
        } else {
            datosFiltrados = lista.stream()
                    .filter(s -> {
                        Date fechaSensor = new Date(s.getFecha());
                        return fechaSensor != null &&
                                !fechaSensor.before(fechaInicio) &&
                                !fechaSensor.after(fechaFin);
                    })
                    .collect(Collectors.toList());
        }

        if (datosFiltrados.isEmpty()) {
            recyclerViewHistorial.setVisibility(View.GONE);
            layoutNoData.setVisibility(View.VISIBLE);
            actualizarContadorRegistros(0);
        } else {
            recyclerViewHistorial.setVisibility(View.VISIBLE);
            layoutNoData.setVisibility(View.GONE);
            adapter.setSensorList(datosFiltrados);
            actualizarContadorRegistros(datosFiltrados.size());
        }
    }

    // Metodo para actualizar el contador de registros
    private void actualizarContadorRegistros(int cantidad) {
        if (tvContadorRegistros != null) {
            String texto = cantidad == 1 ? cantidad + " registro" : cantidad + " registros";
            tvContadorRegistros.setText(texto);
        }
    }

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